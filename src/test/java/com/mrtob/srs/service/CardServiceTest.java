package com.mrtob.srs.service;

import com.mrtob.srs.dto.CardCreateRequest;
import com.mrtob.srs.dto.CardUpdateRequest;
import com.mrtob.srs.entity.Card;
import com.mrtob.srs.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void findAll_returnsPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> expected = new PageImpl<>(List.of(buildCard()));
        when(cardRepository.findAll(pageable)).thenReturn(expected);

        Page<Card> result = cardService.findAll(pageable);

        assertThat(result).isEqualTo(expected);
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void search_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> expected = new PageImpl<>(List.of(buildCard()));
        when(cardRepository.search("java", pageable)).thenReturn(expected);

        Page<Card> result = cardService.search("java", pageable);

        assertThat(result).isEqualTo(expected);
        verify(cardRepository).search("java", pageable);
    }

    @Test
    void create_savesCardWithRequestFieldsAndNowReview() {
        CardCreateRequest request = new CardCreateRequest("Q", "A");
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant before = Instant.now();
        Card result = cardService.create(request);
        Instant after = Instant.now();

        assertThat(result.getFront()).isEqualTo("Q");
        assertThat(result.getBack()).isEqualTo("A");
        assertThat(result.getNextReview()).isBetween(before, after);

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        assertThat(captor.getValue().getEasinessFactor()).isEqualTo(2.5);
    }

    @Test
    void update_updatesExistingCard() {
        UUID id = UUID.randomUUID();
        Card existing = buildCard();
        existing.setId(id);
        when(cardRepository.findById(id)).thenReturn(Optional.of(existing));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardUpdateRequest request = new CardUpdateRequest("New Q", "New A");
        Card result = cardService.update(id, request);

        assertThat(result.getFront()).isEqualTo("New Q");
        assertThat(result.getBack()).isEqualTo("New A");
        verify(cardRepository).save(existing);
    }

    @Test
    void update_throwsWhenCardNotFound() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.update(id, new CardUpdateRequest("Q", "A")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void delete_deletesExistingCard() {
        UUID id = UUID.randomUUID();
        when(cardRepository.existsById(id)).thenReturn(true);

        cardService.delete(id);

        verify(cardRepository).deleteById(id);
    }

    @Test
    void delete_throwsWhenCardNotFound() {
        UUID id = UUID.randomUUID();
        when(cardRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> cardService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(cardRepository, never()).deleteById(any());
    }

    @Test
    void findDueCards_returnsCardsDueBeforeNow() {
        List<Card> expected = List.of(buildCard());
        when(cardRepository.findByNextReviewBefore(any(Instant.class))).thenReturn(expected);

        List<Card> result = cardService.findDueCards();

        assertThat(result).isEqualTo(expected);
        verify(cardRepository).findByNextReviewBefore(any(Instant.class));
    }

    private Card buildCard() {
        return Card.builder()
                .id(UUID.randomUUID())
                .front("What is Java?")
                .back("A programming language")
                .nextReview(Instant.now())
                .build();
    }
}
