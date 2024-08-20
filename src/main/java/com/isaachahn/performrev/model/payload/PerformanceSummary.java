package com.isaachahn.performrev.model.payload;

import com.isaachahn.performrev.model.domain.Review;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class PerformanceSummary {
    private String departmentId;
    private double averageScore;
    private List<EmployeeRank> topPerformance;
    private List<EmployeeRank> lowPerformance;
}
