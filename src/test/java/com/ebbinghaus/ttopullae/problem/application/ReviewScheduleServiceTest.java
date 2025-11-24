package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ProblemType;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewScheduleServiceTest {

    @Autowired
    private ReviewScheduleService reviewScheduleService;

    @Autowired
    private ProblemReviewStateRepository problemReviewStateRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    private User testUser;
    private StudyRoom testStudyRoom;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(testUser);

        testStudyRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 개념 정리")
                .category("프로그래밍")
                .build();
        studyRoomRepository.save(testStudyRoom);
    }

    @Test
    @DisplayName("스냅샷 생성 성공 - nextReviewDate가 오늘인 문제")
    void createSnapshot_Success_TodayProblem() {
        // given
        LocalDate today = LocalDate.now();

        Problem problem = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.OX)
                .question("JVM은 Java Virtual Machine의 약자이다.")
                .explanation("맞습니다.")
                .answerBoolean(true)
                .build();
        problemRepository.save(problem);

        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .build();
        problemReviewStateRepository.save(reviewState);

        // when
        reviewScheduleService.createDailyReviewSnapshot();

        // then
        ProblemReviewState updated = problemReviewStateRepository.findById(reviewState.getStateId()).orElseThrow();
        assertThat(updated.getTodayReviewIncludedDate()).isEqualTo(today);
        assertThat(updated.getTodayReviewIncludedGate()).isEqualTo(ReviewGate.GATE_1);
    }

    @Test
    @DisplayName("스냅샷 생성 성공 - 이월 문제 포함 (nextReviewDate가 과거)")
    void createSnapshot_Success_CarryoverProblem() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Problem problem = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.SHORT)
                .question("JPA의 영속성 컨텍스트란?")
                .explanation("EntityManager가 관리하는 엔티티 집합입니다.")
                .answerText("영속성 컨텍스트")
                .build();
        problemRepository.save(problem);

        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(yesterday)  // 어제가 복습날이었음 (이월)
                .reviewCount(1)
                .todayReviewIncludedDate(yesterday)  // 어제 스냅샷됨
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .build();
        problemReviewStateRepository.save(reviewState);

        // when
        reviewScheduleService.createDailyReviewSnapshot();

        // then
        ProblemReviewState updated = problemReviewStateRepository.findById(reviewState.getStateId()).orElseThrow();
        assertThat(updated.getTodayReviewIncludedDate()).isEqualTo(today);  // 오늘로 재스냅샷
        assertThat(updated.getTodayReviewIncludedGate()).isEqualTo(ReviewGate.GATE_2);
    }

    @Test
    @DisplayName("스냅샷 생성 - 졸업 문제 제외")
    void createSnapshot_ExcludeGraduated() {
        // given
        LocalDate today = LocalDate.now();

        Problem problem = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.MCQ)
                .question("졸업한 문제")
                .explanation("이미 학습 완료")
                .correctChoiceIndex(0)
                .build();
        problemRepository.save(problem);

        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GRADUATED)  // 졸업 상태
                .nextReviewDate(today.plusYears(100))
                .reviewCount(3)
                .build();
        problemReviewStateRepository.save(reviewState);

        // when
        reviewScheduleService.createDailyReviewSnapshot();

        // then
        ProblemReviewState updated = problemReviewStateRepository.findById(reviewState.getStateId()).orElseThrow();
        assertThat(updated.getTodayReviewIncludedDate()).isNull();  // 스냅샷되지 않음
        assertThat(updated.getTodayReviewIncludedGate()).isNull();
    }

    @Test
    @DisplayName("스냅샷 생성 - 중복 스냅샷 방지 (이미 오늘 스냅샷된 문제)")
    void createSnapshot_PreventDuplicateSnapshot() {
        // given
        LocalDate today = LocalDate.now();

        Problem problem = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.OX)
                .question("중복 테스트 문제")
                .explanation("테스트")
                .answerBoolean(true)
                .build();
        problemRepository.save(problem);

        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)  // 이미 오늘 스냅샷됨
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .build();
        problemReviewStateRepository.save(reviewState);

        // when
        reviewScheduleService.createDailyReviewSnapshot();

        // then
        ProblemReviewState updated = problemReviewStateRepository.findById(reviewState.getStateId()).orElseThrow();
        assertThat(updated.getTodayReviewIncludedDate()).isEqualTo(today);
        assertThat(updated.getTodayReviewIncludedGate()).isEqualTo(ReviewGate.GATE_1);
        // 상태가 변경되지 않았음을 확인 (중복 업데이트 방지)
    }

    @Test
    @DisplayName("스냅샷 생성 - 미래 복습 문제 제외")
    void createSnapshot_ExcludeFutureProblem() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        Problem problem = Problem.builder()
                .studyRoom(testStudyRoom)
                .creator(testUser)
                .problemType(ProblemType.SHORT)
                .question("미래 복습 문제")
                .explanation("아직 복습날이 아님")
                .answerText("답")
                .build();
        problemRepository.save(problem);

        ProblemReviewState reviewState = ProblemReviewState.builder()
                .user(testUser)
                .problem(problem)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(tomorrow)  // 내일이 복습날
                .reviewCount(1)
                .build();
        problemReviewStateRepository.save(reviewState);

        // when
        reviewScheduleService.createDailyReviewSnapshot();

        // then
        ProblemReviewState updated = problemReviewStateRepository.findById(reviewState.getStateId()).orElseThrow();
        assertThat(updated.getTodayReviewIncludedDate()).isNull();  // 스냅샷되지 않음
        assertThat(updated.getTodayReviewIncludedGate()).isNull();
    }
}