package com.ebbinghaus.ttopullae.user.presentation.dto;

import com.ebbinghaus.ttopullae.user.application.dto.SignupCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 회원가입 API 요청 DTO
 */
@Schema(description = "회원가입 요청")
public record SignupRequest(

        @Schema(description = "이메일 (로그인 ID)", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이어야 합니다")
        String email,

        @Schema(description = "비밀번호 (평문)", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다")
        String password,

        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank(message = "사용자 이름은 필수입니다")
        String username,

        @Schema(description = "알림 수신 여부", example = "true")
        @NotNull(message = "알림 수신 여부는 필수입니다")
        Boolean receiveNotifications
) {
    public SignupCommand toCommand() {
        return new SignupCommand(email, password, username, receiveNotifications);
    }
}