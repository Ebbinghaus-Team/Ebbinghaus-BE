package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.problem.application.ProblemService;
import com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.TodayReviewResult;
import com.ebbinghaus.ttopullae.problem.presentation.dto.TodayReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerDocs {

    private final ProblemService problemService;

    @GetMapping("/today")
    public ResponseEntity<TodayReviewResponse> getTodayReviewProblems(
        @LoginUser Long userId,
        @RequestParam(defaultValue = "ALL") String filter
    ) {
        TodayReviewCommand command = new TodayReviewCommand(userId, filter);
        TodayReviewResult result = problemService.getTodayReviewProblems(command);
        TodayReviewResponse response = TodayReviewResponse.from(result);
        return ResponseEntity.ok(response);
    }
}