package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.GroupRoomCreateRequest;
import com.ebbinghaus.ttopullae.studyroom.presentation.dto.PersonalRoomCreateRequest;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRoomRepository studyRoomRepository;

    @Autowired
    private StudyRoomMemberRepository studyRoomMemberRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("개인 공부방 생성 성공")
    void createPersonalRoom_Success() throws Exception {
        // given
        PersonalRoomCreateRequest request = new PersonalRoomCreateRequest(
                testUser.getUserId(),
                "자바 스터디",
                "자바 개념 정리",
                "프로그래밍"
        );

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyRoomId").exists())
                .andExpect(jsonPath("$.name").value("자바 스터디"))
                .andExpect(jsonPath("$.category").value("프로그래밍"))
                .andExpect(jsonPath("$.description").value("자바 개념 정리"))
                .andExpect(jsonPath("$.createdAt").exists());

        // 데이터베이스 검증
        StudyRoom savedRoom = studyRoomRepository.findAll().get(0);
        assertThat(savedRoom.getName()).isEqualTo("자바 스터디");
        assertThat(savedRoom.getRoomType()).isEqualTo(RoomType.PERSONAL);
        assertThat(savedRoom.getJoinCode()).isNull(); // 개인방은 참여 코드가 없음
        assertThat(savedRoom.getOwner().getUserId()).isEqualTo(testUser.getUserId());
    }

    @Test
    @DisplayName("그룹 스터디 생성 성공")
    void createGroupRoom_Success() throws Exception {
        // given
        GroupRoomCreateRequest request = new GroupRoomCreateRequest(
                testUser.getUserId(),
                "알고리즘 스터디",
                "매일 알고리즘 풀이",
                "알고리즘"
        );

        // when & then
        mockMvc.perform(post("/api/study-rooms/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyRoomId").exists())
                .andExpect(jsonPath("$.name").value("알고리즘 스터디"))
                .andExpect(jsonPath("$.category").value("알고리즘"))
                .andExpect(jsonPath("$.description").value("매일 알고리즘 풀이"))
                .andExpect(jsonPath("$.joinCode").exists()) // 그룹방은 참여 코드 필수
                .andExpect(jsonPath("$.createdAt").exists());

        // 데이터베이스 검증
        StudyRoom savedRoom = studyRoomRepository.findAll().get(0);
        assertThat(savedRoom.getName()).isEqualTo("알고리즘 스터디");
        assertThat(savedRoom.getRoomType()).isEqualTo(RoomType.GROUP);
        assertThat(savedRoom.getJoinCode()).isNotNull();
        assertThat(savedRoom.getJoinCode()).hasSize(8); // 8자리 참여 코드
        assertThat(savedRoom.getOwner().getUserId()).isEqualTo(testUser.getUserId());

        // 방장이 그룹 멤버로 자동 등록되었는지 검증
        assertThat(studyRoomMemberRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("개인 공부방 생성 실패 - 사용자 ID 누락")
    void createPersonalRoom_Fail_MissingUserId() throws Exception {
        // given
        PersonalRoomCreateRequest request = new PersonalRoomCreateRequest(
                null, // userId 누락
                "자바 스터디",
                "자바 개념 정리",
                "프로그래밍"
        );

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("개인 공부방 생성 실패 - 공부방 이름 누락")
    void createPersonalRoom_Fail_MissingName() throws Exception {
        // given
        PersonalRoomCreateRequest request = new PersonalRoomCreateRequest(
                testUser.getUserId(),
                "", // 빈 이름
                "자바 개념 정리",
                "프로그래밍"
        );

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("개인 공부방 생성 실패 - 존재하지 않는 사용자")
    void createPersonalRoom_Fail_UserNotFound() throws Exception {
        // given
        PersonalRoomCreateRequest request = new PersonalRoomCreateRequest(
                99999L, // 존재하지 않는 userId
                "자바 스터디",
                "자바 개념 정리",
                "프로그래밍"
        );

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("사용자를 찾을 수 없음"));
    }

    @Test
    @DisplayName("그룹 스터디 생성 실패 - 그룹 스터디 이름 누락")
    void createGroupRoom_Fail_MissingName() throws Exception {
        // given
        GroupRoomCreateRequest request = new GroupRoomCreateRequest(
                testUser.getUserId(),
                "", // 빈 이름
                "매일 알고리즘 풀이",
                "알고리즘"
        );

        // when & then
        mockMvc.perform(post("/api/study-rooms/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
