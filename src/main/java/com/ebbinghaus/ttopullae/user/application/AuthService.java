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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     * @param command 회원가입 요청 정보
     * @return 생성된 사용자 정보
     * @throws ApplicationException 이메일이 이미 존재하는 경우
     */
    @Transactional
    public SignupResult signup(SignupCommand command) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(command.email())) {
            throw new ApplicationException(UserException.DUPLICATE_EMAIL);
        }

        // 사용자 엔티티 생성 및 저장
        User user = User.builder()
                .email(command.email())
                .password(command.password()) // MVP: 평문 저장
                .username(command.username())
                .receiveNotifications(command.receiveNotifications())
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResult(
                savedUser.getUserId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getReceiveNotifications()
        );
    }

    /**
     * 로그인
     * @param command 로그인 요청 정보
     * @return JWT 토큰 및 사용자 정보
     * @throws ApplicationException 사용자를 찾을 수 없거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new ApplicationException(UserException.USER_NOT_FOUND));

        // 비밀번호 검증 (MVP: 평문 비교)
        if (!user.getPassword().equals(command.password())) {
            throw new ApplicationException(UserException.INVALID_PASSWORD);
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(user.getUserId());

        return new LoginResult(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                accessToken
        );
    }
}