package com.isaachahn.performrev;

import com.isaachahn.performrev.model.domain.Metric;
import com.isaachahn.performrev.model.domain.Review;
import com.isaachahn.performrev.model.payload.InputReviewRequest;
import com.isaachahn.performrev.model.payload.InputReviewResponse;
import com.isaachahn.performrev.model.payload.PeerComparison;
import com.isaachahn.performrev.model.payload.PerformanceReport;
import com.isaachahn.performrev.service.ReviewProducer;
import com.isaachahn.performrev.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewProducer reviewProducer;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void submitReview_shouldReturnInputReviewResponse() {
        InputReviewRequest request = new InputReviewRequest();
        request.setEmployeeId("emp1");
        request.setReviewerId("rev1");
        request.setMetrics(new Metric(4, 3, 5));
        request.setComments("Good performance");
        InputReviewResponse response = reviewService.submitReview(request);
        assertNotNull(response);
        assertEquals("submitted", response.getStatus());
        verify(reviewProducer, times(1)).sendReview(any(Review.class));
    }

    @Test
    public void testGetPerformanceReport_NotFound() {
        AggregationResults<PerformanceReport> mockResults = mock(AggregationResults.class);

        when(mongoTemplate.aggregate(any(), anyString(), eq(PerformanceReport.class)))
                .thenReturn(mockResults);
        when(mockResults.getMappedResults()).thenReturn(Collections.emptyList());

        assertThrows(ResponseStatusException.class, () -> reviewService.getPerformanceReport("employee1"));
    }

    @Test
    void getDepartmentPerformanceSummary_shouldThrowExceptionWhenDepartmentNotFound() {
        String departmentId = "dept1";
        AggregationResults<Map> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(null);
        when(mongoTemplate.aggregate(any(), eq("reviews"), eq(Map.class))).thenReturn(aggregationResults);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            reviewService.getDepartmentPerformanceSummary(departmentId);
        });

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    public void testGetPeerComparison_NotFound() {
        when(mongoTemplate.findOne(any(Query.class), eq(Review.class))).thenReturn(null);

        ResponseEntity<PeerComparison> response = reviewService.getPeerComparison("employee1");

        assertEquals(404, response.getStatusCodeValue());
    }
}

