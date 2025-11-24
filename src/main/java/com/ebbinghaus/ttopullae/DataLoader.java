package com.ebbinghaus.ttopullae;

import com.ebbinghaus.ttopullae.problem.domain.*;
import com.ebbinghaus.ttopullae.problem.domain.repository.*;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;
    private final ProblemRepository problemRepository;
    private final ProblemReviewStateRepository problemReviewStateRepository;
    private final ProblemAttemptRepository problemAttemptRepository;
    private final ProblemChoiceRepository problemChoiceRepository;
    private final ProblemKeywordRepository problemKeywordRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========== 개발용 테스트 데이터 초기화 시작 ==========");

        // 데이터가 이미 있으면 스킵
        if (userRepository.count() > 0) {
            log.info("데이터가 이미 존재하여 초기화를 건너뜁니다.");
            return;
        }

        initUserData();
        initStudyRoomData();
        initProblemData();

        log.info("========== 개발용 테스트 데이터 초기화 완료 ==========");
    }

    private void initUserData() {
        log.info("사용자 정보를 초기화합니다.");

        User user1 = User.builder()
                .email("test1@example.com")
                .password("password123")
                .username("테스트유저1")
                .receiveNotifications(true)
                .build();

        User user2 = User.builder()
                .email("test2@example.com")
                .password("password123")
                .username("테스트유저2")
                .receiveNotifications(false)
                .build();

        userRepository.saveAll(List.of(user1, user2));
        log.info("사용자 2명 초기화 완료");
    }

    private void initStudyRoomData() {
        log.info("스터디룸 정보를 초기화합니다.");

        List<User> users = userRepository.findAll();
        User user1 = users.get(0);
        User user2 = users.get(1);

        // 개인 스터디룸
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(user1)
                .roomType(RoomType.PERSONAL)
                .name("테스트유저1의 개인 공부방")
                .description("개인 학습용 스터디룸")
                .category("개인")
                .build();

        // 그룹 스터디룸
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(user1)
                .roomType(RoomType.GROUP)
                .name("Java 스터디")
                .description("Java 공부 그룹")
                .category("프로그래밍")
                .joinCode("JAVA2024")
                .build();

        studyRoomRepository.saveAll(List.of(personalRoom, groupRoom));

        // 그룹룸에 user2 멤버 추가
        StudyRoomMember member = StudyRoomMember.builder()
                .studyRoom(groupRoom)
                .user(user2)
                .build();
        studyRoomMemberRepository.save(member);

        log.info("스터디룸 2개 초기화 완료 (개인방 1개, 그룹방 1개)");
    }

    private void initProblemData() {
        log.info("문제 및 복습 상태를 초기화합니다.");

        List<User> users = userRepository.findAll();
        User user1 = users.get(0);
        User user2 = users.get(1);

        List<StudyRoom> rooms = studyRoomRepository.findAll();
        StudyRoom personalRoom = rooms.get(0);
        StudyRoom groupRoom = rooms.get(1);

        LocalDate today = LocalDate.now();

        // Problem 1: 오늘의 복습 문제 (GATE_1, 첫 시도 대상) - 객관식
        Problem problem1 = createMultipleChoiceProblem(
                user1, personalRoom,
                "Java의 접근 제어자 중 가장 넓은 범위는?",
                "public은 모든 클래스에서 접근 가능합니다.",
                3
        );
        problemRepository.save(problem1);

        ProblemReviewState state1 = ProblemReviewState.builder()
                .user(user1)
                .problem(problem1)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today)
                .reviewCount(0)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_1)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(state1);

        // 객관식 선택지 생성
        createChoices(problem1, List.of("private", "protected", "default", "public"));

        // Problem 2: 오늘의 복습 문제 (GATE_2, 첫 시도 대상) - OX
        Problem problem2 = createTrueFalseProblem(
                user1, personalRoom,
                "Java는 다중 상속을 지원한다.",
                "Java는 인터페이스를 통한 다중 구현만 지원하며, 클래스 다중 상속은 지원하지 않습니다.",
                false
        );
        problemRepository.save(problem2);

        ProblemReviewState state2 = ProblemReviewState.builder()
                .user(user1)
                .problem(problem2)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(1)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(state2);

        // Problem 3: 오늘의 복습 문제 (이미 첫 시도 완료) - 단답형
        Problem problem3 = createShortAnswerProblem(
                user1, personalRoom,
                "Java의 가비지 컬렉션을 담당하는 JVM 구성 요소는?",
                "Garbage Collector가 Heap 영역의 사용하지 않는 객체를 자동으로 정리합니다.",
                "Garbage Collector"
        );
        problemRepository.save(problem3);

        ProblemReviewState state3 = ProblemReviewState.builder()
                .user(user1)
                .problem(problem3)
                .gate(ReviewGate.GATE_2)
                .nextReviewDate(today)
                .reviewCount(2)
                .todayReviewIncludedDate(today)
                .todayReviewIncludedGate(ReviewGate.GATE_2)
                .todayReviewFirstAttemptDate(today)
                .build();
        problemReviewStateRepository.save(state3);

        // 첫 시도 기록 생성
        ProblemAttempt attempt3 = ProblemAttempt.builder()
                .user(user1)
                .problem(problem3)
                .submittedAnswerText("Garbage Collector")
                .isCorrect(true)
                .build();
        problemAttemptRepository.save(attempt3);

        // Problem 4: 비복습 문제 (미래) - 서술형
        Problem problem4 = createEssayProblem(
                user1, personalRoom,
                "Spring Framework의 IoC(Inversion of Control) 개념을 설명하시오.",
                "IoC는 객체의 생성과 의존성 관리를 개발자가 아닌 프레임워크(Spring Container)가 담당하는 설계 원칙입니다.",
                "IoC는 제어의 역전을 의미하며, 객체의 생성과 생명주기 관리를 Spring Container가 담당합니다."
        );
        problemRepository.save(problem4);

        ProblemReviewState state4 = ProblemReviewState.builder()
                .user(user1)
                .problem(problem4)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today.plusDays(3))
                .reviewCount(0)
                .todayReviewIncludedDate(null)
                .todayReviewIncludedGate(null)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(state4);

        // 서술형 키워드 생성
        createKeywords(problem4, List.of("제어의 역전", "Spring Container", "객체 생성", "생명주기"));

        // Problem 5: 비복습 문제 (졸업) - 객관식
        Problem problem5 = createMultipleChoiceProblem(
                user1, personalRoom,
                "HTTP 상태 코드 중 성공을 나타내는 코드는?",
                "200번대는 성공, 300번대는 리다이렉션, 400번대는 클라이언트 오류, 500번대는 서버 오류를 나타냅니다.",
                0
        );
        problemRepository.save(problem5);

        ProblemReviewState state5 = ProblemReviewState.builder()
                .user(user1)
                .problem(problem5)
                .gate(ReviewGate.GRADUATED)
                .nextReviewDate(null)
                .reviewCount(3)
                .todayReviewIncludedDate(null)
                .todayReviewIncludedGate(null)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(state5);

        createChoices(problem5, List.of("200", "300", "400", "500"));

        // Problem 6: 그룹방 타인 문제 (user2가 생성, user1의 ReviewState 없음)
        Problem problem6 = createMultipleChoiceProblem(
                user2, groupRoom,
                "데이터베이스의 ACID 특성 중 'A'가 의미하는 것은?",
                "Atomicity(원자성)는 트랜잭션의 연산이 모두 성공하거나 모두 실패해야 함을 의미합니다.",
                0
        );
        problemRepository.save(problem6);

        // user2의 ReviewState만 생성 (문제 생성자)
        ProblemReviewState state6 = ProblemReviewState.builder()
                .user(user2)
                .problem(problem6)
                .gate(ReviewGate.GATE_1)
                .nextReviewDate(today.plusDays(1))
                .reviewCount(0)
                .todayReviewIncludedDate(null)
                .todayReviewIncludedGate(null)
                .todayReviewFirstAttemptDate(null)
                .build();
        problemReviewStateRepository.save(state6);

        createChoices(problem6, List.of("Atomicity", "Consistency", "Isolation", "Durability"));

        log.info("문제 6개 및 복습 상태 초기화 완료");
    }

    // 객관식 문제 생성 헬퍼 메서드
    private Problem createMultipleChoiceProblem(User creator, StudyRoom room,
                                                 String question, String explanation,
                                                 int correctIndex) {
        return Problem.builder()
                .creator(creator)
                .studyRoom(room)
                .problemType(ProblemType.MULTIPLE_CHOICE)
                .question(question)
                .explanation(explanation)
                .correctChoiceIndex(correctIndex)
                .build();
    }

    // OX 문제 생성 헬퍼 메서드
    private Problem createTrueFalseProblem(User creator, StudyRoom room,
                                           String question, String explanation,
                                           boolean answer) {
        return Problem.builder()
                .creator(creator)
                .studyRoom(room)
                .problemType(ProblemType.TRUE_FALSE)
                .question(question)
                .explanation(explanation)
                .answerBoolean(answer)
                .build();
    }

    // 단답형 문제 생성 헬퍼 메서드
    private Problem createShortAnswerProblem(User creator, StudyRoom room,
                                             String question, String explanation,
                                             String answer) {
        return Problem.builder()
                .creator(creator)
                .studyRoom(room)
                .problemType(ProblemType.SHORT_ANSWER)
                .question(question)
                .explanation(explanation)
                .answerText(answer)
                .build();
    }

    // 서술형 문제 생성 헬퍼 메서드
    private Problem createEssayProblem(User creator, StudyRoom room,
                                       String question, String explanation,
                                       String modelAnswer) {
        return Problem.builder()
                .creator(creator)
                .studyRoom(room)
                .problemType(ProblemType.ESSAY)
                .question(question)
                .explanation(explanation)
                .modelAnswerText(modelAnswer)
                .build();
    }

    // 객관식 선택지 생성 헬퍼 메서드
    private void createChoices(Problem problem, List<String> choiceTexts) {
        for (int i = 0; i < choiceTexts.size(); i++) {
            ProblemChoice choice = ProblemChoice.builder()
                    .problem(problem)
                    .choiceOrder(i)
                    .choiceText(choiceTexts.get(i))
                    .build();
            problemChoiceRepository.save(choice);
        }
    }

    // 서술형 키워드 생성 헬퍼 메서드
    private void createKeywords(Problem problem, List<String> keywords) {
        for (String keyword : keywords) {
            ProblemKeyword kw = ProblemKeyword.builder()
                    .problem(problem)
                    .keyword(keyword)
                    .build();
            problemKeywordRepository.save(kw);
        }
    }
}
