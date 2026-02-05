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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AvailabilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TimeBlockRepository repository;

    private static final String API_URL = "/api/v1/availability";

    @Test
    @DisplayName("should paginate results when many slots exist")
    void testPagination() throws Exception {
        repository.deleteAll();
        var baseTime = LocalDateTime.now().plusDays(5).withHour(8).withMinute(0);

        for (int i = 0; i < 25; i++) {
            var block = TimeBlock.builder()
                    .startTime(baseTime.plusHours(i))
                    .endTime(baseTime.plusHours(i + 1))
                    .ownerId(1L)
                    .status(BlockStatus.AVAILABLE)
                    .build();

            repository.save(block);
        }

        mockMvc.perform(get(API_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "startTime,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.totalPages", is(3)));
    }

    @Test
    @DisplayName("should reserve and then cancel a slot")
    void testReservationAndCancellationLifecycle() throws Exception {
        var start = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        var createPayload = new AvailabilityRequest(start, start.plusHours(1), 99L);

        String responseJson = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var createdBlock = objectMapper.readValue(responseJson, TimeBlockResponse.class);

        var reservationPayload = new ReservationRequest(
                500L,
                createdBlock.version(),
                "Daily Meeting",
                "Discussing Project X",
                Set.of("dev@company.com", "pm@company.com")
        );

        mockMvc.perform(post(API_URL + "/" + createdBlock.id() + "/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESERVED")))
                .andExpect(jsonPath("$.title", is("Daily Meeting")))
                .andExpect(jsonPath("$.participants", hasSize(2)));

        mockMvc.perform(post(API_URL + "/" + createdBlock.id() + "/cancel")
                        .param("userId", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("AVAILABLE")))
                .andExpect(jsonPath("$.reservedBy").isEmpty());
    }

    @Test
    @DisplayName("should prevent double booking using optimistic locking")
    void testConcurrency_optimisticLocking() throws Exception {
        var start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var end = start.plusHours(1);
        var availabilityPayload = new AvailabilityRequest(start, end, 1L);

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
        Long initialVersion = createdBlock.version();

        var user1Request = new ReservationRequest(
                101L, initialVersion, "Meeting A", "Desc A", Set.of("a@test.com")
        );
        var user2Request = new ReservationRequest(
                102L, initialVersion, "Meeting B", "Desc B", Set.of("b@test.com")
        );

        String jsonUser1 = objectMapper.writeValueAsString(user1Request);
        String jsonUser2 = objectMapper.writeValueAsString(user2Request);

        CompletableFuture<Integer> user1Future = CompletableFuture.supplyAsync(() ->
                doReserve(blockId, jsonUser1));

        CompletableFuture<Integer> user2Future = CompletableFuture.supplyAsync(() ->
                doReserve(blockId, jsonUser2));

        CompletableFuture.allOf(user1Future, user2Future).join();

        int statusUser1 = user1Future.get();
        int statusUser2 = user2Future.get();

        boolean oneSucceeded = (statusUser1 == 200 || statusUser2 == 200);
        boolean oneConflict = (statusUser1 == 409 || statusUser2 == 409);

        assertThat(oneSucceeded).as("Pelo menos um deve conseguir").isTrue();
        assertThat(oneConflict).as("O outro deve receber conflito (409)").isTrue();
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
            return 500;
        }
    }
}