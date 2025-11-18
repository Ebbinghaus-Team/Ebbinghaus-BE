package com.ebbinghaus.ttopullae.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 메서드 파라미터에 현재 로그인한 사용자의 ID를 주입하기 위한 어노테이션
 *
 * 사용 예시:
 * public ResponseEntity<?> createStudyRoom(@LoginUser Long userId, @RequestBody StudyRoomRequest request)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}