package com.ebbinghaus.ttopullae.problem.domain.repository.dto;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;

/**
 * 문제와 현재 사용자의 복습 상태를 함께 담는 DTO
 * Repository 쿼리 결과를 담기 위한 용도로 사용됩니다.
 *
 * @param problem 문제 엔티티 (creator fetch join 완료)
 * @param myReviewState 현재 사용자의 복습 상태 (없으면 null)
 */
public record ProblemWithMyReviewDto(
    Problem problem,
    ProblemReviewState myReviewState
) {
}