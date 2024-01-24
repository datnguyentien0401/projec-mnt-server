package com.example.projecmntserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.projecmntserver.domain.Planning;

@Repository
public interface PlanningRepository extends JpaRepository<Planning, Long> {
}
