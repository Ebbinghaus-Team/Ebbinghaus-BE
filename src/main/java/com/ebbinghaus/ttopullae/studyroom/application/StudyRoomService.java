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
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult.PersonalRoomInfo;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomProblemListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomProblemListResult.ProblemInfo;
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
     * 단일 쿼리로 문제 + 생성자 + 복습 상태를 Fetch Join으로 조회합니다.
     *
     * @param userId 사용자 ID
     * @param studyRoomId 스터디룸 ID
     * @param filter 필터 타입 (ALL, GATE_1, GATE_2, GRADUATED)
     * @return 문제 목록 결과
     */
    @Transactional(readOnly = true)
    public PersonalRoomProblemListResult getPersonalRoomProblems(
            Long userId,
            Long studyRoomId,
            String filter
    ) {
        User user = findUserById(userId);

        // 스터디룸 조회
        StudyRoom studyRoom = studyRoomRepository.findById(studyRoomId)
                .orElseThrow(() -> new ApplicationException(StudyRoomException.STUDY_ROOM_NOT_FOUND));

        // 개인방인지 확인
        if (studyRoom.getRoomType() != RoomType.PERSONAL) {
            throw new ApplicationException(StudyRoomException.NOT_PERSONAL_ROOM);
        }

        // 소유자 권한 확인
        if (!studyRoom.getOwner().getUserId().equals(userId)) {
            throw new ApplicationException(StudyRoomException.NOT_ROOM_OWNER);
        }

        // 동적 필터링을 위한 ReviewGate 변환 (ALL이면 null로 전달)
        ReviewGate targetGate = "ALL".equals(filter) ? null : ReviewGate.valueOf(filter);

        // 단일 쿼리로 문제 + 생성자 + 복습 상태 조회 (Fetch Join)
        List<Problem> problems = problemRepository.findPersonalRoomProblemsWithReviewState(
                studyRoomId,
                userId,
                targetGate
        );

        // 문제 ID 목록 추출 (마지막 복습일 조회용)
        List<Long> problemIds = problems.stream()
                .map(Problem::getProblemId)
                .toList();

        // 마지막 복습일 조회 (Batch 쿼리)
        List<ProblemAttempt> latestAttempts = problemIds.isEmpty()
                ? List.of()
                : problemAttemptRepository.findLatestAttemptsByUserAndProblems(userId, problemIds);

        // 마지막 풀이 기록 맵 생성 (problemId -> ProblemAttempt)
        Map<Long, ProblemAttempt> attemptMap = latestAttempts.stream()
                .collect(Collectors.toMap(
                        attempt -> attempt.getProblem().getProblemId(),
                        attempt -> attempt
                ));

        // DTO 변환
        List<ProblemInfo> problemInfos = problems.stream()
                .map(problem -> {
                    // reviewStates에서 현재 사용자의 복습 상태 찾기
                    ProblemReviewState userReviewState = problem.getReviewStates().stream()
                            .filter(rs -> rs.getUser().getUserId().equals(userId))
                            .findFirst()
                            .orElse(null);

                    // 복습 상태가 없으면 null (실제로는 발생하지 않음 - 쿼리에서 필터링됨)
                    if (userReviewState == null) {
                        return null;
                    }

                    ProblemAttempt latestAttempt = attemptMap.get(problem.getProblemId());

                    return new ProblemInfo(
                            problem.getProblemId(),
                            problem.getQuestion(),
                            problem.getProblemType(),
                            userReviewState.getGate(),
                            problem.getCreatedAt(),
                            latestAttempt != null ? latestAttempt.getCreatedAt() : null,
                            userReviewState.getReviewCount()
                    );
                })
                .filter(info -> info != null)
                .toList();

        return new PersonalRoomProblemListResult(
                studyRoomId,
                studyRoom.getName(),
                problemInfos,
                problemInfos.size()
        );
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
}
