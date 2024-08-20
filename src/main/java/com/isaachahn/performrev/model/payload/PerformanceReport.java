package com.isaachahn.performrev.model.payload;

import com.isaachahn.performrev.model.domain.Review;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PerformanceReport {
    private String employeeId;
    private double averageScore;
    private String departmentId;
    private List<Review> reviews;
    private Trends trends;
}
