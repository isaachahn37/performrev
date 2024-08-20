package com.isaachahn.performrev.repository;

import com.isaachahn.performrev.model.domain.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewRepository extends MongoRepository<Review, String> {
}
