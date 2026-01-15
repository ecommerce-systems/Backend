package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.auth.service.RedisService;
import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseService;
import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import com.creepereye.ecommerce.global.security.provider.JwtTokenProvider;
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

@WebMvcTest(CoPurchaseControllerV2.class)
class CoPurchaseControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoPurchaseService coPurchaseService;

    @MockBean
    private RedisService redisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("V2 - 인증된 사용자는 추천 상품 상세 정보 목록을 조회할 수 있다")
    @WithMockUser
    void getRecommendationsV2_shouldReturnDetails() throws Exception {
        Integer productId = 1;
        ProductSearch response = ProductSearch.builder()
                .productId(2)
                .prodName("Recommended Product")
                .build();
        when(coPurchaseService.getRecommendationsV2(productId))
                .thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/v2/co-purchase/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].prodName").value("Recommended Product"));
    }

    @Test
    @DisplayName("V2 - 인증되지 않은 사용자는 401 Unauthorized를 반환한다")
    void getRecommendationsV2_shouldReturnUnauthorized() throws Exception {
        Integer productId = 1;

        mockMvc.perform(get("/api/v2/co-purchase/{productId}", productId))
                .andExpect(status().isUnauthorized());
    }
}
