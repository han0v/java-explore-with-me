package ru.practicum.service.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.model.Event;
import ru.practicum.model.EventSort;
import ru.practicum.model.EventState;
import ru.practicum.repository.event.EventRepositoryCustom;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Event> findPublicEventsWithFilters(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            Pageable pageable
    ) {
        // Валидация временного диапазона
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Дата начала диапазона не может быть позже даты окончания");
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

        if (text != null && !text.isBlank()) {
            Predicate annotationPredicate = cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
            predicates.add(cb.or(annotationPredicate, descriptionPredicate));
        }

        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }

        if (paid != null) {
            predicates.add(cb.equal(root.get("paid"), paid));
        }

        if (rangeStart != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            predicates.add(cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit")));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        if (sort == EventSort.EVENT_DATE) {
            cq.orderBy(cb.asc(root.get("eventDate")));
        } else if (sort == EventSort.VIEWS) {
            cq.orderBy(cb.desc(root.get("views")));
        }

        TypedQuery<Event> query = entityManager.createQuery(cq);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }
}

