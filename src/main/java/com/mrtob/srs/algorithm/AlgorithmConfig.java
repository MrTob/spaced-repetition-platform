package com.mrtob.srs.algorithm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlgorithmConfig {

    @Bean
    @ConditionalOnProperty(name = "srs.algorithm", havingValue = "sm2")
    public SpacedRepetitionAlgorithm sm2Algorithm() {
        return new SM2Algorithm();
    }

    @Bean
    @ConditionalOnProperty(name = "srs.algorithm", havingValue = "fsrs", matchIfMissing = true)
    public SpacedRepetitionAlgorithm fsrsAlgorithm() {
        return new FSRSAlgorithm();
    }
}
