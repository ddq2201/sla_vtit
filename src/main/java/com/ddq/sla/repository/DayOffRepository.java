package com.ddq.sla.repository;

import com.ddq.sla.entity.DayOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DayOffRepository extends JpaRepository<DayOff, Integer> {
    List<DayOff> findAllByDateBetween(LocalDate startDate, LocalDate endDate);
}
