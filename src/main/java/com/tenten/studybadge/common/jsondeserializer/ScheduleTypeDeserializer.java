package com.tenten.studybadge.common.jsondeserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tenten.studybadge.type.schedule.ScheduleType;
import java.io.IOException;

public class ScheduleTypeDeserializer extends JsonDeserializer<ScheduleType> {
  @Override
  public ScheduleType deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    String value = p.getText().toUpperCase();
    return ScheduleType.valueOf(value);
  }
}