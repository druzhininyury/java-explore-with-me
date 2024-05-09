package ru.practicum.ewm;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("select compilation " +
           "from Compilation compilation " +
           "where (:pinned is null or compilation.pinned = :pinned) ")
    List<Compilation> findAllWithFilters(@Param("pinned") Boolean pinned, Pageable pageable);

}
