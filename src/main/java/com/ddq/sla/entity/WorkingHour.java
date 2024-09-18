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
@Table(name = "working_hour")
public class WorkingHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "shift")
    private int shift;

    @Column(name = "start", nullable = false)
    private String start;

    @Column(name = "start_integer", nullable = false)
    private int startInteger;

    @Column(name = "end", nullable = false)
    private String end;

    @Column(name = "end_integer", nullable = false)
    private int endInteger;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active")
    private int isActive;

    @Column(name = "is_deleted")
    private int isDeleted;

    @Override
    public String toString() {
        return "WorkingHour{" +
                "id=" + id +
                ", shift=" + shift +
                ", startTime=" + start +
                ", endTime=" + end +
                ", description=" + description +
                '}';
    }

}
