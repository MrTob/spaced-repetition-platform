package com.mrtob.srs.controller;

import com.mrtob.srs.dto.*;
import com.mrtob.srs.entity.Card;
import com.mrtob.srs.service.CardService;
import com.mrtob.srs.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final ReviewService reviewService;
    private final CardMapper cardMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@Valid @RequestBody CardCreateRequest request) {
        Card card = cardService.create(request);
        return cardMapper.toResponse(card);
    }

    @GetMapping
    public PageResponse<CardResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Card> cardPage = (search != null && !search.isBlank())
                ? cardService.search(search.trim(), pageable)
                : cardService.findAll(pageable);

        return PageResponse.from(cardPage.map(cardMapper::toResponse));
    }

    @GetMapping("/due")
    public List<CardResponse> due() {
        return cardMapper.toResponseList(cardService.findDueCards());
    }

    @PutMapping("/{id}")
    public CardResponse update(@PathVariable UUID id,
                               @Valid @RequestBody CardUpdateRequest request) {
        Card card = cardService.update(id, request);
        return cardMapper.toResponse(card);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        cardService.delete(id);
    }

    @PostMapping("/{id}/review")
    public CardResponse review(@PathVariable UUID id,
                               @RequestParam int quality) {
        Card card = reviewService.review(id, quality);
        return cardMapper.toResponse(card);
    }
}
