package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.dto.CoPurchaseResponseV1;
import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoPurchaseControllerV1.class)
class CoPurchaseControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoPurchaseService coPurchaseService;

    @Test
    @DisplayName("GET /api/v1/co-purchase/{productId} should return recommendations")
    @WithMockUser
    void getRecommendationsV1_shouldReturnRecommendations() throws Exception {
        // Given
        Integer productId = 1;
        CoPurchaseResponseV1 response = new CoPurchaseResponseV1(2);
        when(coPurchaseService.getRecommendationsV1(productId))
                .thenReturn(Collections.singletonList(response));

        // When & Then
        mockMvc.perform(get("/api/v1/co-purchase/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/co-purchase/{productId} should return unauthorized without authentication")
    void getRecommendationsV1_shouldReturnUnauthorized() throws Exception {
        // Given
        Integer productId = 1;

        // When & Then
        mockMvc.perform(get("/api/v1/co-purchase/{productId}", productId))
                .andExpect(status().isUnauthorized());
    }
}
