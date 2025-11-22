package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomJoinResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.GroupRoomListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.PersonalRoomProblemListResult;
import com.ebbinghaus.ttopullae.studyroom.application.dto.StudyRoomCreateResult;
import com.ebbinghaus.ttopullae.studyroom.application.StudyRoomService;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomJoinRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomJoinResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomListResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomListResponse;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomProblemListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController implements StudyRoomControllerDocs {

    private final StudyRoomService studyRoomService;

    /**
     * 개인 공부방을 생성합니다.
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @param request 개인 공부방 생성 요청
     * @return 생성된 개인 공부방 정보
     */
    @PostMapping("/personal")
    public ResponseEntity<PersonalRoomCreateResponse> createPersonalRoom(
            @LoginUser Long userId,
            @Valid @RequestBody PersonalRoomCreateRequest request
    ) {
        StudyRoomCreateResult result = studyRoomService.createPersonalRoom(request.toCommand(userId));
        PersonalRoomCreateResponse response = PersonalRoomCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 그룹 스터디를 생성합니다.
     * 생성 시 고유한 참여 코드가 자동으로 발급됩니다.
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @param request 그룹 스터디 생성 요청
     * @return 생성된 그룹 스터디 정보 (참여 코드 포함)
     */
    @PostMapping("/group")
    public ResponseEntity<GroupRoomCreateResponse> createGroupRoom(
            @LoginUser Long userId,
            @Valid @RequestBody GroupRoomCreateRequest request
    ) {
        StudyRoomCreateResult result = studyRoomService.createGroupRoom(request.toCommand(userId));
        GroupRoomCreateResponse response = GroupRoomCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 참여 코드로 그룹 스터디에 참여합니다.
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @param request 그룹 스터디 참여 요청 (참여 코드 포함)
     * @return 참여한 그룹 스터디 정보
     */
    @PostMapping("/group/join")
    public ResponseEntity<GroupRoomJoinResponse> joinGroupRoom(
            @LoginUser Long userId,
            @Valid @RequestBody GroupRoomJoinRequest request
    ) {
        GroupRoomJoinResult result = studyRoomService.joinGroupRoom(request.toCommand(userId));
        GroupRoomJoinResponse response = GroupRoomJoinResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자의 개인 공부방 목록을 조회합니다.
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @return 개인 공부방 목록 (문제 수, 완료 문제 수 포함)
     */
    @GetMapping("/personal")
    public ResponseEntity<PersonalRoomListResponse> getPersonalRooms(
            @LoginUser Long userId
    ) {
        PersonalRoomListResult result = studyRoomService.getPersonalRooms(userId);
        PersonalRoomListResponse response = PersonalRoomListResponse.from(result);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자가 속한 그룹 스터디 목록을 조회합니다.
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @return 그룹 스터디 목록 (문제 수, 완료 문제 수 포함)
     */
    @GetMapping("/group")
    public ResponseEntity<GroupRoomListResponse> getGroupRooms(
            @LoginUser Long userId
    ) {
        GroupRoomListResult result = studyRoomService.getGroupRooms(userId);
        GroupRoomListResponse response = GroupRoomListResponse.from(result);
        return ResponseEntity.ok(response);
    }

    /**
     * 개인 공부방의 문제 목록을 조회합니다.
     * 필터링 옵션(ALL/GATE_1/GATE_2/GRADUATED)을 지원합니다.
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @param studyRoomId 스터디룸 ID
     * @param filter 필터 타입 (기본값: ALL)
     * @return 개인 공부방 문제 목록
     */
    @GetMapping("/personal/{studyRoomId}/problems")
    public ResponseEntity<PersonalRoomProblemListResponse> getPersonalRoomProblems(
            @LoginUser Long userId,
            @PathVariable Long studyRoomId,
            @RequestParam(defaultValue = "ALL") String filter
    ) {
        PersonalRoomProblemListResult result = studyRoomService.getPersonalRoomProblems(
                userId,
                studyRoomId,
                filter
        );
        PersonalRoomProblemListResponse response = PersonalRoomProblemListResponse.from(result);
        return ResponseEntity.ok(response);
    }
}
