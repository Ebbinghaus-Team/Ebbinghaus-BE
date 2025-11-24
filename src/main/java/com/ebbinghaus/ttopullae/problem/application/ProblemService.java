package com.ebbinghaus.ttopullae.problem.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.exception.CommonException;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateResult;
import com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult;
import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import com.ebbinghaus.ttopullae.problem.domain.ProblemChoice;
import com.ebbinghaus.ttopullae.problem.domain.ProblemKeyword;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemAttemptRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemChoiceRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemKeywordRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.problem.exception.ProblemException;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
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

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemChoiceRepository problemChoiceRepository;
    private final ProblemKeywordRepository problemKeywordRepository;
    private final ProblemReviewStateRepository problemReviewStateRepository;
    private final ProblemAttemptRepository problemAttemptRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProblemCreateResult createProblem(ProblemCreateCommand command) {
        User user = findUserById(command.userId());
        StudyRoom studyRoom = findStudyRoomById(command.studyRoomId());

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
                    .build();

            problemReviewStateRepository.save(reviewState);
        }
    }
}
