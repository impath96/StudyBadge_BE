package com.tenten.studybadge.point.dto;

import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.type.point.PointHistoryType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointResponse {

    private PointHistoryType historyType;
    private Long amount;
    private LocalDateTime createdAt;


    public static List<PointResponse> listToResponse(List<Point> points) {

        return points.stream().map(PointResponse::toResponse).collect(Collectors.toList());
    }

    public static PointResponse toResponse(Point point) {

        return PointResponse.builder()
                .historyType(point.getHistoryType())
                .amount(point.getAmount())
                .createdAt(point.getCreatedAt())
                .build();
    }
}
