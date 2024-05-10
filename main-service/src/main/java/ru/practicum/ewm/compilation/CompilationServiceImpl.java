package ru.practicum.ewm.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapping.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements  CompilationService {

    private final CompilationMapper compilationMapper;

    private final EventService eventService;

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = newCompilationDto.getEvents() != null ?
                eventRepository.findAllById(newCompilationDto.getEvents()) : List.of();

        if (newCompilationDto.getEvents() != null && events.size() < newCompilationDto.getEvents().size()) {
            throw new ConditionsException("Can't create compilation because not all events ids are valid.");
        }

        Compilation compilation = compilationRepository.save(compilationMapper.toCompilation(newCompilationDto, events));

        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        eventService.loadShortEventsViewsNumber(compilationDto.getEvents());

        log.info("Compilation added: {}", compilation);

        return compilationDto;
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Optional<Compilation> compilationContainer = compilationRepository.findById(compilationId);
        if (compilationContainer.isEmpty()) {
            throw new EntityNotFoundException("Compilation with id=" + compilationId + " not found.");
        }
        Compilation compilation = compilationContainer.get();

        List<Event> events = updateCompilationRequest.getEvents() != null ?
                eventRepository.findAllById(updateCompilationRequest.getEvents()) : List.of();

        if (updateCompilationRequest.getEvents() != null && events.size() < updateCompilationRequest.getEvents().size()) {
            throw new ConditionsException("Can't update compilation because not all events ids are valid.");
        }

        compilationMapper.updateCompilation(compilation, updateCompilationRequest, events);

        compilation = compilationRepository.save(compilation);

        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        eventService.loadShortEventsViewsNumber(compilationDto.getEvents());

        log.info("Compilation updated: {}", compilation);

        return compilationDto;
    }

    @Override
    @Transactional
    public void deleteCompilation(long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new EntityNotFoundException("Compilation with id=" + compilationId + " not found.");
        }

        compilationRepository.deleteById(compilationId);

        log.info("Compilation with id=" + compilationId + " was deleted.");
    }

    public CompilationDto getCompilation(long compilationId) {
        Optional<Compilation> compilationContainer = compilationRepository.findById(compilationId);
        if (compilationContainer.isEmpty()) {
            throw new EntityNotFoundException("Compilation with id=" + compilationId + " not found.");
        }
        Compilation compilation = compilationContainer.get();

        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        eventService.loadShortEventsViewsNumber(compilationDto.getEvents());

        log.info("Response for get compilation request with id={} was send.", compilationId);

        return compilationDto;
    }

    public List<CompilationDto> getCompilation(Boolean pinned, int from, int size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));

        List<Compilation> compilations = compilationRepository.findAllWithFilters(pinned, page);

        List<CompilationDto> compilationDtos = compilationMapper.toCompilationDto(compilations);
        List<EventShortDto> eventShortDtos = compilationDtos.stream()
                .flatMap(compilationDto -> compilationDto.getEvents().stream())
                .distinct()
                .collect(Collectors.toList());
        eventService.loadShortEventsViewsNumber(eventShortDtos);

        log.info("Response for get compilations request with pinned={}, from={}, size={} was send.", pinned, from, size);

        return compilationDtos;
    }

}
