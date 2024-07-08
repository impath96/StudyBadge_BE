package com.tenten.studybadge.place.service;


import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.common.exception.place.NotFoundPlaceException;
import com.tenten.studybadge.place.domain.entity.Place;
import com.tenten.studybadge.place.domain.repository.PlaceRepository;
import com.tenten.studybadge.place.dto.PlaceRequest;
import com.tenten.studybadge.place.dto.PlaceResponse;
import com.tenten.studybadge.study.channel.domain.repository.StudyChannelRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {
  private final PlaceRepository placeRepository;
  private final StudyChannelRepository studyChannelRepository;

  public PlaceResponse postPlace(Long studyChannelId, PlaceRequest placeRequest) {

    // TODO studyChannel Exception 사용 예정
    studyChannelRepository.findById(studyChannelId).orElseThrow(
        (() -> new EntityNotFoundException(
            "StudyChannel with id " + studyChannelId + " not found")));

    Optional<Place> placeByStudyChannelIdAndXAndY = placeRepository.findPlaceByLatAndLng(
        placeRequest.getLat(), placeRequest.getLng());
    if (placeByStudyChannelIdAndXAndY.isPresent()) {
      Place place = placeByStudyChannelIdAndXAndY.get();
      if (!place.getPlaceName().equals(placeRequest.getPlaceName())) {
        place.setPlaceName(placeRequest.getPlaceName());
      }
      return new PlaceResponse(place.getId());
    }

    return new PlaceResponse(placeRepository.save(placeRequest.toEntity()).getId());
  }

  public PlaceResponse getPlace(Long studyChannelId, Long placeId) {
    // TODO studyChannel Exception 사용 예정
    studyChannelRepository.findById(studyChannelId).orElseThrow(
        (() -> new EntityNotFoundException(
            "StudyChannel with id " + studyChannelId + " not found")));

    Place place = placeRepository.findById(placeId)
        .orElseThrow(NotFoundPlaceException::new);

    return new PlaceResponse(place.getId());
  }
}
