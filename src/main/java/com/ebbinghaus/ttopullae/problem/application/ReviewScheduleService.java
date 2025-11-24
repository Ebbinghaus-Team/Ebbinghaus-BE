package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewScheduleService {

    private final ProblemReviewStateRepository problemReviewStateRepository;

    /**
     * 매일 자정 5분에 오늘의 복습 문제 스냅샷 생성
     *
     * nextReviewDate가 오늘 이하인 문제들의 todayReviewIncludedDate를 오늘로 설정하고,
     * todayReviewIncludedGate를 현재 gate로 보존하여 필터 일관성을 유지합니다.
     *
     * 처리 대상:
     * - 오늘이 복습날인 문제 (신규)
     * - 이전 복습날에 풀지 않아서 이월된 문제
     *
     * 제외 대상:
     * - 졸업한 문제 (GRADUATED)
     * - 이미 오늘 스냅샷된 문제
     */
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 00:00:00
    @Transactional
    public void createDailyReviewSnapshot() {
        LocalDate today = LocalDate.now();

        int snapshotCount = problemReviewStateRepository.snapshotTodayReviewProblems(today);

        log.info("오늘의 복습 문제 스냅샷 생성 완료: {} 건", snapshotCount);
    }
}
