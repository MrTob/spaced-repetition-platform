package com.mrtob.srs.config;

import com.mrtob.srs.entity.Card;
import com.mrtob.srs.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final CardRepository repo;
    private final Environment env;

    @Override
    public void run(String... args) {
        if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
            log.debug("Prod profile active — skipping dev seed");
            return;
        }

        if (repo.count() > 0) {
            log.info("Dev data already present — skipping seed");
            return;
        }

        List<Card> cards = List.of(
                Card.builder()
                        .front("What is spaced repetition?")
                        .back("A learning technique that reviews material at increasing intervals to optimize long-term retention.")
                        .nextReview(Instant.now())
                        .build(),
                Card.builder()
                        .front("Who created the SM-2 algorithm?")
                        .back("Piotr Wozniak, as part of the SuperMemo project in 1987.")
                        .nextReview(Instant.now())
                        .build(),
                Card.builder()
                        .front("What does the Easiness Factor represent in SM-2?")
                        .back("A multiplier (minimum 1.3) that controls how fast the review interval grows. Higher = easier card.")
                        .nextReview(Instant.now())
                        .build(),
                Card.builder()
                        .front("What does FSRS stand for?")
                        .back("Free Spaced Repetition Scheduler — a modern, open-source algorithm based on a power-law forgetting curve.")
                        .nextReview(Instant.now())
                        .build(),
                Card.builder()
                        .front("What is 'stability' in FSRS?")
                        .back("The time (in days) at which the probability of recall drops to 90%. Higher stability = longer retention.")
                        .nextReview(Instant.now())
                        .build(),
                Card.builder()
                        .front("What is the forgetting curve?")
                        .back("A model showing how memory retention decays over time. FSRS uses R(t) = (1 + t/(9S))^(-0.5).")
                        .nextReview(Instant.now().minus(1, ChronoUnit.DAYS))
                        .build(),
                Card.builder()
                        .front("What quality score means 'perfect response' in SM-2?")
                        .back("5 — the response was perfect with no hesitation.")
                        .nextReview(Instant.now().minus(2, ChronoUnit.DAYS))
                        .build()
        );

        repo.saveAll(cards);
        log.info("Seeded {} demo cards", cards.size());
    }
}
