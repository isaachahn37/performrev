package com.isaachahn.performrev.model.domain;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String employeeId;
    private String reviewerId;
    private LocalDate reviewDate;
    private String comments;
    private double overallScore;
    private Metric metrics;
    private EmployeeInfo employeeInfo;
}
