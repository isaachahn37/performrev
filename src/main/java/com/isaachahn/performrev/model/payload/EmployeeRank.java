package com.isaachahn.performrev.model.payload;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class EmployeeRank {
    private String employeeId;
    private double overallScore;
    private int rank;
}
