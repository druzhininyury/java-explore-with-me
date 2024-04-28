package ru.practicum.ewm.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long>, JpaSpecificationExecutor<Hit> {

    @Query("select new ru.practicum.ewm.dto.stats.ViewStats(hit.app, hit.uri, count(hit.id) as counter) " +
           "from Hit as hit " +
           "where hit.timestamp between :start and :end " +
           "group by hit.app, hit.uri " +
           "order by counter desc")
    List<ViewStats> getStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ewm.dto.stats.ViewStats(hit.app, hit.uri, count(distinct(hit.ip)) as counter) " +
            "from Hit as hit " +
            "where hit.timestamp between :start and :end " +
            "group by hit.app, hit.uri " +
            "order by counter desc")
    List<ViewStats> getStatsUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select new ru.practicum.ewm.dto.stats.ViewStats(hit.app, hit.uri, count(hit.id) as counter) " +
            "from Hit as hit " +
            "where (hit.timestamp between :start and :end) and hit.uri in :uris " +
            "group by hit.app, hit.uri " +
            "order by counter desc")
    List<ViewStats> getStatsWithUris(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);

    @Query("select new ru.practicum.ewm.dto.stats.ViewStats(hit.app, hit.uri, count(distinct(hit.ip)) as counter) " +
            "from Hit as hit " +
            "where (hit.timestamp between :start and :end) and hit.uri in :uris " +
            "group by hit.app, hit.uri " +
            "order by counter desc")
    List<ViewStats> getStatsUniqueWithUris(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("uris") List<String> uris);

}
