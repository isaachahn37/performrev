package com.isaachahn.performrev.service;

import com.isaachahn.performrev.model.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewProducer {
    private static final String TOPIC = "reviews";
    private final KafkaTemplate<String, Review> kafkaTemplate;

    public void sendReview(Review review) {
        kafkaTemplate.send(TOPIC, review);
    }
}
