package com.mrtob.srs.algorithm;

import com.mrtob.srs.entity.Card;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * SuperMemo 2 algorithm by Piotr Wozniak.
 * <p>
 * Quality scores: 0-5 where 0-2 = failure, 3-5 = success.
 * On success the review interval grows exponentially via the easiness factor.
 * On failure the card resets to the beginning of the learning phase.
 */
public class SM2Algorithm implements SpacedRepetitionAlgorithm {

    @Override
    public Card review(Card card, int quality) {
        quality = Math.clamp(quality, 0, 5);

        double ef = card.getEasinessFactor();
        int repetitions = card.getRepetitions();
        int interval = card.getIntervalDays();

        // Update easiness factor (always, regardless of pass/fail)
        ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        ef = Math.max(1.3, ef);

        if (quality >= 3) {
            // Correct response — advance through the schedule
            switch (repetitions) {
                case 0 -> interval = 1;
                case 1 -> interval = 6;
                default -> interval = (int) Math.round(interval * ef);
            }
            repetitions++;
        } else {
            // Incorrect response — restart the learning phase
            repetitions = 0;
            interval = 1;
        }

        card.setEasinessFactor(ef);
        card.setRepetitions(repetitions);
        card.setIntervalDays(interval);
        card.setNextReview(Instant.now().plus(interval, ChronoUnit.DAYS));

        return card;
    }
}
