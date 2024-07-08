package com.tenten.studybadge.place.dto;

import com.tenten.studybadge.place.domain.entity.Place;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceRequest {
  @Min(value = -90, message = "유효한 위도(lat) 좌표는 -90 이상이어야 합니다.")
  @Max(value = 90, message = "유효한 위도(lat) 좌표는 90 이하여야 합니다.")
  private double lat;
  @Min(value = -180, message = "유효한 경도(lng) 좌표는 -180 이상이어야 합니다.")
  @Max(value = 180, message = "유효한 경도(lng) 좌표는 180 이하여야 합니다.")
  private double lng;
  @NotBlank(message = "장소의 이름은 필수 입니다.")
  private String placeName;
  @NotBlank(message = "장소의 주소는 필수 입니다.")
  private String placeAddress;

  public Place toEntity() {
    return Place.builder()
        .lat(this.lat)
        .lng(this.lng)
        .placeName(this.placeName)
        .placeAddress(this.placeAddress)
        .build();
  }
}
