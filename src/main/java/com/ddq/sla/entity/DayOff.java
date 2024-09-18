package com.ddq.sla.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "day_off")
public class DayOff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ref_id")
    private Integer refId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "year")
    private Integer year;

    @Column(name = "month")
    private String month;

    @Column(name = "month_of_year")
    private String monthOfYear;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @Column(name = "day")
    private String day;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "weekend", nullable = false)
    private String weekend = "Weekday";

    @Column(name = "day_of_year")
    private Integer dayOfYear;

    @Column(name = "week_of_year")
    private String weekOfYear;

    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "previous_day")
    private LocalDate previousDay;

    @Column(name = "next_day")
    private LocalDate nextDay;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Override
    public String toString() {
        return "DayOff{" +
                "id=" + id +
                ", date=" + date +
                '}';
    }
}
