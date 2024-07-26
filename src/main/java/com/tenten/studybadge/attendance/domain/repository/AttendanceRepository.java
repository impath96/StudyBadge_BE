package com.tenten.studybadge.attendance.domain.repository;

import com.tenten.studybadge.attendance.domain.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findAllBySingleScheduleId(long singleScheduleId);
    List<Attendance> findAllByRepeatScheduleId(long repeatScheduleId);
    List<Attendance> findAllByRepeatScheduleIdAndAttendanceDateTimeBetween(Long repeatScheduleId, LocalDateTime attendanceStartTime, LocalDateTime attendanceEndTime);
    List<Attendance> findAllByRepeatScheduleIdAndStudyMemberId(long repeatScheduleId, Long studyMemberId);
    List<Attendance> findAllBySingleScheduleIdAndStudyMemberId(long singleScheduleId, Long studyMemberId);

}
