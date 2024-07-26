package com.tenten.studybadge.common.jsondeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForScheduleEditRequestException;
import com.tenten.studybadge.schedule.dto.RepeatScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.ScheduleEditRequest;
import com.tenten.studybadge.schedule.dto.SingleScheduleEditRequest;
import com.tenten.studybadge.type.schedule.RepeatCycle;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import com.tenten.studybadge.type.schedule.ScheduleType;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleEditRequestDeserializer extends JsonDeserializer<ScheduleEditRequest> {

    @Override
    public ScheduleEditRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        ScheduleType editType = ScheduleType.valueOf(node.get("editType").asText().toUpperCase());

        if (ScheduleType.SINGLE == editType) {
            return createSingleScheduleEditRequest(node);
        } else if (ScheduleType.REPEAT == editType) {
            return createRepeatScheduleEditRequest(node);
        }

        throw new IllegalArgumentForScheduleEditRequestException();
    }

    private SingleScheduleEditRequest createSingleScheduleEditRequest(JsonNode node) {
        return SingleScheduleEditRequest.builder()
            .memberId(node.get("memberId").asLong())
            .scheduleId(node.get("scheduleId").asLong())
            .originType(ScheduleType.valueOf(node.get("originType").asText().toUpperCase()))
            .editType(ScheduleType.valueOf(node.get("editType").asText().toUpperCase()))
            .scheduleName(node.get("scheduleName").asText())
            .scheduleContent(node.get("scheduleContent").asText())
            .selectedDate(LocalDate.parse(node.get("selectedDate").asText()))
            .scheduleStartTime(LocalTime.parse(node.get("scheduleStartTime").asText()))
            .scheduleEndTime(LocalTime.parse(node.get("scheduleEndTime").asText()))
            .placeId(node.has("placeId") ? node.get("placeId").asLong() : null)
            .build();
    }

    private RepeatScheduleEditRequest createRepeatScheduleEditRequest(JsonNode node) {
        return RepeatScheduleEditRequest.builder()
            .memberId(node.get("memberId").asLong())
            .scheduleId(node.get("scheduleId").asLong())
            .originType(ScheduleType.valueOf(node.get("originType").asText().toUpperCase()))
            .editType(ScheduleType.valueOf(node.get("editType").asText().toUpperCase()))
            .scheduleName(node.get("scheduleName").asText())
            .scheduleContent(node.get("scheduleContent").asText())
            .selectedDate(LocalDate.parse(node.get("selectedDate").asText()))
            .scheduleStartTime(LocalTime.parse(node.get("scheduleStartTime").asText()))
            .scheduleEndTime(LocalTime.parse(node.get("scheduleEndTime").asText()))
            .placeId(node.has("placeId") ? node.get("placeId").asLong() : null)
            .repeatCycle(RepeatCycle.valueOf(node.get("repeatCycle").asText().toUpperCase()))
            .repeatSituation(deserializeRepeatSituation(node.get("repeatSituation")))
            .repeatEndDate(LocalDate.parse(node.get("repeatEndDate").asText()))
            .build();
    }

    private RepeatSituation deserializeRepeatSituation(JsonNode node) {
        if (node.isInt()) {
            return RepeatSituation.fromInt(node.asInt());
        } else if (node.isTextual()) {
            return RepeatSituation.fromString(node.asText());
        }
        throw new IllegalArgumentException("Invalid RepeatSituation value");
    }
}

