package com.mrtob.srs.service;

import com.mrtob.srs.algorithm.SpacedRepetitionAlgorithm;
import com.mrtob.srs.entity.Card;
import com.mrtob.srs.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private CardRepository repo;

    @Mock
    private SpacedRepetitionAlgorithm algorithm;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void review_delegatesToAlgorithmAndSaves() {
        UUID id = UUID.randomUUID();
        Card card = Card.builder()
                .id(id)
                .front("Q")
                .back("A")
                .nextReview(Instant.now())
                .build();
        Card reviewed = Card.builder()
                .id(id)
                .front("Q")
                .back("A")
                .nextReview(Instant.now().plusSeconds(86400))
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(card));
        when(algorithm.review(card, 4)).thenReturn(reviewed);
        when(repo.save(reviewed)).thenReturn(reviewed);

        Card result = reviewService.review(id, 4);

        assertThat(result).isEqualTo(reviewed);
        verify(algorithm).review(card, 4);
        verify(repo).save(reviewed);
    }

    @Test
    void review_throwsWhenCardNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.review(id, 4))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Card not found");

        verify(algorithm, never()).review(any(), anyInt());
        verify(repo, never()).save(any());
    }
}
