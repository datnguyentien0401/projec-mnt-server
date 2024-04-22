package com.example.projecmntserver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.projecmntserver.domain.Team;
import com.example.projecmntserver.dto.response.TeamResponse;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    @Query(" select new com.example.projecmntserver.dto.response.TeamResponse( t.id, t.name, count(m), t.createdAt, t.updatedAt) "
           + " from Team t join Member m on t.id = m.team.id group by  t.id")
    List<TeamResponse> findAllWithNumberOfMembers();
}
