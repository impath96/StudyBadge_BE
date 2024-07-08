package com.tenten.studybadge.place.service;

import static com.tenten.studybadge.type.study.channel.Category.IT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tenten.studybadge.common.exception.place.NotFoundPlaceException;
import com.tenten.studybadge.place.domain.entity.Place;
import com.tenten.studybadge.place.domain.repository.PlaceRepository;
import com.tenten.studybadge.place.dto.PlaceCreateResponse;
import com.tenten.studybadge.place.dto.PlaceRequest;
import com.tenten.studybadge.place.dto.PlaceResponse;
import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PlaceServiceTest {

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private StudyChannelRepository studyChannelRepository;
  @InjectMocks
  private PlaceService placeService;

  private PlaceRequest placeRequest;
  private Place place;
  private StudyChannel studyChannel;
  private PlaceResponse placeResponse;

  private PlaceRequest newNamePlaceRequest;
  private PlaceRequest sameNamePlaceRequest;
  private Place existingPlace;
  private PlaceResponse newNamePlaceResponse;

  @BeforeEach
  void setUp() {
    placeRequest = PlaceRequest.builder()
        .lat(37.7749)
        .lng(-122.4194)
        .placeName("A 카페")
        .placeAddress("123 Main St")
        .build();

    place = Place.builder()
        .id(1L)
        .lat(37.7749)
        .lng(-122.4194)
        .placeName("A 카페")
        .placeAddress("123 Main St")
        .build();

    studyChannel = StudyChannel.builder()
        .id(1L)
        .category(IT)
        .name("test studyChannel")
        .build();

    placeResponse = PlaceResponse.builder()
        .id(1L)
        .build();

    sameNamePlaceRequest = PlaceRequest.builder()
        .lat(13.7749)
        .lng(-52.4194)
        .placeName("Old Name")
        .placeAddress("456 Main St")
        .build();

    newNamePlaceRequest = PlaceRequest.builder()
        .lat(13.7749)
        .lng(-52.4194)
        .placeName("New Name")
        .placeAddress("456 Main St")
        .build();

    existingPlace = Place.builder()
        .id(2L)
        .lat(13.7749)
        .lng(-52.4194)
        .placeName("Old Name")
        .placeAddress("456 Main St")
        .build();

    newNamePlaceResponse = PlaceResponse.builder()
        .id(2L)
        .build();
  }

  @Test
  @DisplayName("유효한 장소 저장")
  void postPlace_ShouldSavePlaceSuccessfully() {
    // given
    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));
    given(placeRepository.save(any(Place.class))).willReturn(place);

    // when
    PlaceCreateResponse result = placeService.postPlace(1L, placeRequest);

    // then
    verify(placeRepository).save(any(Place.class));
    assertEquals(placeResponse.getId(), result.getId());
  }

  @Test
  @DisplayName("유효한 장소 저장 > 기존에 존재하는 위/경도 값이지만 이름이 다를 때")
  void postPlace_ShouldSavePlaceSuccessfully_ByDifferentName() {

    // given
    given(placeRepository.findPlaceByLatAndLng(newNamePlaceRequest.getLat(), newNamePlaceRequest.getLng()))
        .willReturn(Optional.of(existingPlace));
    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));

    // when
    PlaceCreateResponse result = placeService.postPlace(1L, newNamePlaceRequest);

    // then
    assertEquals(existingPlace.getPlaceName(), "New Name");
    assertNotEquals(existingPlace.getPlaceName(), "Old Name");
  }

  @Test
  @DisplayName("유효한 장소 저장 > 기존에 존재하는 위/경도 값이지만 이름이 같을 때")
  void postPlace_ShouldSavePlaceSuccessfully_BySameName() {
    // given
    given(placeRepository.findPlaceByLatAndLng(newNamePlaceRequest.getLat(), newNamePlaceRequest.getLng()))
        .willReturn(Optional.of(existingPlace));
    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));

    // when
    PlaceCreateResponse result = placeService.postPlace(1L, sameNamePlaceRequest);

    // then
    assertEquals(existingPlace.getPlaceName(), "Old Name");
  }


  @Test
  @DisplayName("장소 조회 성공 테스트")
  void getPlace_ShouldReturnPlaceResponse() {
    // given
    given(placeRepository.findById(1L)).willReturn(Optional.of(place));
    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));

    // when
    PlaceResponse result = placeService.getPlace(1L, 1L);

    // then
    assertEquals(place.getId(), result.getId());
    assertEquals(place.getLat(), result.getLat());
    assertEquals(place.getLng(), result.getLng());
    assertEquals(place.getPlaceName(), result.getPlaceName());
    assertEquals(place.getPlaceAddress(), result.getPlaceAddress());
  }

  @Test
  @DisplayName("장소 조회 실패 테스트")
  void getPlace_ShouldThrowException_WhenPlaceNotFound() {
    // given
    given(placeRepository.findById(anyLong())).willReturn(Optional.empty());
    given(studyChannelRepository.findById(1L)).willReturn(Optional.of(studyChannel));

    // when & then
    NotFoundPlaceException exception = assertThrows(NotFoundPlaceException.class, () -> {
      placeService.getPlace(1L, 1L);
    });

    assertEquals( "존재하지 않는 장소입니다.", exception.getMessage());
  }
}