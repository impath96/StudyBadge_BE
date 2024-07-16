package com.tenten.studybadge.schedule.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tenten.studybadge.common.jsondeserializer.ScheduleEditRequestDeserializer;
import com.tenten.studybadge.type.schedule.ScheduleType;

@JsonDeserialize(using = ScheduleEditRequestDeserializer.class)
public interface ScheduleEditRequest {

  ScheduleType getEditType();
}
