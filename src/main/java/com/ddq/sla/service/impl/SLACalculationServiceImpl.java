package com.ddq.sla.service.impl;

import com.ddq.sla.entity.DayOff;
import com.ddq.sla.entity.WorkingHour;
import com.ddq.sla.repository.DayOffRepository;
import com.ddq.sla.repository.WorkingHourRepository;
import com.ddq.sla.service.SLACalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SLACalculationServiceImpl implements SLACalculationService {

    private static final Logger logger = LoggerFactory.getLogger(SLACalculationServiceImpl.class);

    private final DayOffRepository dayOffRepository;
    private final WorkingHourRepository workingHourRepository;

    public SLACalculationServiceImpl(DayOffRepository dayOffRepository, WorkingHourRepository workingHourRepository) {
        this.dayOffRepository = dayOffRepository;
        this.workingHourRepository = workingHourRepository;
    }

    // Public method: Main SLA calculation
    @Override
    public String calculateSLAExpiration(LocalDateTime triggerTime, String slaType, double slaValue) {
        logger.info("Starting SLA calculation for triggerTime: {}, slaType: {}, slaValue: {}", triggerTime, slaType, slaValue);

        // Adjust triggerTime if it falls on a holiday or non-working day
        triggerTime = adjustToNextWorkingTime(triggerTime);

        LocalDateTime expirationTime = triggerTime;

        // Get working hours for the period
        List<WorkingHour> workingHours = workingHourRepository.findAllByFromDateLessThanEqualAndToDateGreaterThanEqual(
                triggerTime.toLocalDate(), triggerTime.toLocalDate().plusDays((long) Math.ceil(slaValue))
        );
        logger.info("Working hours retrieved: {}", workingHours);

        // Get all holidays in the period
        List<DayOff> holidays = dayOffRepository.findAllByDateBetween(
                triggerTime.toLocalDate(), triggerTime.toLocalDate().plusWeeks((long) Math.ceil(slaValue))
        );
        logger.info("Holidays retrieved: {}", holidays);

        if ("hour".equalsIgnoreCase(slaType)) {
            logger.info("Calculating hour-based SLA.");
            expirationTime = calculateHourBasedSLA(triggerTime, slaValue, workingHours, holidays);
        } else if ("day".equalsIgnoreCase(slaType)) {
            logger.info("Calculating day-based SLA.");
            expirationTime = calculateDayBasedSLA(triggerTime, slaValue, workingHours, holidays);
        }

        logger.info("SLA calculation completed. Expiration time: {}", expirationTime);
        // Return the formatted expiration time
        String formattedExpirationTime = formatDateTime(expirationTime);
        logger.info("Formatted Expiration Time: {}", formattedExpirationTime);
        return formattedExpirationTime;
    }

    // Public method: Day-based SLA calculation (reuses hour-based logic)
    public LocalDateTime calculateDayBasedSLA(LocalDateTime triggerTime, double slaDays,
                                              List<WorkingHour> workingHours, List<DayOff> holidays) {
        double slaHours = slaDays * 8;  // Convert SLA days to hours
        return calculateHourBasedSLA(triggerTime, slaHours, workingHours, holidays);
    }

    // Public method: Hour-based SLA calculation
    private LocalDateTime calculateHourBasedSLA(LocalDateTime triggerTime, double slaHours,
                                                List<WorkingHour> workingHours, List<DayOff> holidays) {
        logger.info("Starting hour-based SLA calculation for triggerTime: {}, slaHours: {}", triggerTime, slaHours);

        LocalDateTime expirationTime = triggerTime;
        int remainingMinutes = (int) (slaHours * 60);  // Convert SLA hours to minutes

        while (remainingMinutes > 0) {
            List<WorkingHour> todayWorkingHours = getWorkingHoursForDate(expirationTime.toLocalDate(), workingHours);

            if (todayWorkingHours.isEmpty()) {
                expirationTime = getNextWorkingDay(expirationTime, holidays);
                continue;
            }

            boolean shiftFound = false;

            for (WorkingHour todayWorkingHour : todayWorkingHours) {
                LocalTime shiftStart = LocalTime.parse(todayWorkingHour.getStart());
                LocalTime shiftEnd = LocalTime.parse(todayWorkingHour.getEnd());
                LocalDateTime shiftStartDateTime = LocalDateTime.of(expirationTime.toLocalDate(), shiftStart);
                LocalDateTime shiftEndDateTime = LocalDateTime.of(expirationTime.toLocalDate(), shiftEnd);

                if (expirationTime.isBefore(shiftStartDateTime)) {
                    expirationTime = shiftStartDateTime;
                }

                if (expirationTime.isBefore(shiftEndDateTime)) {
                    int minutesLeftInCurrentPeriod = (int) Duration.between(expirationTime, shiftEndDateTime).toMinutes();
                    if (minutesLeftInCurrentPeriod > remainingMinutes) {
                        expirationTime = expirationTime.plusMinutes(remainingMinutes);
                        return expirationTime;
                    } else {
                        remainingMinutes -= minutesLeftInCurrentPeriod;
                        expirationTime = shiftEndDateTime.plusMinutes(1);  // Move to the next shift
                        shiftFound = true;
                        break;  // Break from the shift loop to process the next shift
                    }
                }
            }

            if (!shiftFound) {
                expirationTime = getNextWorkingDay(expirationTime, holidays);
            }
        }

        return expirationTime;
    }

    // Private helper method: Adjust to the next valid working time if the trigger time falls on a holiday or non-working day
    private LocalDateTime adjustToNextWorkingTime(LocalDateTime triggerTime) {
        List<DayOff> holidays = dayOffRepository.findAll();  // Fetch all holidays for easier checks

        while (isHoliday(triggerTime.toLocalDate(), holidays) || getWorkingHoursForDate(triggerTime.toLocalDate(), workingHourRepository.findAll()).isEmpty()) {
            // If it's a holiday or there's no working shift for the day, move to the next day
            triggerTime = triggerTime.plusDays(1).with(LocalTime.MIDNIGHT);
        }

        // If the adjusted time is before the first shift, move to the start of the first working shift of the day
        List<WorkingHour> workingHoursForDay = getWorkingHoursForDate(triggerTime.toLocalDate(), workingHourRepository.findAll());
        if (!workingHoursForDay.isEmpty() && triggerTime.toLocalTime().isBefore(LocalTime.parse(workingHoursForDay.get(0).getStart()))) {
            triggerTime = triggerTime.with(LocalTime.parse(workingHoursForDay.get(0).getStart()));
        }

        return triggerTime;
    }

    // Private helper method: Get the next valid working day, skipping holidays and weekends
    private LocalDateTime getNextWorkingDay(LocalDateTime dateTime, List<DayOff> holidays) {
        LocalDate nextDate = dateTime.toLocalDate().plusDays(1);
        while (isHoliday(nextDate, holidays)) {
            nextDate = nextDate.plusDays(1);
        }
        return LocalDateTime.of(nextDate, LocalTime.MIDNIGHT);
    }

    // Private helper method: Get working hours for a specific date
    private List<WorkingHour> getWorkingHoursForDate(LocalDate date, List<WorkingHour> workingHours) {
        return workingHours.stream()
                .filter(wh -> !date.isBefore(wh.getFromDate()) && !date.isAfter(wh.getToDate()))
                .sorted(Comparator.comparingInt(WorkingHour::getShift))  // Ensure shifts are in order
                .collect(Collectors.toList());
    }

    // Utility method: Check if a date is a holiday
    private boolean isHoliday(LocalDate date, List<DayOff> holidays) {
        return holidays.stream()
                .anyMatch(holiday -> holiday.getDate().equals(date));
    }

    // Format the LocalDateTime in the desired format
    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy, EEEE, HH:mm");
        return dateTime.format(formatter);
    }
}
