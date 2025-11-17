package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.studyroom.application.StudyRoomCreateResult;
import com.ebbinghaus.ttopullae.studyroom.application.StudyRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    /**
     * 개인 공부방을 생성합니다.
     *
     * @param request 개인 공부방 생성 요청
     * @return 생성된 개인 공부방 정보
     */
    @PostMapping("/personal")
    public ResponseEntity<PersonalRoomCreateResponse> createPersonalRoom(
            @Valid @RequestBody PersonalRoomCreateRequest request
    ) {
        StudyRoomCreateResult result = studyRoomService.createPersonalRoom(request.toCommand());
        PersonalRoomCreateResponse response = PersonalRoomCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 그룹 스터디를 생성합니다.
     * 생성 시 고유한 참여 코드가 자동으로 발급됩니다.
     *
     * @param request 그룹 스터디 생성 요청
     * @return 생성된 그룹 스터디 정보 (참여 코드 포함)
     */
    @PostMapping("/group")
    public ResponseEntity<GroupRoomCreateResponse> createGroupRoom(
            @Valid @RequestBody GroupRoomCreateRequest request
    ) {
        StudyRoomCreateResult result = studyRoomService.createGroupRoom(request.toCommand());
        GroupRoomCreateResponse response = GroupRoomCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}