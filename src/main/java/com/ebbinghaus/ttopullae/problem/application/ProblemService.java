package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.exception.CommonException;
import com.ebbinghaus.ttopullae.problem.application.dto.*;
import com.ebbinghaus.ttopullae.problem.domain.*;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemAttemptRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemChoiceRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemKeywordRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.problem.exception.ProblemException;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemChoiceRepository problemChoiceRepository;
    private final ProblemKeywordRepository problemKeywordRepository;
    private final ProblemReviewStateRepository problemReviewStateRepository;
    private final ProblemAttemptRepository problemAttemptRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;
    private final UserRepository userRepository;
    private final AiGradingService aiGradingService;

    @Transactional
    public ProblemCreateResult createProblem(ProblemCreateCommand command) {
        User user = findUserById(command.userId());
        StudyRoom studyRoom = findStudyRoomById(command.studyRoomId());

        // 스터디룸 멤버십 검증
        validateStudyRoomMembership(user, studyRoom);

        validateProblemData(command);

        Problem problem = buildProblem(command, user, studyRoom);
        Problem savedProblem = problemRepository.save(problem);

        saveRelatedEntities(command, savedProblem);
        initializeReviewState(user, savedProblem, studyRoom);

        return ProblemCreateResult.from(savedProblem);
    }

    /**
     * 오늘의 복습 문제 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public TodayReviewResult getTodayReviewProblems(TodayReviewCommand command) {
        LocalDate today = LocalDate.now();

        // 필터 파라미터 변환
        ReviewGate targetGate = parseFilterToGate(command.filter());

        // 복습 상태 조회 (Problem 엔티티 fetch join)
        List<ProblemReviewState> reviewStates = problemReviewStateRepository
            .findTodaysReviewProblems(command.userId(), today, targetGate);

        // 오늘의 첫 번째 풀이 기록 조회 (N+1 방지)
        List<ProblemAttempt> todaysAttempts = Collections.emptyList();
        if (!reviewStates.isEmpty()) {
            List<Long> problemIds = reviewStates.stream()
                .map(rs -> rs.getProblem().getProblemId())
                .toList();

            LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
            LocalDateTime tomorrowStart = LocalDateTime.of(today.plusDays(1), LocalTime.MIN);

            todaysAttempts = problemAttemptRepository.findTodaysFirstAttemptsByUserAndProblems(
                command.userId(),
                problemIds,
                todayStart,
                tomorrowStart
            );
        }

        // DTO 변환 (대시보드 통계 계산 및 풀이 상태 포함)
        return TodayReviewResult.of(reviewStates, today, todaysAttempts);
    }

    /**
     * 문제 상세 정보를 조회합니다.
     * 정답 정보는 노출하지 않습니다.
     */
    @Transactional(readOnly = true)
    public ProblemDetailResult getProblemDetail(Long userId, Long problemId) {
        User user = findUserById(userId);
        Problem problem = findProblemById(problemId);

        // 스터디룸 멤버십 검증
        validateStudyRoomAccess(user, problem);

        // 객관식 선택지 조회 (정답 제외)
        List<ProblemChoice> choices = null;
        if (problem.getProblemType() == ProblemType.MCQ) {
            choices = problemChoiceRepository.findByProblem(problem);
        }

        // 사용자의 복습 상태 조회 (없으면 null)
        ProblemReviewState reviewState = problemReviewStateRepository
                .findByUserAndProblem(user, problem)
                .orElse(null);

        return ProblemDetailResult.of(problem, choices, reviewState);
    }

    private ReviewGate parseFilterToGate(String filter) {
        return switch (filter) {
            case "GATE_1" -> ReviewGate.GATE_1;
            case "GATE_2" -> ReviewGate.GATE_2;
            case "ALL" -> null;
            default -> throw new ApplicationException(CommonException.INVALID_QUERY_PARAMETER);
        };
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserException.USER_NOT_FOUND));
    }

    private StudyRoom findStudyRoomById(Long studyRoomId) {
        return studyRoomRepository.findById(studyRoomId)
                .orElseThrow(() -> new ApplicationException(ProblemException.STUDYROOM_NOT_FOUND));
    }

    private void validateProblemData(ProblemCreateCommand command) {
        switch (command.problemType()) {
            case MCQ -> validateMcqData(command);
            case OX -> validateOxData(command);
            case SHORT -> validateShortData(command);
            case SUBJECTIVE -> validateSubjectiveData(command);
        }
    }

    private void validateMcqData(ProblemCreateCommand command) {
        if (command.choices() == null || command.choices().isEmpty()) {
            throw new ApplicationException(ProblemException.INVALID_MCQ_DATA);
        }
        if (command.correctChoiceIndex() == null) {
            throw new ApplicationException(ProblemException.INVALID_MCQ_DATA);
        }
        if (command.correctChoiceIndex() < 0 || command.correctChoiceIndex() >= command.choices().size()) {
            throw new ApplicationException(ProblemException.INVALID_CHOICE_INDEX);
        }
    }

    private void validateOxData(ProblemCreateCommand command) {
        if (command.answerBoolean() == null) {
            throw new ApplicationException(ProblemException.INVALID_OX_DATA);
        }
    }

    private void validateShortData(ProblemCreateCommand command) {
        if (command.answerText() == null || command.answerText().isBlank()) {
            throw new ApplicationException(ProblemException.INVALID_SHORT_DATA);
        }
    }

    private void validateSubjectiveData(ProblemCreateCommand command) {
        if (command.modelAnswerText() == null || command.modelAnswerText().isBlank()) {
            throw new ApplicationException(ProblemException.INVALID_SUBJECTIVE_DATA);
        }
        if (command.keywords() == null || command.keywords().isEmpty()) {
            throw new ApplicationException(ProblemException.INVALID_SUBJECTIVE_DATA);
        }
    }

    private Problem buildProblem(ProblemCreateCommand command, User user, StudyRoom studyRoom) {
        return Problem.builder()
                .studyRoom(studyRoom)
                .creator(user)
                .problemType(command.problemType())
                .question(command.question())
                .explanation(command.explanation())
                .answerBoolean(command.answerBoolean())
                .answerText(command.answerText())
                .modelAnswerText(command.modelAnswerText())
                .correctChoiceIndex(command.correctChoiceIndex())
                .build();
    }

    private void saveRelatedEntities(ProblemCreateCommand command, Problem problem) {
        if (command.problemType() == com.ebbinghaus.ttopullae.problem.domain.ProblemType.MCQ) {
            saveProblemChoices(command.choices(), problem);
        }
        if (command.problemType() == com.ebbinghaus.ttopullae.problem.domain.ProblemType.SUBJECTIVE) {
            saveProblemKeywords(command.keywords(), problem);
        }
    }

    private void saveProblemChoices(List<String> choices, Problem problem) {
        List<ProblemChoice> problemChoices = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            ProblemChoice choice = ProblemChoice.builder()
                    .problem(problem)
                    .choiceOrder(i + 1)
                    .choiceText(choices.get(i))
                    .build();
            problemChoices.add(choice);
        }
        problemChoiceRepository.saveAll(problemChoices);
    }

    private void saveProblemKeywords(List<String> keywords, Problem problem) {
        List<ProblemKeyword> problemKeywords = keywords.stream()
                .map(keyword -> ProblemKeyword.builder()
                        .problem(problem)
                        .keyword(keyword)
                        .build())
                .toList();
        problemKeywordRepository.saveAll(problemKeywords);
    }

    private void initializeReviewState(User user, Problem problem, StudyRoom studyRoom) {
        if (studyRoom.getRoomType() == RoomType.PERSONAL ||
                studyRoom.getOwner().getUserId().equals(user.getUserId())) {

            ProblemReviewState reviewState = ProblemReviewState.builder()
                    .user(user)
                    .problem(problem)
                    .gate(ReviewGate.GATE_1)
                    .nextReviewDate(LocalDate.now().plusDays(1))
                    .reviewCount(0)
                    .includeInReview(true)  // 본인이 만든 문제는 무조건 복습 루프에 포함
                    .reviewInclusionConfigured(true)  // 본인 문제는 설정 변경 불가
                    .build();

            problemReviewStateRepository.save(reviewState);
        }
    }

    /**
     * 문제 풀이 제출 및 채점
     */
    @Transactional
    public ProblemSubmitResult submitProblemAnswer(ProblemSubmitCommand command) {
        User user = findUserById(command.userId());
        Problem problem = findProblemById(command.problemId());
        LocalDate today = LocalDate.now();

        // 그룹 스터디룸 접근 권한 검증
        validateStudyRoomAccess(user, problem);

        // ReviewState 조회
        Optional<ProblemReviewState> optionalReviewState =
                problemReviewStateRepository.findByUserAndProblem(user, problem);

        // 채점 수행
        boolean isCorrect = gradeAnswer(problem, command.answer());
        String aiFeedback = null;

        if (problem.getProblemType() == ProblemType.SUBJECTIVE) {
            aiFeedback = gradeEssayWithAi(problem, command.answer());
        }

        // 시도 로그 저장 (ReviewState 존재 여부와 무관하게 항상 저장)
        saveProblemAttempt(user, problem, command.answer(), isCorrect, aiFeedback);

        // ReviewState가 없으면 채점 결과만 반환 (그룹방 타인 문제 첫 풀이)
        if (optionalReviewState.isEmpty()) {
            return buildSubmitResultWithoutReviewState(
                    isCorrect,
                    problem.getExplanation(),
                    aiFeedback
            );
        }

        // ReviewState가 있는 경우 기존 복습 로직 수행
        ProblemReviewState reviewState = optionalReviewState.get();

        // 오늘의 복습 문제 여부 판단
        boolean isTodayReview = reviewState.isTodayReviewProblem(today);

        // 첫 시도 여부 판단 (오늘의 복습 문제인 경우만)
        boolean isFirstAttemptToday = isTodayReview && reviewState.isFirstAttemptToday(today);

        // 상태 전이 처리 (오늘의 복습 문제 + 첫 시도만)
        boolean isReviewStateChanged = false;
        if (isTodayReview && isFirstAttemptToday) {
            updateReviewStateOnFirstAttempt(reviewState, isCorrect, today);
            isReviewStateChanged = true;
        }

        // 결과 반환
        return buildSubmitResult(
                isCorrect,
                problem.getExplanation(),
                aiFeedback,
                reviewState,
                isFirstAttemptToday,
                isReviewStateChanged
        );
    }

    private Problem findProblemById(Long problemId) {
        return problemRepository.findById(problemId)
                .orElseThrow(() -> new ApplicationException(ProblemException.PROBLEM_NOT_FOUND));
    }

    /**
     * ReviewState 없이 채점 결과만 반환 (그룹방 타인 문제 첫 풀이)
     */
    private ProblemSubmitResult buildSubmitResultWithoutReviewState(
            boolean isCorrect,
            String explanation,
            String aiFeedback
    ) {
        return new ProblemSubmitResult(
                isCorrect,
                explanation,
                aiFeedback,
                null,     // currentGate: null = 복습 주기에 없음
                null,     // reviewCount: null = 복습 안 함
                null,     // nextReviewDate: null = 복습 예정 없음
                false,    // isFirstAttempt: false = 오늘의 복습 아님
                false     // isReviewStateChanged: false = 상태 변화 없음
        );
    }

    /**
     * 답안 채점 (문제 유형별)
     */
    private boolean gradeAnswer(Problem problem, String answer) {
        return switch (problem.getProblemType()) {
            case MCQ -> gradeMultipleChoice(problem, answer);
            case OX -> gradeTrueFalse(problem, answer);
            case SHORT -> gradeShortAnswer(problem, answer);
            case SUBJECTIVE -> gradeEssay(problem, answer);
        };
    }

    private boolean gradeMultipleChoice(Problem problem, String answer) {
        try {
            int submittedIndex = Integer.parseInt(answer);
            return submittedIndex == problem.getCorrectChoiceIndex();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean gradeTrueFalse(Problem problem, String answer) {
        try {
            boolean submittedAnswer = Boolean.parseBoolean(answer);
            return submittedAnswer == problem.getAnswerBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean gradeShortAnswer(Problem problem, String answer) {
        if (answer == null || answer.isBlank()) {
            return false;
        }
        // 대소문자 무시, 모든 공백 제거 후 비교
        String normalizedAnswer = answer.replaceAll("\\s+", "").toLowerCase();
        String correctAnswer = problem.getAnswerText().replaceAll("\\s+", "").toLowerCase();
        return normalizedAnswer.equals(correctAnswer);
    }

    private boolean gradeEssay(Problem problem, String answer) {
        // 서술형은 AI 채점 결과로 판단
        if (answer == null || answer.isBlank()) {
            return false;
        }

        List<ProblemKeyword> keywords = problemKeywordRepository.findByProblem(problem);
        List<String> keywordTexts = keywords.stream()
                .map(ProblemKeyword::getKeyword)
                .toList();

        AiGradingRequest request = new AiGradingRequest(
                problem.getStudyRoom().getName(),
                problem.getQuestion(),
                problem.getModelAnswerText(),
                keywordTexts,
                answer
        );

        AiGradingResult result = aiGradingService.gradeSubjectiveAnswer(request);
        return result.isCorrect();
    }

    private String gradeEssayWithAi(Problem problem, String answer) {
        if (answer == null || answer.isBlank()) {
            return "답안이 입력되지 않았습니다.";
        }

        List<ProblemKeyword> keywords = problemKeywordRepository.findByProblem(problem);
        List<String> keywordTexts = keywords.stream()
                .map(ProblemKeyword::getKeyword)
                .toList();

        AiGradingRequest request = new AiGradingRequest(
                problem.getStudyRoom().getName(),
                problem.getQuestion(),
                problem.getModelAnswerText(),
                keywordTexts,
                answer
        );

        AiGradingResult result = aiGradingService.gradeSubjectiveAnswer(request);
        return result.feedback();
    }

    /**
     * 오늘의 복습 첫 시도 시 상태 전이 처리
     */
    private void updateReviewStateOnFirstAttempt(ProblemReviewState reviewState,
                                                  boolean isCorrect,
                                                  LocalDate today) {
        // 첫 시도 날짜 기록
        reviewState.recordFirstAttemptToday(today);

        // 복습 횟수 증가
        reviewState.increaseReviewCount();

        // 정답/오답에 따른 상태 전이
        if (isCorrect) {
            handleCorrectAnswer(reviewState, today);
        } else {
            handleWrongAnswer(reviewState, today);
        }
    }

    private void handleCorrectAnswer(ProblemReviewState reviewState, LocalDate today) {
        ReviewGate currentGate = reviewState.getGate();

        if (currentGate == ReviewGate.GATE_1) {
            // GATE_1 → GATE_2 승급
            reviewState.updateGate(ReviewGate.GATE_2, today.plusDays(7));
        } else if (currentGate == ReviewGate.GATE_2) {
            // GATE_2 → GRADUATED 졸업
            reviewState.updateGate(ReviewGate.GRADUATED, null);
        }
        // GRADUATED인 경우 상태 불변
    }

    private void handleWrongAnswer(ProblemReviewState reviewState, LocalDate today) {
        ReviewGate currentGate = reviewState.getGate();

        if (currentGate == ReviewGate.GATE_1) {
            // GATE_1 유지 (강등 불가)
            reviewState.updateGate(ReviewGate.GATE_1, today.plusDays(1));
        } else if (currentGate == ReviewGate.GATE_2) {
            // GATE_2 → GATE_1 강등
            reviewState.updateGate(ReviewGate.GATE_1, today.plusDays(1));
        }
        // GRADUATED인 경우 상태 불변
    }

    /**
     * 문제 풀이 시도 로그 저장
     */
    private void saveProblemAttempt(User user, Problem problem, String answer,
                                    boolean isCorrect, String aiFeedback) {
        Integer choiceIndex = null;
        Boolean boolAnswer = null;
        String textAnswer = null;

        // 답안 유형별 저장
        switch (problem.getProblemType()) {
            case MCQ -> {
                try {
                    choiceIndex = Integer.parseInt(answer);
                } catch (NumberFormatException e) {
                    // 파싱 실패 시 null 유지
                }
            }
            case OX -> {
                try {
                    boolAnswer = Boolean.parseBoolean(answer);
                } catch (Exception e) {
                    // 파싱 실패 시 null 유지
                }
            }
            case SHORT, SUBJECTIVE -> textAnswer = answer;
        }

        ProblemAttempt attempt = ProblemAttempt.builder()
                .user(user)
                .problem(problem)
                .submittedChoiceIndex(choiceIndex)
                .submittedBoolean(boolAnswer)
                .submittedAnswerText(textAnswer)
                .isCorrect(isCorrect)
                .aiFeedbackJson(aiFeedback)
                .build();

        problemAttemptRepository.save(attempt);
    }

    /**
     * 제출 결과 DTO 생성
     */
    private ProblemSubmitResult buildSubmitResult(boolean isCorrect,
                                                  String explanation,
                                                  String aiFeedback,
                                                  ProblemReviewState reviewState,
                                                  boolean isFirstAttempt,
                                                  boolean isReviewStateChanged) {
        String nextReviewDateStr = null;
        if (reviewState.getNextReviewDate() != null) {
            nextReviewDateStr = reviewState.getNextReviewDate().toString();
        }

        return new ProblemSubmitResult(
                isCorrect,
                explanation,
                aiFeedback,
                reviewState.getGate(),
                reviewState.getReviewCount(),
                nextReviewDateStr,
                isFirstAttempt,
                isReviewStateChanged
        );
    }

    /**
     * 그룹 스터디룸 접근 권한 검증 (Problem 객체 기반)
     * - 개인 스터디룸: 소유자만 접근 가능
     * - 그룹 스터디룸: 소유자 또는 활성 멤버만 접근 가능
     */
    private void validateStudyRoomAccess(User user, Problem problem) {
        StudyRoom studyRoom = problem.getStudyRoom();

        // 개인 스터디룸인 경우: 소유자만 접근 가능
        if (studyRoom.getRoomType() == RoomType.PERSONAL) {
            if (!studyRoom.getOwner().getUserId().equals(user.getUserId())) {
                throw new ApplicationException(ProblemException.ROOM_ACCESS_DENIED);
            }
        }

        // 그룹 스터디룸인 경우: 소유자 또는 활성 멤버만 접근 가능
        if (studyRoom.getRoomType() == RoomType.GROUP) {
            boolean isOwner = studyRoom.getOwner().getUserId().equals(user.getUserId());
            boolean isMember = studyRoomMemberRepository
                    .existsByUserAndStudyRoomAndActive(user, studyRoom, true);

            if (!isOwner && !isMember) {
                throw new ApplicationException(ProblemException.ROOM_ACCESS_DENIED);
            }
        }
    }

    /**
     * 스터디룸 멤버십 검증 (StudyRoom 객체 기반)
     * - 개인 스터디룸: 소유자만 접근 가능
     * - 그룹 스터디룸: 소유자 또는 활성 멤버만 접근 가능
     */
    private void validateStudyRoomMembership(User user, StudyRoom studyRoom) {
        // 개인 스터디룸인 경우: 소유자만 접근 가능
        if (studyRoom.getRoomType() == RoomType.PERSONAL) {
            if (!studyRoom.getOwner().getUserId().equals(user.getUserId())) {
                throw new ApplicationException(ProblemException.ROOM_ACCESS_DENIED);
            }
        }

        // 그룹 스터디룸인 경우: 소유자 또는 활성 멤버만 접근 가능
        if (studyRoom.getRoomType() == RoomType.GROUP) {
            boolean isOwner = studyRoom.getOwner().getUserId().equals(user.getUserId());
            boolean isMember = studyRoomMemberRepository
                    .existsByUserAndStudyRoomAndActive(user, studyRoom, true);

            if (!isOwner && !isMember) {
                throw new ApplicationException(ProblemException.ROOM_ACCESS_DENIED);
            }
        }
    }

    /**
     * 복습 루프 포함 설정 변경 (복습에 추가 역할 겸함)
     */
    @Transactional
    public Boolean configureReviewInclusion(Long userId, ProblemReviewInclusionCommand command) {
        User user = findUserById(userId);
        Problem problem = findProblemById(command.problemId());

        // 본인이 만든 문제인지 확인 (본인 문제는 설정 변경 불가)
        boolean isOwnProblem = problem.getCreator().getUserId().equals(user.getUserId());
        if (isOwnProblem) {
            throw new ApplicationException(ProblemException.REVIEW_INCLUSION_NOT_CONFIGURABLE);
        }

        // ReviewState 조회 또는 생성 (그룹방 타인 문제를 복습에 추가하는 시점)
        ProblemReviewState reviewState = problemReviewStateRepository
                .findByUserAndProblem(user, problem)
                .orElseGet(() -> {
                    // ReviewState가 없으면 생성 (복습 주기에 추가)
                    ProblemReviewState newState = ProblemReviewState.builder()
                            .user(user)
                            .problem(problem)
                            .gate(ReviewGate.GATE_1)
                            .nextReviewDate(LocalDate.now().plusDays(1))
                            .reviewCount(0)
                            .includeInReview(command.includeInReview())
                            .reviewInclusionConfigured(true)
                            .build();
                    return problemReviewStateRepository.save(newState);
                });

        // 이미 설정했는지 확인
        if (!reviewState.canConfigureReviewInclusion()) {
            throw new ApplicationException(ProblemException.REVIEW_INCLUSION_ALREADY_CONFIGURED);
        }

        // 설정 변경
        reviewState.configureReviewInclusion(command.includeInReview());

        return command.includeInReview();
    }
}
