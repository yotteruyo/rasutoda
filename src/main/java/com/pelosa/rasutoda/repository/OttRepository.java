package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.Ott;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

    public interface OttRepository extends JpaRepository<Ott, Long> {
    Optional<Ott> findByNameIgnoreCase(String name);
}