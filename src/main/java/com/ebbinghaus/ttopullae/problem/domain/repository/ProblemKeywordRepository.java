package com.ebbinghaus.ttopullae.problem.domain.repository;

import com.ebbinghaus.ttopullae.problem.domain.Problem;
import com.ebbinghaus.ttopullae.problem.domain.ProblemKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemKeywordRepository extends JpaRepository<ProblemKeyword, Long> {

    List<ProblemKeyword> findByProblem(Problem problem);
}
