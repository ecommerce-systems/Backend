package com.creepereye.ecommerce.domain.copurchase.controller;

import com.creepereye.ecommerce.domain.copurchase.service.CoPurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoPurchaseController.class)
class CoPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoPurchaseService coPurchaseService;

    @Test
    @WithMockUser
    void getRecommendations() throws Exception {
        // given
        when(coPurchaseService.getRecommendations(1)).thenReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/v1/co-purchase/1"))
                .andExpect(status().isOk());
    }
}