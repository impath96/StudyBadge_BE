package com.tenten.studybadge.point.domain.repository;

import com.tenten.studybadge.point.domain.entity.Point;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByMemberId(Long memberId, PageRequest pageRequest);
}
