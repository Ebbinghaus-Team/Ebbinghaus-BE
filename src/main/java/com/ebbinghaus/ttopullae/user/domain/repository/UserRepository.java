package com.ebbinghaus.ttopullae.user.domain.repository;

import com.ebbinghaus.ttopullae.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * @param email 사용자 이메일
     * @return 사용자 엔티티 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 이메일이 이미 존재하면 true
     */
    boolean existsByEmail(String email);
}
