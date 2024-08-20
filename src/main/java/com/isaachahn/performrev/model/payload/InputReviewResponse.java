package com.isaachahn.performrev.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InputReviewResponse {
    private String reviewId;
    private String status;
}
