package com.isaachahn.performrev.restcontroller;

import com.isaachahn.performrev.model.domain.Metric;
import com.isaachahn.performrev.model.payload.InputReviewRequest;
import com.isaachahn.performrev.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequiredArgsConstructor
@RequestMapping("/data-populator")
public class DataPopulatorController {
    private final ReviewService reviewService;

    @PostMapping("/generate")
    public String generateSampleData() {
        Random random = new Random();
        List<String> employees = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            employees.add("emp" + i);
        }

        List<String> reviewers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            reviewers.add("rev" + i);
        }

        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();

        for (String employee : employees) {
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                for (int i = 0; i < 10; i++) { // 10 reviews per month per employee
                    InputReviewRequest reviewRequest = new InputReviewRequest();
                    reviewRequest.setEmployeeId(employee);
                    reviewRequest.setReviewerId(reviewers.get(random.nextInt(reviewers.size())));


                    Metric metric = new Metric(0,0,0);
                    metric.setGoalAchievement(random.nextInt(41) + 60);
                    metric.setSkillLevel(random.nextInt(41) + 60);
                    metric.setTeamwork(random.nextInt(41) + 60);

                    reviewRequest.setMetrics(metric);
                    reviewRequest.setComments(randomComment(random));
                    reviewRequest.setReviewDate(currentDate);
                    reviewService.submitReview(reviewRequest);
                }
                currentDate = currentDate.plusMonths(1);
            }
        }

        return "Sample data generation started!";
    }

    private String randomComment(Random random) {
        String[] comments = {"Great job", "Needs improvement", "Keep it up", "Excellent work", "Satisfactory performance"};
        return comments[random.nextInt(comments.length)];
    }
}
