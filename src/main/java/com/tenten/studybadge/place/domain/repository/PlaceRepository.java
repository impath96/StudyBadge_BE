package com.tenten.studybadge.place.domain.repository;

import com.tenten.studybadge.place.domain.entity.Place;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
  Optional<Place> findPlaceByLatAndLng(double lat, double lng);
}
