package com.ebbinghaus.ttopullae.user.domain.repository;

import com.ebbinghaus.ttopullae.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
