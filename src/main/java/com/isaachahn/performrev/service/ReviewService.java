package com.isaachahn.performrev.service;


import com.isaachahn.performrev.model.domain.EmployeeInfo;
import com.isaachahn.performrev.model.domain.Metric;
import com.isaachahn.performrev.model.domain.Review;
import com.isaachahn.performrev.model.payload.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewProducer reviewProducer;
    private final MongoTemplate mongoTemplate;


    public InputReviewResponse submitReview(InputReviewRequest inputReviewRequest) {
        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setEmployeeId(inputReviewRequest.getEmployeeId());
        review.setReviewerId(inputReviewRequest.getReviewerId());
        review.setReviewDate(inputReviewRequest.getReviewDate()==null ? LocalDate.now() : inputReviewRequest.getReviewDate());
        review.setMetrics(inputReviewRequest.getMetrics());
        review.setComments(inputReviewRequest.getComments());

        EmployeeInfo employeeInfo = new EmployeeInfo();
        employeeInfo.setDepartmentId("dept1");
        employeeInfo.setRole("developer");

        review.setEmployeeInfo(employeeInfo);
        review.setOverallScore(calculateOverallScore(inputReviewRequest.getMetrics()));

        reviewProducer.sendReview(review);

        return new InputReviewResponse(review.getId(),"submitted");
    }

    private double calculateOverallScore(Metric metrics) {
        return (metrics.getGoalAchievement() * 0.4) +(metrics.getSkillLevel() * 0.3) +(metrics.getTeamwork() * 0.3) ;
    }

    public PerformanceReport getPerformanceReport(String employeeId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("employeeId").is(employeeId)),
                Aggregation.group("employeeId")
                        .avg("overallScore").as("averageScore")
                        .first("employeeInfo.departmentId").as("departmentId")
                        .first("employeeId").as("employeeId"),
                Aggregation.lookup("reviews", "_id", "employeeId", "reviews")
        );

        AggregationResults<PerformanceReport> results = mongoTemplate.aggregate(aggregation, "reviews", PerformanceReport.class);
        if (results == null || results.getMappedResults().isEmpty()) {
            throw new ResponseStatusException(404, "not found", null);
        }else {
            return results.getUniqueMappedResult().setTrends(calculateTrends());
        }
    }

    private Trends calculateTrends() {
        Trends trends = new Trends();

        LocalDate lastQuarter = LocalDate.now().minusMonths(3);
        LocalDate lastYear = LocalDate.now().minusYears(1);

        Aggregation lastQuarterAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("reviewDate").gte(lastQuarter)),
                Aggregation.group().avg("overallScore").as("lastQuarter")
        );
        AggregationResults<Trends> lastQuarterResult = mongoTemplate.aggregate(lastQuarterAgg, "reviews", Trends.class);
        Trends uniqueMappedResult = lastQuarterResult.getUniqueMappedResult();
        if (uniqueMappedResult != null) {
            trends.setLastQuarter(uniqueMappedResult.getLastQuarter());
        } else {
            throw new ResponseStatusException(500, "internal server error", null);
        }

        Aggregation lastYearAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("reviewDate").gte(lastYear)),
                Aggregation.group().avg("overallScore").as("lastYear")
        );
        AggregationResults<Trends> lastYearResult = mongoTemplate.aggregate(lastYearAgg, "reviews", Trends.class);
        Trends uniqueMappedResultLastYear = lastYearResult.getUniqueMappedResult();

        if (uniqueMappedResultLastYear != null) {
            trends.setLastYear(uniqueMappedResultLastYear.getLastYear());
        } else {
            throw new ResponseStatusException(500, "internal server error", null);
        }
        return trends;
    }

    public ResponseEntity<PeerComparison> getPeerComparison(String employeeId) {
        Review employeeReview = mongoTemplate.findOne(
                Query.query(Criteria.where("employeeId").is(employeeId)),
                Review.class
        );

        if (employeeReview == null) {
            return ResponseEntity.notFound().build();
        }

        double employeeScore = employeeReview.getOverallScore();

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("employeeInfo.departmentId").is(employeeReview.getEmployeeInfo().getDepartmentId())
                        .and("employeeInfo.role").is(employeeReview.getEmployeeInfo().getRole())),

                Aggregation.project("overallScore")
                        .and(ConditionalOperators.Cond.when(Criteria.where("overallScore").lt(employeeScore)).then(1).otherwise(0)).as("belowCount"),

                Aggregation.group()
                        .avg("overallScore").as("peerAverageScore")
                        .sum("belowCount").as("belowCount")
                        .count().as("totalPeers")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "reviews", Map.class);
        Map<String, Object> result = results.getUniqueMappedResult();

        if (result == null || !result.containsKey("peerAverageScore")) {
            return ResponseEntity.internalServerError().build();
        }

        double peerAverageScore = (double) result.get("peerAverageScore");
        long belowCount = ((Number) result.get("belowCount")).longValue();
        long totalPeers = ((Number) result.get("totalPeers")).longValue();

        double percentileRank = ((double) belowCount / totalPeers) * 100;


        return ResponseEntity.ok(new PeerComparison()
                .setEmployeeId(employeeId)
                .setDepartmentId(employeeReview.getEmployeeInfo().getDepartmentId())
                .setRole(employeeReview.getEmployeeInfo().getRole())
                .setAverageScore(employeeReview.getOverallScore())
                .setPercentileRank(percentileRank)
                .setPeerAverageScore(peerAverageScore)
        );
    }

    public PerformanceSummary getDepartmentPerformanceSummary(String departmentId) {
        Aggregation departmentAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("employeeInfo.departmentId").is(departmentId)),
                Aggregation.group("employeeInfo.departmentId")
                        .avg("overallScore").as("departmentAverageScore")
        );

        AggregationResults<Map> departmentResults = mongoTemplate.aggregate(departmentAggregation, "reviews", Map.class);

        Double departmentAverageScore;
        if (departmentResults != null) {
            Map uniqueMappedResult = departmentResults.getUniqueMappedResult();
            if (uniqueMappedResult != null) {
                departmentAverageScore = (Double) uniqueMappedResult.get("departmentAverageScore");
            } else {
                throw new ResponseStatusException(404, "department not found", null);
            }
        }else {
            throw new ResponseStatusException(404, "department not found", null);
        }

        Aggregation topPerformersAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("employeeInfo.departmentId").is(departmentId)),
                Aggregation.group("employeeId")
                        .avg("overallScore").as("averageScore")
                        .first("employeeId").as("employeeId"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "averageScore")),
                Aggregation.limit(2)
        );

        AggregationResults<Map> topPerformersResults = mongoTemplate.aggregate(topPerformersAggregation, "reviews", Map.class);
        List<Map> topPerformers = addRanking(topPerformersResults.getMappedResults());
        List<EmployeeRank> listTopRank = topPerformers.stream().map(val -> new EmployeeRank()
                .setEmployeeId((String) val.get("employeeId"))
                .setOverallScore((Double) val.get("averageScore"))
                .setRank((Integer) val.get("rank"))).toList();

        Aggregation lowPerformersAggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("employeeInfo.departmentId").is(departmentId)),
                Aggregation.group("employeeId")
                        .avg("overallScore").as("averageScore")
                        .first("employeeId").as("employeeId"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "averageScore")),
                Aggregation.limit(2)
        );

        AggregationResults<Map> lowPerformersResults = mongoTemplate.aggregate(lowPerformersAggregation, "reviews", Map.class);
        List<Map> lowPerformers = addRanking(lowPerformersResults.getMappedResults());
        List<EmployeeRank> listLowRank = lowPerformers.stream().map(val -> new EmployeeRank()
                .setEmployeeId((String) val.get("employeeId"))
                .setOverallScore((Double) val.get("averageScore"))
                .setRank((Integer) val.get("rank"))).toList();

        return new PerformanceSummary()
                .setDepartmentId(departmentId)
                .setAverageScore(departmentAverageScore)
                .setTopPerformance(listTopRank)
                .setLowPerformance(listLowRank);
    }

    private List<Map> addRanking(List<Map> performers) {
        return IntStream.range(0, performers.size())
                .mapToObj(i -> {
                    Map performer = performers.get(i);
                    performer.put("rank", i + 1);
                    return performer;
                })
                .toList();
    }
}
