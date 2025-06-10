package com.pelosa.rasutoda.repository;


import com.pelosa.rasutoda.domain.PartyMember;
import com.pelosa.rasutoda.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long>{
    List<PartyMember> findByMember(User member);
}
