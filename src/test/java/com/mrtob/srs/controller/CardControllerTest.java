package com.mrtob.srs.controller;

import tools.jackson.databind.ObjectMapper;
import com.mrtob.srs.dto.CardCreateRequest;
import com.mrtob.srs.dto.CardMapper;
import com.mrtob.srs.dto.CardResponse;
import com.mrtob.srs.dto.CardUpdateRequest;
import com.mrtob.srs.entity.Card;
import com.mrtob.srs.service.CardService;
import com.mrtob.srs.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private CardMapper cardMapper;

    private final UUID cardId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void create_returns201WithCardResponse() throws Exception {
        Card card = buildCard();
        CardResponse response = buildResponse();

        when(cardService.create(any(CardCreateRequest.class))).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(response);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CardCreateRequest("Q", "A"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.front").value("Q"))
                .andExpect(jsonPath("$.back").value("A"));
    }

    @Test
    void create_returns400WhenFrontIsBlank() throws Exception {
        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CardCreateRequest("", "A"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_returnsPagedCards() throws Exception {
        Card card = buildCard();
        CardResponse response = buildResponse();
        Page<Card> page = new PageImpl<>(List.of(card));

        when(cardService.findAll(any(Pageable.class))).thenReturn(page);
        when(cardMapper.toResponse(card)).thenReturn(response);

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(cardId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void list_withSearch_delegatesToSearchMethod() throws Exception {
        Card card = buildCard();
        CardResponse response = buildResponse();
        Page<Card> page = new PageImpl<>(List.of(card));

        when(cardService.search(eq("java"), any(Pageable.class))).thenReturn(page);
        when(cardMapper.toResponse(card)).thenReturn(response);

        mockMvc.perform(get("/cards").param("search", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].front").value("Q"));
    }

    @Test
    void due_returnsDueCards() throws Exception {
        Card card = buildCard();
        CardResponse response = buildResponse();

        when(cardService.findDueCards()).thenReturn(List.of(card));
        when(cardMapper.toResponseList(List.of(card))).thenReturn(List.of(response));

        mockMvc.perform(get("/cards/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(cardId.toString()));
    }

    @Test
    void update_returnsUpdatedCard() throws Exception {
        Card card = buildCard();
        CardResponse response = new CardResponse(cardId, "New Q", "New A", now, now);

        when(cardService.update(eq(cardId), any(CardUpdateRequest.class))).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(response);

        mockMvc.perform(put("/cards/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CardUpdateRequest("New Q", "New A"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.front").value("New Q"))
                .andExpect(jsonPath("$.back").value("New A"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/cards/{id}", cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    void review_returnsReviewedCard() throws Exception {
        Card card = buildCard();
        CardResponse response = buildResponse();

        when(reviewService.review(cardId, 4)).thenReturn(card);
        when(cardMapper.toResponse(card)).thenReturn(response);

        mockMvc.perform(post("/cards/{id}/review", cardId).param("quality", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()));
    }

    private Card buildCard() {
        return Card.builder()
                .id(cardId)
                .front("Q")
                .back("A")
                .nextReview(now)
                .build();
    }

    private CardResponse buildResponse() {
        return new CardResponse(cardId, "Q", "A", now, now);
    }
}
