package com.pelosa.rasutoda.repository;


import com.pelosa.rasutoda.domain.Party;
import com.pelosa.rasutoda.domain.PartyMember;
import com.pelosa.rasutoda.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long>{
    List<PartyMember> findByMember(User member);
    List<PartyMember> findByParty(Party party);
    Optional<PartyMember> findByPartyAndMember(Party party, User member);

}
