package com.tenten.studybadge.type.schedule;

import com.tenten.studybadge.common.exception.schedule.IllegalArgumentForRepeatSituationException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RepeatSituation {
  // Daily
  EVERYDAY("매일"),

  // Weekly
  MONDAY("월요일"),
  TUESDAY("화요일"),
  WEDNESDAY("수요일"),
  THURSDAY("목요일"),
  FRIDAY("금요일"),

  // Monthly
  MONTHLY_ONE(1), MONTHLY_TWO(2), MONTHLY_THREE(3), MONTHLY_FOUR(4), MONTHLY_FIVE(5),
  MONTHLY_SIX(6), MONTHLY_SEVEN(7), MONTHLY_EIGHT(8), MONTHLY_NINE(9), MONTHLY_TEN(10),
  MONTHLY_ELEVEN(11), MONTHLY_TWELVE(12), MONTHLY_THIRTEEN(13), MONTHLY_FOURTEEN(14),
  MONTHLY_FIFTEEN(15), MONTHLY_SIXTEEN(16), MONTHLY_SEVENTEEN(17), MONTHLY_EIGHTEEN(18),
  MONTHLY_NINETEEN(19), MONTHLY_TWENTY(20), MONTHLY_TWENTY_ONE(21), MONTHLY_TWENTY_TWO(22),
  MONTHLY_TWENTY_THREE(23), MONTHLY_TWENTY_FOUR(24), MONTHLY_TWENTY_FIVE(25),
  MONTHLY_TWENTY_SIX(26), MONTHLY_TWENTY_SEVEN(27), MONTHLY_TWENTY_EIGHT(28),
  MONTHLY_TWENTY_NINE(29), MONTHLY_THIRTY(30), MONTHLY_THIRTY_ONE(31);


  private final Object description;

  public Object getDescription() {
    return description;
  }

  public static RepeatSituation fromString(String value) {
    for (RepeatSituation situation : values()) {
      if (situation.description.equals(value) || situation.name().equalsIgnoreCase(value)) {
        return situation;
      }
    }
    throw new IllegalArgumentException("Unknown repeat situation: " + value);
  }

  public static RepeatSituation fromInt(int value) {
    for (RepeatSituation situation : values()) {
      if (situation.description instanceof Integer && (Integer) situation.description == value) {
        return situation;
      }
    }
    throw new IllegalArgumentException("Unknown repeat situation: " + value);
  }
}
