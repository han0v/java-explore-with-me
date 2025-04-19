package ru.practicum.service.compilation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.compilation.CompilationRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CompilationMapper compilationMapper;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private final Event testEvent = new Event();
    private final EventShortDto testEventShortDto = EventShortDto.builder().build();
    private final Compilation testCompilation = Compilation.builder()
            .id(1L)
            .title("Test Compilation")
            .pinned(true)
            .events(Set.of(testEvent))
            .build();
    private final CompilationDto testCompilationDto = CompilationDto.builder()
            .id(1L)
            .title("Test Compilation")
            .pinned(true)
            .events(List.of(testEventShortDto))
            .build();


    @Test
    void findAll_shouldReturnPinnedCompilations() {
        PageRequest page = PageRequest.of(0, 10);
        Compilation pinnedCompilation = Compilation.builder()
                .id(2L)
                .title("Pinned Compilation")
                .pinned(true)
                .events(Set.of())
                .build();

        when(compilationRepository.findAll(page)).thenReturn(new PageImpl<>(List.of(testCompilation, pinnedCompilation)));
        when(eventMapper.toEventShortDto(any(), anyLong(), anyLong())).thenReturn(testEventShortDto);
        when(compilationMapper.toCompilationDto(eq(testCompilation), any())).thenReturn(testCompilationDto);
        when(compilationMapper.toCompilationDto(eq(pinnedCompilation), any())).thenReturn(
                CompilationDto.builder()
                        .id(2L)
                        .title("Pinned Compilation")
                        .pinned(true)
                        .events(List.of())
                        .build());

        List<CompilationDto> result = compilationService.findAll(true, 0, 10);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(CompilationDto::getPinned));
    }

    @Test
    void findById_shouldReturnCompilationWithEvents() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(testCompilation));
        when(eventMapper.toEventShortDto(any(), anyLong(), anyLong())).thenReturn(testEventShortDto);
        when(compilationMapper.toCompilationDto(any(), any())).thenReturn(testCompilationDto);

        CompilationDto result = compilationService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(1, result.getEvents().size());
        assertEquals(testEventShortDto, result.getEvents().get(0));
    }
}