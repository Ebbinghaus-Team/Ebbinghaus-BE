package com.ebbinghaus.ttopullae.studyroom.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.util.JoinCodeGenerator;
import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemAttempt;
import com.ebbinghaus.ttopullae.problem.domain.ProblemReviewState;
import com.ebbinghaus.ttopullae.problem.domain.ReviewGate;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemAttemptRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinCommand;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomListResult.GroupRoomInfo;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomProblemListCommand;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomProblemListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult.PersonalRoomInfo;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomProblemListCommand;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomProblemListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateCommand;
import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateResult;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.studyroom.exception.StudyRoomException;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyRoomService {

    private static final int MAX_JOIN_CODE_ATTEMPTS = 10;

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomMemberRepository studyRoomMemberRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ProblemReviewStateRepository problemReviewStateRepository;
    private final ProblemAttemptRepository problemAttemptRepository;

    /**
     * 개인 공부방을 생성합니다.
     */
    @Transactional
    public StudyRoomCreateResult createPersonalRoom(StudyRoomCreateCommand command) {
        User owner = findUserById(command.userId());

        StudyRoom personalRoom = StudyRoom.builder()
                .owner(owner)
                .roomType(RoomType.PERSONAL)
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .joinCode(null) // 개인방은 참여 코드가 없음
                .build();

        StudyRoom savedRoom = studyRoomRepository.save(personalRoom);

        return StudyRoomCreateResult.from(savedRoom);
    }

    /**
     * 그룹 스터디를 생성합니다.
     * 생성 시 고유한 참여 코드를 자동으로 발급합니다.
     */
    @Transactional
    public StudyRoomCreateResult createGroupRoom(StudyRoomCreateCommand command) {
        User owner = findUserById(command.userId());

        String uniqueJoinCode = generateUniqueJoinCode();

        StudyRoom groupRoom = StudyRoom.builder()
                .owner(owner)
                .roomType(RoomType.GROUP)
                .name(command.name())
                .description(command.description())
                .category(command.category())
                .joinCode(uniqueJoinCode)
                .build();

        StudyRoom savedRoom = studyRoomRepository.save(groupRoom);

        // 방장을 그룹 멤버로 자동 등록
        StudyRoomMember ownerMembership = StudyRoomMember.builder()
                .user(owner)
                .studyRoom(savedRoom)
                .active(true)
                .build();

        studyRoomMemberRepository.save(ownerMembership);

        return StudyRoomCreateResult.from(savedRoom);
    }

    /**
     * 참여 코드로 그룹 스터디에 참여합니다.
     */
    @Transactional
    public GroupRoomJoinResult joinGroupRoom(GroupRoomJoinCommand command) {
        User user = findUserById(command.userId());

        // 참여 코드로 스터디룸 조회
        StudyRoom studyRoom = studyRoomRepository.findByJoinCode(command.joinCode())
                .orElseThrow(() -> new ApplicationException(StudyRoomException.STUDY_ROOM_NOT_FOUND));

        // 그룹방인지 확인
        if (!studyRoom.isGroupRoom()) {
            throw new ApplicationException(StudyRoomException.NOT_GROUP_ROOM);
        }

        // 중복 참여 확인
        if (studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(user, studyRoom, true)) {
            throw new ApplicationException(StudyRoomException.ALREADY_JOINED);
        }

        // StudyRoomMember 생성 및 저장
        StudyRoomMember member = StudyRoomMember.builder()
                .user(user)
                .studyRoom(studyRoom)
                .active(true)
                .build();

        StudyRoomMember savedMember = studyRoomMemberRepository.save(member);

        return GroupRoomJoinResult.from(savedMember);
    }

    /**
     * 사용자의 개인 공부방 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public PersonalRoomListResult getPersonalRooms(Long userId) {
        User user = findUserById(userId);

        // 개인방 목록 조회
        List<StudyRoom> personalRooms = studyRoomRepository.findAllByOwnerAndRoomType(user, RoomType.PERSONAL);

        // 각 방별 문제 수 및 완료 문제 수 집계
        List<PersonalRoomInfo> roomInfos = personalRooms.stream()
                .map(studyRoom -> {
                    int totalProblems = problemRepository.countByStudyRoom(studyRoom);
                    int graduatedProblems = problemReviewStateRepository.countByUserAndProblem_StudyRoomAndGate(
                            user, studyRoom, ReviewGate.GRADUATED
                    );

                    return new PersonalRoomInfo(
                            studyRoom.getStudyRoomId(),
                            studyRoom.getName(),
                            studyRoom.getCategory(),
                            studyRoom.getDescription(),
                            totalProblems,
                            graduatedProblems,
                            studyRoom.getCreatedAt()
                    );
                })
                .toList();

        return new PersonalRoomListResult(roomInfos);
    }

    /**
     * 사용자가 속한 그룹 스터디 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public GroupRoomListResult getGroupRooms(Long userId) {
        User user = findUserById(userId);

        // 활성 멤버십 조회
        List<StudyRoomMember> memberships = studyRoomMemberRepository.findAllByUserAndActive(user, true);

        // 각 그룹별 문제 수 및 완료 문제 수 집계
        List<GroupRoomInfo> roomInfos = memberships.stream()
                .map(member -> {
                    StudyRoom studyRoom = member.getStudyRoom();
                    int totalProblems = problemRepository.countByStudyRoom(studyRoom);
                    int graduatedProblems = problemReviewStateRepository.countByUserAndProblem_StudyRoomAndGate(
                            user, studyRoom, ReviewGate.GRADUATED
                    );

                    return new GroupRoomInfo(
                            studyRoom.getStudyRoomId(),
                            studyRoom.getName(),
                            studyRoom.getCategory(),
                            studyRoom.getDescription(),
                            studyRoom.getJoinCode(),
                            totalProblems,
                            graduatedProblems,
                            member.getCreatedAt() // 참여일
                    );
                })
                .toList();

        return new GroupRoomListResult(roomInfos);
    }

    /**
     * 개인 공부방의 문제 목록을 조회합니다.
     * 필터링 옵션(ALL/GATE_1/GATE_2/GRADUATED)을 지원합니다.
     *
     * @param command 문제 목록 조회 요청 Command
     * @return 문제 목록 결과
     */
    @Transactional(readOnly = true)
    public PersonalRoomProblemListResult getPersonalRoomProblems(
            PersonalRoomProblemListCommand command
    ) {
        User user = findUserById(command.userId());
        StudyRoom studyRoom = findStudyRoomById(command.studyRoomId());
        validatePersonalRoomOwnership(studyRoom, user.getUserId());

        ReviewGate targetGate = parseFilter(command.filter());
        List<Problem> problems = problemRepository.findPersonalRoomProblemsWithReviewState(
                command.studyRoomId(), user.getUserId(), targetGate);
        Map<Long, ProblemAttempt> attemptMap = findLatestAttempts(problems, user.getUserId());

        return PersonalRoomProblemListResult.of(studyRoom, problems, attemptMap, user.getUserId());
    }

    /**
     * 그룹 공부방의 문제 목록을 조회합니다.
     * 필터링 옵션(ALL/NOT_IN_REVIEW/GATE_1/GATE_2/GRADUATED)을 지원합니다.
     *
     * @param command 문제 목록 조회 요청 Command
     * @return 문제 목록 결과 (isMyProblem, creatorName 포함)
     */
    @Transactional(readOnly = true)
    public GroupRoomProblemListResult getGroupRoomProblems(
            GroupRoomProblemListCommand command
    ) {
        // 1. 사용자 검증
        User user = findUserById(command.userId());

        // 2. 스터디룸 검증 및 조회
        StudyRoom studyRoom = findStudyRoomById(command.studyRoomId());

        // 3. 그룹 멤버십 검증
        validateGroupRoomMembership(studyRoom, user);

        // 4. 필터 파라미터 변환 (filter 문자열 → Boolean 플래그)
        FilterParams filterParams = convertFilterToParams(command.filter());

        // 5. 문제 목록 조회 (creator, reviewStates fetch join)
        List<Problem> problems = problemRepository.findGroupRoomProblemsWithReviewStateAndCreator(
                command.studyRoomId(),
                user.getUserId(),
                filterParams.includeAll(),
                filterParams.includeNotInReview(),
                filterParams.targetGate()
        );

        // 6. 최근 시도 기록 조회
        Map<Long, ProblemAttempt> attemptMap = findLatestAttempts(problems, user.getUserId());

        // 7. DTO 변환 및 반환
        return GroupRoomProblemListResult.of(studyRoom, problems, attemptMap, user.getUserId());
    }

    /**
     * 스터디룸 ID로 StudyRoom 엔티티를 조회합니다.
     */
    private StudyRoom findStudyRoomById(Long studyRoomId) {
        return studyRoomRepository.findById(studyRoomId)
                .orElseThrow(() -> new ApplicationException(StudyRoomException.STUDY_ROOM_NOT_FOUND));
    }

    /**
     * 개인방 소유자 권한을 검증합니다.
     * 개인방 타입 확인 및 소유자 검증을 수행합니다.
     */
    private void validatePersonalRoomOwnership(StudyRoom studyRoom, Long userId) {
        if (studyRoom.getRoomType() != RoomType.PERSONAL) {
            throw new ApplicationException(StudyRoomException.NOT_PERSONAL_ROOM);
        }
        if (!studyRoom.getOwner().getUserId().equals(userId)) {
            throw new ApplicationException(StudyRoomException.NOT_ROOM_OWNER);
        }
    }

    /**
     * 필터 문자열을 ReviewGate로 변환합니다.
     * "ALL"인 경우 null을 반환하여 전체 조회를 수행합니다.
     */
    private ReviewGate parseFilter(String filter) {
        return "ALL".equals(filter) ? null : ReviewGate.valueOf(filter);
    }

    /**
     * 문제 목록에 대한 사용자의 최근 시도 기록을 조회하여 Map으로 반환합니다.
     */
    private Map<Long, ProblemAttempt> findLatestAttempts(List<Problem> problems, Long userId) {
        List<Long> problemIds = problems.stream()
                .map(Problem::getProblemId)
                .toList();

        if (problemIds.isEmpty()) {
            return Map.of();
        }

        List<ProblemAttempt> latestAttempts =
                problemAttemptRepository.findLatestAttemptsByUserAndProblems(userId, problemIds);

        return latestAttempts.stream()
                .collect(Collectors.toMap(
                        attempt -> attempt.getProblem().getProblemId(),
                        attempt -> attempt
                ));
    }

    /**
     * 고유한 참여 코드를 생성합니다.
     * 최대 10회까지 재시도하며, 모두 실패 시 예외를 발생시킵니다.
     */
    private String generateUniqueJoinCode() {
        for (int attempt = 0; attempt < MAX_JOIN_CODE_ATTEMPTS; attempt++) {
            String joinCode = JoinCodeGenerator.generateCode();
            if (!studyRoomRepository.existsByJoinCode(joinCode)) {
                return joinCode;
            }
        }
        throw new ApplicationException(StudyRoomException.JOIN_CODE_GENERATION_FAILED);
    }

    /**
     * 사용자 ID로 User 엔티티를 조회합니다.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserException.USER_NOT_FOUND));
    }

    /**
     * 그룹방 멤버십을 검증합니다.
     * 그룹방 타입 확인 및 활성 멤버 여부를 확인합니다.
     */
    private void validateGroupRoomMembership(StudyRoom studyRoom, User user) {
        // 그룹방인지 확인
        if (studyRoom.getRoomType() != RoomType.GROUP) {
            throw new ApplicationException(StudyRoomException.NOT_GROUP_ROOM);
        }

        // 활성 멤버인지 확인
        if (!studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(user, studyRoom, true)) {
            throw new ApplicationException(StudyRoomException.NOT_GROUP_MEMBER);
        }
    }

    /**
     * 필터 문자열을 Boolean 플래그로 변환합니다.
     *
     * @param filter 필터 문자열 (ALL, NOT_IN_REVIEW, GATE_1, GATE_2, GRADUATED)
     * @return FilterParams (includeAll, includeNotInReview, targetGate)
     */
    private FilterParams convertFilterToParams(String filter) {
        return switch (filter) {
            case "ALL" -> new FilterParams(true, false, null);
            case "NOT_IN_REVIEW" -> new FilterParams(false, true, null);
            case "GATE_1" -> new FilterParams(false, false, ReviewGate.GATE_1);
            case "GATE_2" -> new FilterParams(false, false, ReviewGate.GATE_2);
            case "GRADUATED" -> new FilterParams(false, false, ReviewGate.GRADUATED);
            default -> throw new ApplicationException(StudyRoomException.INVALID_FILTER);
        };
    }

    /**
     * 필터 파라미터를 담는 내부 record
     */
    private record FilterParams(
        boolean includeAll,
        boolean includeNotInReview,
        ReviewGate targetGate
    ) {}
}
