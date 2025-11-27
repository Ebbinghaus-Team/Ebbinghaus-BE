package com.ebbinghaus.ttopullae.problem.domain;

/**
 * 문제의 복습 단계를 나타내는 열거형
 * - NOT_IN_REVIEW: 복습 주기에 포함되지 않음 (아직 풀지 않았거나 복습 루프에 미포함)
 * - GATE_1: 1일차 복습 단계
 * - GATE_2: 7일차 복습 단계
 * - GRADUATED: 복습 완료
 */
public enum ReviewGate {
    NOT_IN_REVIEW,  // 복습 주기에 포함되지 않음 (DB에는 저장 안 됨, 응답 전용)
    GATE_1,         // 1일차 복습
    GATE_2,         // 7일차 복습
    GRADUATED       // 복습 완료
}
