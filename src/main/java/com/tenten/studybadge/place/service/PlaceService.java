package com.tenten.studybadge.place.service;


import com.tenten.studybadge.common.exception.place.NotFoundPlaceException;
import com.tenten.studybadge.common.exception.studychannel.NotFoundStudyChannelException;
import com.tenten.studybadge.place.domain.entity.Place;
import com.tenten.studybadge.place.domain.repository.PlaceRepository;
import com.tenten.studybadge.place.dto.PlaceCreateResponse;
import com.tenten.studybadge.place.dto.PlaceRequest;
import com.tenten.studybadge.place.dto.PlaceResponse;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {
  private final PlaceRepository placeRepository;
  private final StudyChannelRepository studyChannelRepository;

  public PlaceCreateResponse postPlace(Long studyChannelId, PlaceRequest placeRequest) {
    studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

    Optional<Place> placeByStudyChannelIdAndXAndY = placeRepository.findPlaceByLatAndLng(
        placeRequest.getLat(), placeRequest.getLng());
    if (placeByStudyChannelIdAndXAndY.isPresent()) {
      Place place = placeByStudyChannelIdAndXAndY.get();
      if (!place.getPlaceName().equals(placeRequest.getPlaceName())) {
        place.setPlaceName(placeRequest.getPlaceName());
      }
      return new PlaceCreateResponse(place.getId());
    }

    return new PlaceCreateResponse(placeRepository.save(placeRequest.toEntity()).getId());
  }

  public PlaceResponse getPlace(Long studyChannelId, Long placeId) {
    studyChannelRepository.findById(studyChannelId)
        .orElseThrow(NotFoundStudyChannelException::new);

    Place place = placeRepository.findById(placeId)
        .orElseThrow(NotFoundPlaceException::new);

    return PlaceResponse.builder()
        .id(place.getId())
        .lat(place.getLat())
        .lng(place.getLng())
        .placeName(place.getPlaceName())
        .placeAddress(place.getPlaceAddress())
        .build();
  }
}
