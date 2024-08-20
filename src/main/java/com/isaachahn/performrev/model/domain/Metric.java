package com.isaachahn.performrev.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Metric {
    private double goalAchievement;
    private double skillLevel;
    private double teamwork;
}
