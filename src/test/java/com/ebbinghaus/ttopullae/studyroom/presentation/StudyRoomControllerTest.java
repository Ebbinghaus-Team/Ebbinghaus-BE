package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomMemberRepository;
import com.ebbinghaus.ttopullae.studyroom.domain.repository.StudyRoomRepository;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(testUser);

        // JWT 토큰 생성
        accessToken = jwtTokenProvider.generateToken(testUser.getUserId());
    }

    @Test
    @DisplayName("개인 공부방 생성 성공 - JWT 인증 사용")
    void createPersonalRoom_Success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "자바 스터디");
        request.put("description", "자바 개념 정리");
        request.put("category", "프로그래밍");

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .cookie(new Cookie("accessToken", accessToken)) // JWT 쿠키 추가
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
    @DisplayName("그룹 스터디 생성 성공 - JWT 인증 사용")
    void createGroupRoom_Success() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "알고리즘 스터디");
        request.put("description", "매일 알고리즘 풀이");
        request.put("category", "알고리즘");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group")
                        .cookie(new Cookie("accessToken", accessToken)) // JWT 쿠키 추가
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
    @DisplayName("개인 공부방 생성 실패 - JWT 토큰 없음 (인증 실패)")
    void createPersonalRoom_Fail_NoToken() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "자바 스터디");
        request.put("description", "자바 개념 정리");
        request.put("category", "프로그래밍");

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        // 쿠키 없이 요청
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("토큰을 찾을 수 없음"));
    }

    @Test
    @DisplayName("개인 공부방 생성 실패 - 잘못된 JWT 토큰")
    void createPersonalRoom_Fail_InvalidToken() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", "자바 스터디");
        request.put("description", "자바 개념 정리");
        request.put("category", "프로그래밍");

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .cookie(new Cookie("accessToken", "invalid.jwt.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("유효하지 않은 토큰"));
    }

    @Test
    @DisplayName("개인 공부방 생성 실패 - 공부방 이름 누락")
    void createPersonalRoom_Fail_MissingName() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", ""); // 빈 이름
        request.put("description", "자바 개념 정리");
        request.put("category", "프로그래밍");

        // when & then
        mockMvc.perform(post("/api/study-rooms/personal")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("그룹 스터디 생성 실패 - 그룹 스터디 이름 누락")
    void createGroupRoom_Fail_MissingName() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("name", ""); // 빈 이름
        request.put("description", "매일 알고리즘 풀이");
        request.put("category", "알고리즘");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}