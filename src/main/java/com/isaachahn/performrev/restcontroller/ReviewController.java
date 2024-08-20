package com.isaachahn.performrev.restcontroller;

import com.isaachahn.performrev.model.payload.*;
import com.isaachahn.performrev.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<InputReviewResponse> submitReview(@RequestBody InputReviewRequest inputReviewRequest) {
        return ResponseEntity.ok(reviewService.submitReview(inputReviewRequest));
    }

    @GetMapping("/employees/{employeeId}/performance")
    public PerformanceReport getPerformanceReport(@PathVariable String employeeId) {
        return reviewService.getPerformanceReport(employeeId);
    }

    @GetMapping("/employees/{employeeId}/peer-comparison")
    public ResponseEntity<PeerComparison> getPeerComparison(@PathVariable String employeeId) {
        return reviewService.getPeerComparison(employeeId);
    }

    @GetMapping("/departments/{departmentId}/performance-summary")
    public PerformanceSummary getPerformanceSummary(@PathVariable String departmentId) {
        return reviewService.getDepartmentPerformanceSummary(departmentId);
    }
}
