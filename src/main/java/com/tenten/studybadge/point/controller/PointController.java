package com.tenten.studybadge.point.controller;

import com.tenten.studybadge.common.security.LoginUser;
import com.tenten.studybadge.point.domain.service.PointService;
import com.tenten.studybadge.point.dto.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;


    @GetMapping("/my-point")
    public ResponseEntity<List<PointResponse>> getMyPointHistory(@LoginUser Long memberId,
                                                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                                                 @RequestParam(name = "size", defaultValue = "10") int size) {

        List<PointResponse> response = pointService.getMyPointHistory(memberId, page, size);

        return ResponseEntity.ok(response);
    }
}
