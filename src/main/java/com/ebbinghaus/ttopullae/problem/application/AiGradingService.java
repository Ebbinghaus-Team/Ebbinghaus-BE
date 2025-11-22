package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingRequest;
import com.ebbinghaus.ttopullae.problem.application.dto.AiGradingResult;

/**
 * AI 기반 서술형 답안 채점 서비스 인터페이스
 */
public interface AiGradingService {

  /**
   * 서술형 답안을 AI로 채점한다
   *
   * @param request 채점 요청 정보 (주제, 문제, 모범답안, 키워드, 사용자답안)
   * @return 채점 결과 (정답여부, 피드백, 누락키워드, 채점근거)
   */
  AiGradingResult gradeSubjectiveAnswer(AiGradingRequest request);
}
