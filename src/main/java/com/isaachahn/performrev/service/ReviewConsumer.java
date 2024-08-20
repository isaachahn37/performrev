package com.isaachahn.performrev.service;

import com.isaachahn.performrev.model.domain.Review;
import com.isaachahn.performrev.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewConsumer {
    private final ReviewRepository reviewRepository;

    @KafkaListener(topics = "reviews", groupId = "reviews-consumer-group")
    public void consume(Review review) {
        reviewRepository.save(review);
    }
}
