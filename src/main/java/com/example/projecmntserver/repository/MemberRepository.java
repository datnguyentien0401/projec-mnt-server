package com.example.projecmntserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.projecmntserver.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    void deleteAllByTeamId(Long teamId);

    Optional<Member> findFirstByJiraMemberId(String jiraMemberId);

    List<Member> findMembersByTeamId(Long teamId);
}
