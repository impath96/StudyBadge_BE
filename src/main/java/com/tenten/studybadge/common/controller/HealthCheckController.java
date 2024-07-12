package com.tenten.studybadge.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/health-check")
  public String healthcheck() {
    return "OK";
  }
}

