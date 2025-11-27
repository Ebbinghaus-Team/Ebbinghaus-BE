package com.ebbinghaus.ttopullae.studyroom.presentation;

import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.studyroom.domain.RoomType;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoom;
import com.ebbinghaus.ttopullae.studyroom.domain.StudyRoomMember;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(jsonPath("$.title").value("인증되지 않은 요청"));
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

    // ==================== 그룹 스터디 참여 API 테스트 ====================

    @Test
    @DisplayName("그룹 스터디 참여 성공")
    void joinGroupRoom_Success() throws Exception {
        // given
        // 그룹 스터디 생성 (방장은 다른 사용자)
        User owner = User.builder()
                .email("owner@example.com")
                .password("password123")
                .username("방장")
                .receiveNotifications(true)
                .build();
        userRepository.save(owner);

        StudyRoom groupRoom = StudyRoom.builder()
                .owner(owner)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘 풀이")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // 방장을 그룹 멤버로 등록
        StudyRoomMember ownerMembership = StudyRoomMember.builder()
                .user(owner)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(ownerMembership);

        Map<String, String> request = new HashMap<>();
        request.put("joinCode", "ABC12345");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group/join")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studyRoomId").value(groupRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.name").value("알고리즘 스터디"))
                .andExpect(jsonPath("$.category").value("알고리즘"))
                .andExpect(jsonPath("$.description").value("매일 알고리즘 풀이"))
                .andExpect(jsonPath("$.joinedAt").exists());

        // 데이터베이스 검증 - 멤버가 추가되었는지 확인
        assertThat(studyRoomMemberRepository.count()).isEqualTo(2); // 방장 + 새 멤버
        assertThat(studyRoomMemberRepository.existsByUserAndStudyRoomAndActive(testUser, groupRoom, true)).isTrue();
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - JWT 토큰 없음")
    void joinGroupRoom_Fail_NoToken() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("joinCode", "ABC12345");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - 존재하지 않는 참여 코드")
    void joinGroupRoom_Fail_InvalidJoinCode() throws Exception {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("joinCode", "INVALID1");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group/join")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("스터디룸을 찾을 수 없음"));
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - 개인 공부방의 참여 코드로 참여 시도")
    void joinGroupRoom_Fail_NotGroupRoom() throws Exception {
        // given
        // 개인 공부방 생성 (참여 코드는 null이지만, 테스트를 위해 임의로 설정)
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("개인 공부방")
                .description("개인 학습용")
                .category("개인")
                .joinCode("PERSONAL")
                .build();
        studyRoomRepository.save(personalRoom);

        Map<String, String> request = new HashMap<>();
        request.put("joinCode", "PERSONAL");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group/join")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("그룹 스터디가 아님"));
    }

    @Test
    @DisplayName("그룹 스터디 참여 실패 - 이미 참여한 스터디룸")
    void joinGroupRoom_Fail_AlreadyJoined() throws Exception {
        // given
        // 그룹 스터디 생성
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘 풀이")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // 이미 멤버로 등록
        StudyRoomMember membership = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership);

        Map<String, String> request = new HashMap<>();
        request.put("joinCode", "ABC12345");

        // when & then
        mockMvc.perform(post("/api/study-rooms/group/join")
                        .cookie(new Cookie("accessToken", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("이미 참여한 스터디룸"));
    }

    // ==================== 개인 공부방 목록 조회 API 테스트 ====================

    @Test
    @DisplayName("개인 공부방 목록 조회 성공 - 빈 목록")
    void getPersonalRooms_Success_EmptyList() throws Exception {
        // given - 개인 공부방이 없는 상태

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms").isArray())
                .andExpect(jsonPath("$.rooms").isEmpty())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    @DisplayName("개인 공부방 목록 조회 성공 - 여러 개의 공부방")
    void getPersonalRooms_Success_MultipleRooms() throws Exception {
        // given
        StudyRoom room1 = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 개념 정리")
                .category("프로그래밍")
                .joinCode(null)
                .build();
        studyRoomRepository.save(room1);

        StudyRoom room2 = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("스프링 스터디")
                .description("스프링 부트 학습")
                .category("프레임워크")
                .joinCode(null)
                .build();
        studyRoomRepository.save(room2);

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms").isArray())
                .andExpect(jsonPath("$.rooms.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.rooms[0].name").value("자바 스터디"))
                .andExpect(jsonPath("$.rooms[0].category").value("프로그래밍"))
                .andExpect(jsonPath("$.rooms[0].totalProblems").value(0))
                .andExpect(jsonPath("$.rooms[0].graduatedProblems").value(0))
                .andExpect(jsonPath("$.rooms[1].name").value("스프링 스터디"))
                .andExpect(jsonPath("$.rooms[1].category").value("프레임워크"));
    }

    @Test
    @DisplayName("개인 공부방 목록 조회 실패 - JWT 토큰 없음")
    void getPersonalRooms_Fail_NoToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/study-rooms/personal"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ==================== 그룹 스터디 목록 조회 API 테스트 ====================

    @Test
    @DisplayName("그룹 스터디 목록 조회 성공 - 빈 목록")
    void getGroupRooms_Success_EmptyList() throws Exception {
        // given - 그룹 멤버십이 없는 상태

        // when & then
        mockMvc.perform(get("/api/study-rooms/group")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms").isArray())
                .andExpect(jsonPath("$.rooms").isEmpty())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    @DisplayName("그룹 스터디 목록 조회 성공 - 여러 개의 그룹")
    void getGroupRooms_Success_MultipleGroups() throws Exception {
        // given
        // 첫 번째 그룹 (방장으로 참여)
        StudyRoom group1 = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("매일 알고리즘 풀이")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(group1);

        StudyRoomMember membership1 = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(group1)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership1);

        // 두 번째 그룹 (일반 멤버로 참여)
        User owner2 = User.builder()
                .email("owner2@example.com")
                .password("password123")
                .username("방장2")
                .receiveNotifications(true)
                .build();
        userRepository.save(owner2);

        StudyRoom group2 = StudyRoom.builder()
                .owner(owner2)
                .roomType(RoomType.GROUP)
                .name("CS 스터디")
                .description("CS 기초 학습")
                .category("CS")
                .joinCode("XYZ67890")
                .build();
        studyRoomRepository.save(group2);

        StudyRoomMember membership2 = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(group2)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership2);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms").isArray())
                .andExpect(jsonPath("$.rooms.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.rooms[0].name").value("알고리즘 스터디"))
                .andExpect(jsonPath("$.rooms[0].category").value("알고리즘"))
                .andExpect(jsonPath("$.rooms[0].joinCode").value("ABC12345"))
                .andExpect(jsonPath("$.rooms[0].totalProblems").value(0))
                .andExpect(jsonPath("$.rooms[0].graduatedProblems").value(0))
                .andExpect(jsonPath("$.rooms[0].memberCount").value(1))
                .andExpect(jsonPath("$.rooms[1].name").value("CS 스터디"))
                .andExpect(jsonPath("$.rooms[1].category").value("CS"))
                .andExpect(jsonPath("$.rooms[1].joinCode").value("XYZ67890"))
                .andExpect(jsonPath("$.rooms[1].memberCount").value(1));
    }

    @Test
    @DisplayName("그룹 스터디 목록 조회 실패 - JWT 토큰 없음")
    void getGroupRooms_Fail_NoToken() throws Exception {
        // when & then
        mockMvc.perform(get("/api/study-rooms/group"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ==================== 개인 공부방 문제 목록 조회 API 테스트 ====================

    @Test
    @DisplayName("개인 공부방 문제 목록 조회 성공 - 빈 목록")
    void getPersonalRoomProblems_Success_EmptyList() throws Exception {
        // given
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 기초")
                .category("프로그래밍")
                .joinCode(null)
                .build();
        studyRoomRepository.save(personalRoom);

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal/" + personalRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyRoomId").value(personalRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.studyRoomName").value("자바 스터디"))
                .andExpect(jsonPath("$.problems").isArray())
                .andExpect(jsonPath("$.problems").isEmpty())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    @DisplayName("개인 공부방 문제 목록 조회 실패 - JWT 토큰 없음")
    void getPersonalRoomProblems_Fail_NoToken() throws Exception {
        // given
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 기초")
                .category("프로그래밍")
                .joinCode(null)
                .build();
        studyRoomRepository.save(personalRoom);

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal/" + personalRoom.getStudyRoomId() + "/problems")
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("개인 공부방 문제 목록 조회 실패 - 스터디룸을 찾을 수 없음")
    void getPersonalRoomProblems_Fail_StudyRoomNotFound() throws Exception {
        // given
        Long nonExistentStudyRoomId = 999L;

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal/" + nonExistentStudyRoomId + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("스터디룸을 찾을 수 없음"));
    }

    @Test
    @DisplayName("개인 공부방 문제 목록 조회 실패 - 개인방이 아님")
    void getPersonalRoomProblems_Fail_NotPersonalRoom() throws Exception {
        // given
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 학습")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal/" + groupRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("개인 공부방이 아님"));
    }

    @Test
    @DisplayName("개인 공부방 문제 목록 조회 실패 - 소유자가 아님")
    void getPersonalRoomProblems_Fail_NotRoomOwner() throws Exception {
        // given
        // 다른 사용자 생성
        User otherUser = User.builder()
                .email("other@example.com")
                .password("password123")
                .username("다른유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(otherUser);

        // 다른 사용자의 개인방 생성
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(otherUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 기초")
                .category("프로그래밍")
                .joinCode(null)
                .build();
        studyRoomRepository.save(personalRoom);

        // when & then
        mockMvc.perform(get("/api/study-rooms/personal/" + personalRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("스터디룸 소유자가 아님"));
    }

    // ===== 그룹 공부방 문제 목록 조회 API 통합 테스트 =====

    @Test
    @DisplayName("그룹 공부방 문제 목록 조회 성공 - ALL 필터")
    void getGroupRoomProblems_Success_AllFilter() throws Exception {
        // given
        // 그룹 스터디 생성
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 공부")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // 그룹 멤버십 생성
        StudyRoomMember membership = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + groupRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyRoomId").value(groupRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.studyRoomName").value("알고리즘 스터디"))
                .andExpect(jsonPath("$.problems").isArray())
                .andExpect(jsonPath("$.totalCount").value(0));
    }

    @Test
    @DisplayName("그룹 공부방 문제 목록 조회 성공 - NOT_IN_REVIEW 필터")
    void getGroupRoomProblems_Success_NotInReviewFilter() throws Exception {
        // given
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 공부")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        StudyRoomMember membership = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + groupRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "NOT_IN_REVIEW"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyRoomId").value(groupRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.problems").isArray());
    }

    @Test
    @DisplayName("그룹 공부방 문제 목록 조회 실패 - 개인 공부방 ID로 요청")
    void getGroupRoomProblems_Fail_NotGroupRoom() throws Exception {
        // given
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 기초")
                .category("프로그래밍")
                .joinCode(null)
                .build();
        studyRoomRepository.save(personalRoom);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + personalRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("그룹 스터디가 아님"));
    }

    @Test
    @DisplayName("그룹 공부방 문제 목록 조회 실패 - 그룹 멤버가 아님")
    void getGroupRoomProblems_Fail_NotGroupMember() throws Exception {
        // given
        // 다른 사용자 생성
        User otherUser = User.builder()
                .email("other@example.com")
                .password("password123")
                .username("다른유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(otherUser);

        // 다른 사용자의 그룹 스터디 생성
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(otherUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 공부")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // 다른 사용자만 멤버로 등록 (testUser는 멤버가 아님)
        StudyRoomMember membership = StudyRoomMember.builder()
                .user(otherUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + groupRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("그룹 멤버가 아님"));
    }

    @Test
    @DisplayName("그룹 공부방 문제 목록 조회 실패 - 잘못된 필터")
    void getGroupRoomProblems_Fail_InvalidFilter() throws Exception {
        // given
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 공부")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        StudyRoomMember membership = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + groupRoom.getStudyRoomId() + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "INVALID_FILTER"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("잘못된 필터"));
    }

    @Test
    @DisplayName("그룹 공부방 문제 목록 조회 실패 - 존재하지 않는 스터디룸")
    void getGroupRoomProblems_Fail_StudyRoomNotFound() throws Exception {
        // given
        Long nonExistentRoomId = 999L;

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + nonExistentRoomId + "/problems")
                        .cookie(new Cookie("accessToken", accessToken))
                        .param("filter", "ALL"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("스터디룸을 찾을 수 없음"));
    }

    // ===== 그룹 스터디 멤버 목록 조회 API 통합 테스트 =====

    @Test
    @DisplayName("그룹 스터디 멤버 목록 조회 성공 - 방장이 맨 앞에 위치")
    void getGroupRoomMembers_Success() throws Exception {
        // given
        // 그룹 스터디 생성
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 공부")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // 방장 멤버로 등록
        StudyRoomMember ownerMembership = StudyRoomMember.builder()
                .user(testUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(ownerMembership);

        // 다른 멤버 추가
        User member1 = User.builder()
                .email("member1@example.com")
                .password("password123")
                .username("멤버1")
                .receiveNotifications(true)
                .build();
        userRepository.save(member1);

        StudyRoomMember member1Ship = StudyRoomMember.builder()
                .user(member1)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(member1Ship);

        User member2 = User.builder()
                .email("member2@example.com")
                .password("password123")
                .username("멤버2")
                .receiveNotifications(true)
                .build();
        userRepository.save(member2);

        StudyRoomMember member2Ship = StudyRoomMember.builder()
                .user(member2)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(member2Ship);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + groupRoom.getStudyRoomId() + "/members")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyRoomId").value(groupRoom.getStudyRoomId()))
                .andExpect(jsonPath("$.studyRoomName").value("알고리즘 스터디"))
                .andExpect(jsonPath("$.totalMembers").value(3))
                .andExpect(jsonPath("$.members").isArray())
                .andExpect(jsonPath("$.members.length()").value(3))
                // 방장이 맨 앞에 위치
                .andExpect(jsonPath("$.members[0].userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.members[0].username").value("테스트유저"))
                .andExpect(jsonPath("$.members[0].isOwner").value(true))
                // 나머지 멤버는 방장이 아님
                .andExpect(jsonPath("$.members[1].isOwner").value(false))
                .andExpect(jsonPath("$.members[2].isOwner").value(false));
    }

    @Test
    @DisplayName("그룹 스터디 멤버 목록 조회 실패 - 개인 공부방 ID로 요청")
    void getGroupRoomMembers_Fail_NotGroupRoom() throws Exception {
        // given
        StudyRoom personalRoom = StudyRoom.builder()
                .owner(testUser)
                .roomType(RoomType.PERSONAL)
                .name("자바 스터디")
                .description("자바 기초")
                .category("프로그래밍")
                .joinCode(null)
                .build();
        studyRoomRepository.save(personalRoom);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + personalRoom.getStudyRoomId() + "/members")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("그룹 스터디가 아님"));
    }

    @Test
    @DisplayName("그룹 스터디 멤버 목록 조회 실패 - 그룹 멤버가 아님")
    void getGroupRoomMembers_Fail_NotGroupMember() throws Exception {
        // given
        // 다른 사용자 생성
        User otherUser = User.builder()
                .email("other@example.com")
                .password("password123")
                .username("다른유저")
                .receiveNotifications(true)
                .build();
        userRepository.save(otherUser);

        // 다른 사용자의 그룹 스터디 생성
        StudyRoom groupRoom = StudyRoom.builder()
                .owner(otherUser)
                .roomType(RoomType.GROUP)
                .name("알고리즘 스터디")
                .description("알고리즘 공부")
                .category("알고리즘")
                .joinCode("ABC12345")
                .build();
        studyRoomRepository.save(groupRoom);

        // 다른 사용자만 멤버로 등록 (testUser는 멤버가 아님)
        StudyRoomMember membership = StudyRoomMember.builder()
                .user(otherUser)
                .studyRoom(groupRoom)
                .active(true)
                .build();
        studyRoomMemberRepository.save(membership);

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + groupRoom.getStudyRoomId() + "/members")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("그룹 멤버가 아님"));
    }

    @Test
    @DisplayName("그룹 스터디 멤버 목록 조회 실패 - 존재하지 않는 스터디룸")
    void getGroupRoomMembers_Fail_StudyRoomNotFound() throws Exception {
        // given
        Long nonExistentRoomId = 999L;

        // when & then
        mockMvc.perform(get("/api/study-rooms/group/" + nonExistentRoomId + "/members")
                        .cookie(new Cookie("accessToken", accessToken)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("스터디룸을 찾을 수 없음"));
    }
}
