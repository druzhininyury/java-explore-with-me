package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.mapping.LocationMapper;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.LocationRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationMapper locationMapper;

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public Location getOrCreate(LocationDto locationDto) {
        Optional<Location> container = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());
        Location location;
        if (container.isPresent()) {
            location = container.get();
        } else {
            location = locationRepository.save(locationMapper.toLocation(locationDto));
            log.info("Location added: {}", location);
        }
        log.info("Location with id={} was send.", location.getId());
        return location;
    }
}
