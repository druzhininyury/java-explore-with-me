package ru.practicum.ewm.compilation;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(long compilationId);

    CompilationDto getCompilation(long compilationId);

    List<CompilationDto> getCompilation(Boolean pinned, int from, int size);

}
