package com.ebbinghaus.ttopullae.problem.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 오늘의 복습 문제에 대한 풀이 상태
 */
@Getter
@AllArgsConstructor
public enum AttemptStatus {

    NOT_ATTEMPTED("아직 풀지 않음"),
    CORRECT("정답"),
    INCORRECT("오답");

    private final String description;
}