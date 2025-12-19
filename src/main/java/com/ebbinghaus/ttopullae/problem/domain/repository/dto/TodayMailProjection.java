package com.ebbinghaus.ttopullae.problem.domain.repository.dto;

public interface TodayMailProjection {
    Long getUserId();
    String getEmail();
    String getUsername();
    String getQuestion();
}
