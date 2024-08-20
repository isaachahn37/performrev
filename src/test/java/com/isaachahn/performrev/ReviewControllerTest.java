package com.isaachahn.performrev;

import com.isaachahn.performrev.model.payload.*;
import com.isaachahn.performrev.restcontroller.ReviewController;
import com.isaachahn.performrev.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSubmitReview() {
        InputReviewRequest mockRequest = new InputReviewRequest();
        InputReviewResponse mockResponse = new InputReviewResponse("123", "submitted");

        when(reviewService.submitReview(any(InputReviewRequest.class))).thenReturn(mockResponse);

        ResponseEntity<InputReviewResponse> response = reviewController.submitReview(mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetPerformanceReport() {
        PerformanceReport mockReport = new PerformanceReport();

        when(reviewService.getPerformanceReport("employee1")).thenReturn(mockReport);

        PerformanceReport response = reviewController.getPerformanceReport("employee1");

        assertEquals(mockReport, response);
    }

    @Test
    public void testGetPeerComparison() {
        PeerComparison mockComparison = new PeerComparison();
        ResponseEntity<PeerComparison> mockResponse = ResponseEntity.ok(mockComparison);

        when(reviewService.getPeerComparison("employee1")).thenReturn(mockResponse);

        ResponseEntity<PeerComparison> response = reviewController.getPeerComparison("employee1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockComparison, response.getBody());
    }

    @Test
    public void testGetPerformanceSummary() {
        PerformanceSummary mockSummary = new PerformanceSummary();

        when(reviewService.getDepartmentPerformanceSummary("dept1")).thenReturn(mockSummary);

        PerformanceSummary response = reviewController.getPerformanceSummary("dept1");

        assertEquals(mockSummary, response);
    }
}
