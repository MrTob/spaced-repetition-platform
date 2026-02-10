package com.mrtob.srs.service;

import com.mrtob.srs.algorithm.SpacedRepetitionAlgorithm;
import com.mrtob.srs.entity.Card;
import com.mrtob.srs.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CardRepository repo;
    private final SpacedRepetitionAlgorithm algorithm;

    public Card review(UUID cardId, int quality) {
        Card card = repo.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Card not found: " + cardId));

        Card updated = algorithm.review(card, quality);

        return repo.save(updated);
    }
}
