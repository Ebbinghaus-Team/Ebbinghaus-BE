package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.problem.application.ProblemService;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateResult;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemReviewInclusionCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitCommand;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemSubmitResult;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemReviewInclusionRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemReviewInclusionResponse;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemSubmitRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemSubmitResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProblemController implements ProblemControllerDocs {

    private final ProblemService problemService;

    @PostMapping("/study-rooms/{studyRoomId}")
    public ResponseEntity<ProblemCreateResponse> createProblem(
            @LoginUser Long userId,
            @PathVariable Long studyRoomId,
            @Valid @RequestBody ProblemCreateRequest request
    ) {
        ProblemCreateResult result = problemService.createProblem(
                request.toCommand(userId, studyRoomId)
        );
        ProblemCreateResponse response = ProblemCreateResponse.from(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{problemId}/submit")
    public ResponseEntity<ProblemSubmitResponse> submitProblemAnswer(
            @LoginUser Long userId,
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemSubmitRequest request
    ) {
        ProblemSubmitCommand command = new ProblemSubmitCommand(
                userId,
                problemId,
                request.answer()
        );

        ProblemSubmitResult result = problemService.submitProblemAnswer(command);
        ProblemSubmitResponse response = ProblemSubmitResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{problemId}/review-inclusion")
    public ResponseEntity<ProblemReviewInclusionResponse> configureReviewInclusion(
            @LoginUser Long userId,
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemReviewInclusionRequest request
    ) {
        ProblemReviewInclusionCommand command = new ProblemReviewInclusionCommand(
                problemId,
                request.includeInReview()
        );

        Boolean configured = problemService.configureReviewInclusion(userId, command);
        ProblemReviewInclusionResponse response = ProblemReviewInclusionResponse.of(configured);
        return ResponseEntity.ok(response);
    }
}
