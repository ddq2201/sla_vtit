package com.ddq.sla.repository;

import com.ddq.sla.entity.WorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkingHourRepository extends JpaRepository<WorkingHour, Integer> {
    List<WorkingHour> findAllByFromDateLessThanEqualAndToDateGreaterThanEqual(LocalDate startDate, LocalDate endDate);
}
