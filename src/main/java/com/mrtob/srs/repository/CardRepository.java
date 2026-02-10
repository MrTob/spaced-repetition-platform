package com.mrtob.srs.repository;

import com.mrtob.srs.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByNextReviewBefore(Instant now);

    @Query("""
    SELECT c FROM Card c
    WHERE LOWER(c.front) LIKE LOWER(CONCAT('%', :term, '%'))
       OR LOWER(c.back) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    Page<Card> search(@Param("term") String term, Pageable pageable);
}
