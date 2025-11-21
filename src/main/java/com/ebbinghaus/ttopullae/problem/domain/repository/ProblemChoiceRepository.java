package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.ProblemChoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemChoiceRepository extends JpaRepository<ProblemChoice, Long> {
}
