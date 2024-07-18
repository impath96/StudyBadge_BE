package com.tenten.studybadge.attendance.domain.entity;

import com.tenten.studybadge.common.BaseEntity;
import com.tenten.studybadge.type.attendance.AttendanceStatus;
import com.tenten.studybadge.type.schedule.ScheduleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long id;
    @Setter
    private Long singleScheduleId;
    @Setter
    private Long repeatScheduleId;
    private Long studyMemberId;
    private LocalDateTime attendanceDateTime;

    @Enumerated(EnumType.STRING)
    private ScheduleType scheduleType;

    @Setter
    @Enumerated(EnumType.STRING)
    private AttendanceStatus attendanceStatus;

}
