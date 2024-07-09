package com.tenten.studybadge.schedule.domain.entity;


import com.tenten.studybadge.schedule.domain.Schedule;
import com.tenten.studybadge.schedule.dto.ScheduleResponse;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(indexes = @Index(name = "idx_study_channel_id", columnList = "study_channel_id"))
public class RepeatSchedule extends Schedule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private boolean isRepeated;
  @Enumerated(EnumType.STRING)
  private RepeatCycle repeatCycle;
  @Enumerated(EnumType.STRING)
  private RepeatSituation repeatSituation;
  private LocalDate repeatEndDate;
  @Setter
  private Long placeId;

  @Setter
  @ManyToOne
  @JoinColumn(name = "study_channel_id", nullable = false)
  private StudyChannel studyChannel;

  @Builder(builderMethodName = "withoutIdBuilder")
  public RepeatSchedule(String scheduleName, String scheduleContent, LocalDate scheduleDate, LocalTime scheduleStartTime,
      LocalTime scheduleEndTime, boolean isRepeated, RepeatCycle repeatCycle, RepeatSituation repeatSituation,
      LocalDate repeatEndDate, Long placeId, StudyChannel studyChannel) {
    this.scheduleName = scheduleName;
    this.scheduleContent = scheduleContent;
    this.scheduleDate = scheduleDate;
    this.scheduleStartTime = scheduleStartTime;
    this.scheduleEndTime = scheduleEndTime;
    this.isRepeated = isRepeated;
    this.repeatCycle = repeatCycle;
    this.repeatSituation = repeatSituation;
    this.repeatEndDate = repeatEndDate;
    this.placeId = placeId;
    this.studyChannel = studyChannel;
  }

  @Builder(builderMethodName = "withIdBuilder")
  public RepeatSchedule(long scheduleId, String scheduleName, String scheduleContent, LocalDate scheduleDate, LocalTime scheduleStartTime,
      LocalTime scheduleEndTime, boolean isRepeated, RepeatCycle repeatCycle, RepeatSituation repeatSituation,
      LocalDate repeatEndDate, Long placeId, StudyChannel studyChannel) {
    this.id = scheduleId;
    this.scheduleName = scheduleName;
    this.scheduleContent = scheduleContent;
    this.scheduleDate = scheduleDate;
    this.scheduleStartTime = scheduleStartTime;
    this.scheduleEndTime = scheduleEndTime;
    this.isRepeated = isRepeated;
    this.repeatCycle = repeatCycle;
    this.repeatSituation = repeatSituation;
    this.repeatEndDate = repeatEndDate;
    this.placeId = placeId;
    this.studyChannel = studyChannel;
  }

  public ScheduleResponse toResponse() {
    return ScheduleResponse.builder()
        .id(this.getId())
        .scheduleName(this.getScheduleName())
        .scheduleContent(this.getScheduleContent())
        .scheduleDate(this.getScheduleDate())
        .scheduleStartTime(this.getScheduleStartTime())
        .scheduleEndTime(this.getScheduleEndTime())
        .isRepeated(this.isRepeated())
        .repeatCycle(this.getRepeatCycle())
        .repeatSituation(this.getRepeatSituation())
        .repeatEndDate(this.getRepeatEndDate())
        .placeId(this.getPlaceId())
        .studyChannelId(this.getStudyChannel().getId())
        .build();
  }
}