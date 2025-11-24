package com.ebbinghaus.ttopullae.problem.application.dto;

public record TodayReviewCommand(
    Long userId,
    String filter  // ALL, GATE_1, GATE_2
) {
}