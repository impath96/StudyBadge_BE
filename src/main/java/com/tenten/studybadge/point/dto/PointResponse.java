package com.tenten.studybadge.point.dto;

import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.type.point.PointHistoryType;
import com.tenten.studybadge.type.point.TransferType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointResponse {

    private PointHistoryType historyType;
    private TransferType transferType;
    private Long amount;
    private LocalDateTime createdAt;


    public static List<PointResponse> listToResponse(List<Point> points) {

        return points.stream().map(PointResponse::toResponse).collect(Collectors.toList());
    }

    public static PointResponse toResponse(Point point) {

        return PointResponse.builder()
                .historyType(point.getHistoryType())
                .transferType(point.getTransferType())
                .amount(point.getAmount())
                .createdAt(point.getCreatedAt())
                .build();
    }
}
