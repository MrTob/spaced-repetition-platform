package com.mrtob.srs.service;

import com.mrtob.srs.dto.CardCreateRequest;
import com.mrtob.srs.dto.CardUpdateRequest;
import com.mrtob.srs.entity.Card;
import com.mrtob.srs.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public Page<Card> findAll(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Page<Card> search(String term, Pageable pageable) {
        return cardRepository.search(term, pageable);
    }

    public Card create(CardCreateRequest request) {
        Card card = Card.builder()
                .front(request.front())
                .back(request.back())
                .nextReview(Instant.now())
                .build();
        return cardRepository.save(card);
    }

    public Card update(UUID id, CardUpdateRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found: " + id));
        card.setFront(request.front());
        card.setBack(request.back());
        return cardRepository.save(card);
    }

    public void delete(UUID id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found: " + id);
        }
        cardRepository.deleteById(id);
    }

    public List<Card> findDueCards() {
        return cardRepository.findByNextReviewBefore(Instant.now());
    }
}
