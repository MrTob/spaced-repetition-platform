package com.mrtob.srs.config;

import com.mrtob.srs.entity.Card;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.UUID;

@Configuration
@ImportRuntimeHints(NativeHints.Registrar.class)
public class NativeHints {

    static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register entity array types for Hibernate
            hints.reflection().registerType(Card[].class, MemberCategory.values());
            hints.reflection().registerType(Card.class, MemberCategory.values());

            hints.reflection().registerType(UUID.class, MemberCategory.values());
            hints.reflection().registerType(UUID[].class, MemberCategory.values());

            hints.reflection().registerType(java.time.Instant.class, MemberCategory.values());
        }
    }
}