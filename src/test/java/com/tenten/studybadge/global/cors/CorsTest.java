package com.tenten.studybadge.global.cors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tenten.studybadge.common.config.CorsConfig;
import com.tenten.studybadge.common.controller.HealthCheckController;
import com.tenten.studybadge.common.jwt.JwtTokenProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class CorsTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private JwtTokenProvider jwtTokenProvider;

  @Test
  @DisplayName("Preflight request")
  void testPreflightRequest() throws Exception {
    mockMvc.perform(options("/health-check")
            .header("Origin", "https://study-badge.vercel.app")
            .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "https://study-badge.vercel.app"))
        .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE"))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
  }
}