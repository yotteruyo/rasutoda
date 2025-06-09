package com.pelosa.rasutoda.repository;

import com.pelosa.rasutoda.domain.Party;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long>{
    List<Party> findByOttNameIgnoreCase(String ottName);
}
