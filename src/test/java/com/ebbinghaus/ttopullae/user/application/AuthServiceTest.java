package com.ebbinghaus.ttopullae.user.application;

import com.ebbinghaus.ttopullae.global.exception.ApplicationException;
import com.ebbinghaus.ttopullae.global.util.JwtTokenProvider;
import com.ebbinghaus.ttopullae.user.application.dto.LoginCommand;
import com.ebbinghaus.ttopullae.user.application.dto.LoginResult;
import com.ebbinghaus.ttopullae.user.application.dto.SignupCommand;
import com.ebbinghaus.ttopullae.user.application.dto.SignupResult;
import com.ebbinghaus.ttopullae.user.domain.User;
import com.ebbinghaus.ttopullae.user.domain.repository.UserRepository;
import com.ebbinghaus.ttopullae.user.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given
        SignupCommand command = new SignupCommand(
                "test@example.com",
                "password123",
                "테스트유저",
                true
        );

        User savedUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("password123")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        given(userRepository.existsByEmail(command.email())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        SignupResult result = authService.signup(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.username()).isEqualTo("테스트유저");
        assertThat(result.receiveNotifications()).isTrue();

        verify(userRepository, times(1)).existsByEmail(command.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_Fail_DuplicateEmail() {
        // given
        SignupCommand command = new SignupCommand(
                "duplicate@example.com",
                "password123",
                "테스트유저",
                true
        );

        given(userRepository.existsByEmail(command.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.DUPLICATE_EMAIL);

        verify(userRepository, times(1)).existsByEmail(command.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginCommand command = new LoginCommand("test@example.com", "password123");

        User mockUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("password123")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        String mockToken = "mock.jwt.token";

        given(userRepository.findByEmail(command.email())).willReturn(Optional.of(mockUser));
        given(jwtTokenProvider.generateToken(mockUser.getUserId())).willReturn(mockToken);

        // when
        LoginResult result = authService.login(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.username()).isEqualTo("테스트유저");
        assertThat(result.accessToken()).isEqualTo(mockToken);

        verify(userRepository, times(1)).findByEmail(command.email());
        verify(jwtTokenProvider, times(1)).generateToken(1L);
    }

    @Test
    @DisplayName("로그인 실패 - 사용자를 찾을 수 없음")
    void login_Fail_UserNotFound() {
        // given
        LoginCommand command = new LoginCommand("notfound@example.com", "password123");

        given(userRepository.findByEmail(command.email())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.USER_NOT_FOUND);

        verify(userRepository, times(1)).findByEmail(command.email());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_InvalidPassword() {
        // given
        LoginCommand command = new LoginCommand("test@example.com", "wrongpassword");

        User mockUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .password("password123")
                .username("테스트유저")
                .receiveNotifications(true)
                .build();

        given(userRepository.findByEmail(command.email())).willReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("code", UserException.INVALID_PASSWORD);

        verify(userRepository, times(1)).findByEmail(command.email());
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}