package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemChoiceRepository extends JpaRepository<ProblemChoice, Long> {
    List<ProblemChoice> findByProblem(Problem problem);
}
