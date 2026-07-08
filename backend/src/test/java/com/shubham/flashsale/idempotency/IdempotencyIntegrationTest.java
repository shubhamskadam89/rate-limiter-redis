package com.shubham.flashsale.idempotency;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.shubham.flashsale.flashsale.order.service.PurchaseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class IdempotencyIntegrationTest {

  private static final String SALE_UUID = "11111111-1111-1111-1111-111111111111";
  private static final String SALE_ITEM_UUID = "22222222-2222-2222-2222-222222222222";

  private static final String PURCHASE_URL =
      "/api/v1/sales/" + SALE_UUID + "/items/" + SALE_ITEM_UUID + "/purchase";

  private static final String USER_A_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
  private static final String USER_B_UUID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

  @Autowired private MockMvc mockMvc;

  @Autowired private StringRedisTemplate redisTemplate;

  @MockitoBean private PurchaseService purchaseService;

  @AfterEach
  void cleanRedis() {
    redisTemplate.delete("idem:" + USER_A_UUID + ":purchase:test-key");
    redisTemplate.delete("idem:" + USER_A_UUID + ":purchase:failure-key");
    redisTemplate.delete("idem:" + USER_B_UUID + ":purchase:test-key");
  }

  @Test
  void shouldReplayCompletedResponseForSameUserAndSameIdempotencyKey() throws Exception {
    when(purchaseService.purchase(anyString(), anyString(), anyString(), any())).thenReturn(null);

    performPurchase(USER_A_UUID, "test-key").andExpect(status().isOk());

    performPurchase(USER_A_UUID, "test-key").andExpect(status().isOk());

    verify(purchaseService, times(1)).purchase(anyString(), anyString(), eq("test-key"), any());
  }

  @Test
  void shouldReleaseProcessingKeyAfterServerErrorSoRequestCanBeRetried() throws Exception {
    when(purchaseService.purchase(anyString(), anyString(), anyString(), any()))
        .thenThrow(new RuntimeException("boom"))
        .thenReturn(null);

    performPurchase(USER_A_UUID, "failure-key").andExpect(status().isInternalServerError());

    performPurchase(USER_A_UUID, "failure-key").andExpect(status().isOk());

    verify(purchaseService, times(2)).purchase(anyString(), anyString(), eq("failure-key"), any());
  }

  @Test
  void shouldIsolateSameIdempotencyKeyAcrossDifferentUsers() throws Exception {
    when(purchaseService.purchase(anyString(), anyString(), anyString(), any())).thenReturn(null);

    performPurchase(USER_A_UUID, "test-key").andExpect(status().isOk());

    performPurchase(USER_B_UUID, "test-key").andExpect(status().isOk());

    verify(purchaseService, times(2)).purchase(anyString(), anyString(), eq("test-key"), any());
  }

  @Test
  void shouldRejectPurchaseWithoutIdempotencyKey() throws Exception {
    mockMvc
        .perform(
            post(PURCHASE_URL)
                .with(jwt().jwt(jwt -> jwt.subject(USER_A_UUID).claim("role", "USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":1}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(purchaseService);
  }

  private org.springframework.test.web.servlet.ResultActions performPurchase(
      String userUuid, String idempotencyKey) throws Exception {
    return mockMvc.perform(
        post(PURCHASE_URL)
            .with(jwt().jwt(jwt -> jwt.subject(userUuid).claim("role", "USER")))
            .header("X-Idempotency-Key", idempotencyKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"quantity\":1}"));
  }
}
