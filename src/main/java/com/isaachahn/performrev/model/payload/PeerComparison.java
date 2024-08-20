package com.isaachahn.performrev.model.payload;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PeerComparison {
    private String employeeId;
    private String role;
    private String departmentId;
    private double percentileRank;
    private double averageScore;
    private double peerAverageScore;
}
