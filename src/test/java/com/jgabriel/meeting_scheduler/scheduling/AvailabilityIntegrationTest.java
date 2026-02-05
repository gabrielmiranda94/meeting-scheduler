package com.jgabriel.meeting_scheduler.scheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgabriel.meeting_scheduler.AbstractIntegrationTest;
import com.jgabriel.meeting_scheduler.scheduling.dto.AvailabilityRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.ReservationRequest;
import com.jgabriel.meeting_scheduler.scheduling.dto.TimeBlockResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat; // Importante para asserções melhores
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AvailabilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_URL = "/api/v1/availability";

    @Test
    @DisplayName("should prevent double booking using optimistic locking")
    void testConcurrency_optimisticLocking() throws Exception {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var end = start.plusHours(1);

        var availabilityPayload = new AvailabilityRequest(start, end);

        MvcResult result = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityPayload)))
                .andExpect(status().isCreated())
                .andReturn();

        var createdBlock = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TimeBlockResponse.class
        );

        Long blockId = createdBlock.id();
        Long initialVersion = createdBlock.version(); // <--- O Pulo do Gato Sênior

        var user1Request = new ReservationRequest(101L, initialVersion);
        var user2Request = new ReservationRequest(102L, initialVersion);

        String jsonUser1 = objectMapper.writeValueAsString(user1Request);
        String jsonUser2 = objectMapper.writeValueAsString(user2Request);

        CompletableFuture<Integer> user1Future = CompletableFuture.supplyAsync(() ->
                doReserve(blockId, jsonUser1));

        CompletableFuture<Integer> user2Future = CompletableFuture.supplyAsync(() ->
                doReserve(blockId, jsonUser2));

        CompletableFuture.allOf(user1Future, user2Future).join();
        int statusUser1 = user1Future.get();
        int statusUser2 = user2Future.get();

        assertThat(statusUser1).isNotEqualTo(statusUser2); // Não podem ser iguais

        boolean oneSucceeded = (statusUser1 == 200 || statusUser2 == 200);
        boolean oneConflict = (statusUser1 == 409 || statusUser2 == 409);

        assertThat(oneSucceeded).as("Pelo menos um usuário deve conseguir reservar").isTrue();
        assertThat(oneConflict).as("O outro usuário deve receber conflito (409)").isTrue();
    }

    private int doReserve(Long blockId, String json) {
        try {
            return mockMvc.perform(post(API_URL + "/" + blockId + "/reserve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }
}