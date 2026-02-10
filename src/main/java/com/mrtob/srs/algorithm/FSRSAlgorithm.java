package com.mrtob.srs.algorithm;

import com.mrtob.srs.entity.Card;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Free Spaced Repetition Scheduler (FSRS) v4.
 * <p>
 * Models memory with two parameters:
 * <ul>
 *   <li><b>Stability (S)</b> — the interval in days at which recall probability is 90%</li>
 *   <li><b>Difficulty (D)</b> — how hard the card is to learn (range 1-10)</li>
 * </ul>
 * <p>
 * Uses the power-law forgetting curve: R(t) = (1 + FACTOR * t/S)^DECAY
 */
public class FSRSAlgorithm implements SpacedRepetitionAlgorithm {

    private static final double DECAY = -0.5;
    private static final double FACTOR = 19.0 / 81.0;
    private static final double DESIRED_RETENTION = 0.9;

    // Learning phase intervals in minutes for "Again" rating
    private static final int[] LEARNING_STEPS_MINUTES = {1, 10};

    // Default FSRS v4 weights
    private static final double[] W = {
            0.4, 0.6, 2.4, 5.8,  // w0-w3: initial stability per rating (Again, Hard, Good, Easy)
            4.93,                  // w4: initial difficulty base
            0.94,                  // w5: initial difficulty scaling
            0.86,                  // w6: difficulty update scaling
            0.01,                  // w7: difficulty mean reversion weight
            1.49,                  // w8: stability increase base
            0.14,                  // w9: stability power factor
            0.94,                  // w10: retrievability factor
            2.18,                  // w11: fail stability base
            0.05,                  // w12: fail difficulty factor
            0.34,                  // w13: fail stability power
            1.26,                  // w14: fail retrievability factor
    };

    @Override
    public Card review(Card card, int quality) {
        int rating = mapQualityToRating(quality);

        double stability = card.getStability();
        double difficulty = card.getDifficulty();
        int learningStep = card.getLearningStep();

        if (stability == 0) {
            // First review — initialize parameters
            stability = initialStability(rating);
            difficulty = initialDifficulty(rating);
        } else {
            // Subsequent review — update parameters
            double elapsedDays = elapsedDaysSinceLastReview(card);
            double retrievability = retrievability(elapsedDays, stability);

            difficulty = nextDifficulty(difficulty, rating);
            stability = (rating == 1)
                    ? failStability(stability, difficulty, retrievability)
                    : successStability(stability, difficulty, retrievability);
        }

        // Handle learning phase and calculate next review
        if (rating == 1) {
            // Again: enter or stay in learning phase
            if (learningStep < 0) {
                // Was in review phase, reset to first learning step
                learningStep = 0;
            }
            // Use short learning interval (minutes)
            int minutes = LEARNING_STEPS_MINUTES[Math.min(learningStep, LEARNING_STEPS_MINUTES.length - 1)];
            card.setNextReview(Instant.now().plus(minutes, ChronoUnit.MINUTES));
            card.setLearningStep(learningStep);
        } else if (rating == 2 && learningStep >= 0) {
            // Hard during learning: repeat current step
            int minutes = LEARNING_STEPS_MINUTES[Math.min(learningStep, LEARNING_STEPS_MINUTES.length - 1)];
            card.setNextReview(Instant.now().plus(minutes, ChronoUnit.MINUTES));
            card.setLearningStep(learningStep);
        } else if (rating == 4) {
            // Easy: always graduate immediately to review phase
            learningStep = -1;
            long intervalDays = Math.max(1, Math.round(nextInterval(stability)));
            card.setNextReview(Instant.now().plus(intervalDays, ChronoUnit.DAYS));
            card.setLearningStep(learningStep);
        } else if (learningStep >= 0 && learningStep < LEARNING_STEPS_MINUTES.length - 1) {
            // Good during learning: advance to next step
            learningStep++;
            int minutes = LEARNING_STEPS_MINUTES[learningStep];
            card.setNextReview(Instant.now().plus(minutes, ChronoUnit.MINUTES));
            card.setLearningStep(learningStep);
        } else {
            // Graduate to review phase (Good at last step, or already in review phase)
            learningStep = -1;
            long intervalDays = Math.max(1, Math.round(nextInterval(stability)));
            card.setNextReview(Instant.now().plus(intervalDays, ChronoUnit.DAYS));
            card.setLearningStep(learningStep);
        }

        card.setStability(stability);
        card.setDifficulty(difficulty);

        return card;
    }

    /**
     * Maps a 0-5 quality score (SM-2 style) to FSRS ratings 1-4.
     */
    private int mapQualityToRating(int quality) {
        quality = Math.clamp(quality, 0, 5);
        if (quality <= 1) return 1; // Again
        if (quality == 2) return 2; // Hard
        if (quality == 3) return 3; // Good
        return 4;                   // Easy
    }

    private double initialStability(int rating) {
        return W[rating - 1];
    }

    private double initialDifficulty(int rating) {
        return clampDifficulty(W[4] - Math.exp(W[5] * (rating - 1)) + 1);
    }

    private double nextDifficulty(double d, int rating) {
        double meanReversionTarget = initialDifficulty(4);
        double newD = W[7] * meanReversionTarget + (1 - W[7]) * (d - W[6] * (rating - 3));
        return clampDifficulty(newD);
    }

    private double retrievability(double elapsedDays, double stability) {
        return Math.pow(1 + FACTOR * elapsedDays / stability, DECAY);
    }

    private double successStability(double s, double d, double r) {
        return s * (Math.exp(W[8]) * (11 - d) * Math.pow(s, -W[9])
                * (Math.exp(W[10] * (1 - r)) - 1) + 1);
    }

    private double failStability(double s, double d, double r) {
        return W[11] * Math.pow(d, -W[12]) * (Math.pow(s + 1, W[13]) - 1)
                * Math.exp(W[14] * (1 - r));
    }

    /**
     * Calculates the interval (in days) for the desired retention rate.
     * For 90% retention, this simplifies to approximately S (stability) days.
     */
    private double nextInterval(double stability) {
        return (stability / FACTOR) * (Math.pow(DESIRED_RETENTION, 1.0 / DECAY) - 1);
    }

    private double elapsedDaysSinceLastReview(Card card) {
        Duration elapsed = Duration.between(card.getNextReview(), Instant.now());
        return Math.max(0, elapsed.toHours() / 24.0);
    }

    private double clampDifficulty(double d) {
        return Math.clamp(d, 1.0, 10.0);
    }
}
