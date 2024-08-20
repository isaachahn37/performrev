package com.isaachahn.performrev.model.payload;
import com.isaachahn.performrev.model.domain.Metric;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InputReviewRequest {
    private String employeeId;
    private String reviewerId;
    private Metric metrics;
    private String comments;

    private LocalDate reviewDate;
}
