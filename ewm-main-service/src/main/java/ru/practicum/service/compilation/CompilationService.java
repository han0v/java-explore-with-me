package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto dto);
    void delete(Long compId);
    CompilationDto update(Long compId, UpdateCompilationRequest dto);
    List<CompilationDto> findAll(Boolean pinned, int from, int size);
    CompilationDto findById(Long compId);
}
