package com.tenten.studybadge.common.jsondeserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tenten.studybadge.type.schedule.RepeatSituation;
import java.io.IOException;

public class RepeatSituationNumberDeserializer extends JsonDeserializer<RepeatSituation> {
  @Override
  public RepeatSituation deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.getCurrentToken().isNumeric()) {
      int value = p.getIntValue();
      return RepeatSituation.fromInt(value);
    } else {
      String value = p.getText();
      return RepeatSituation.fromString(value);
    }
  }
}