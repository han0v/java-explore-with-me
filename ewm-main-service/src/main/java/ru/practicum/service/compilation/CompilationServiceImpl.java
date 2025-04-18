package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.compilation.CompilationRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    public CompilationDto create(NewCompilationDto dto) {
        Set<Event> events = dto.getEvents() != null
                ? new java.util.HashSet<>(eventRepository.findAllById(dto.getEvents()))
                : java.util.Collections.emptySet();

        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(events)
                .build();

        Compilation saved = compilationRepository.save(compilation);

        List<EventShortDto> eventDtos = saved.getEvents().stream()
                .map(event -> eventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toList());

        return compilationMapper.toCompilationDto(saved, eventDtos);
    }

    @Override
    public void delete(Long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }

        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            Set<Event> events = new java.util.HashSet<>(eventRepository.findAllById(dto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);

        List<EventShortDto> eventDtos = updated.getEvents().stream()
                .map(event -> eventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toList());

        return compilationMapper.toCompilationDto(updated, eventDtos);
    }

    @Override
    public List<CompilationDto> findAll(Boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepository.findAll(page).getContent();

        return compilations.stream()
                .filter(c -> pinned == null || c.getPinned().equals(pinned))
                .map(compilation -> {
                    List<EventShortDto> eventDtos = compilation.getEvents().stream()
                            .map(event -> eventMapper.toEventShortDto(event, 0L, 0L))
                            .collect(Collectors.toList());
                    return compilationMapper.toCompilationDto(compilation, eventDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        List<EventShortDto> eventDtos = compilation.getEvents().stream()
                .map(event -> eventMapper.toEventShortDto(event, 0L, 0L))
                .collect(Collectors.toList());

        return compilationMapper.toCompilationDto(compilation, eventDtos);
    }
}
