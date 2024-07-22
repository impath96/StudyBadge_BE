package com.tenten.studybadge.point.domain.service;

import com.tenten.studybadge.common.exception.member.NotFoundMemberException;
import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.point.domain.repository.PointRepository;
import com.tenten.studybadge.point.dto.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    public List<PointResponse> getMyPointHistory(Long memberId, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        List<Point> point = pointRepository.findByMemberId(memberId, pageRequest);
        if (point == null || point.isEmpty())
            throw new NotFoundMemberException();

        return PointResponse.listToResponse(point);
    }

    public void usePoint() {


    }
}
