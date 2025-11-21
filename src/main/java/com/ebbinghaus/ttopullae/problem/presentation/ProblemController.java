package com.ebbinghaus.ttopullae.problem.presentation;

import com.ebbinghaus.ttopullae.global.auth.LoginUser;
import com.ebbinghaus.ttopullae.problem.application.ProblemService;
import com.ebbinghaus.ttopullae.problem.application.dto.ProblemCreateResult;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateRequest;
import com.ebbinghaus.ttopullae.problem.presentation.dto.ProblemCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-rooms/{studyRoomId}/problems")
@RequiredArgsConstructor
public class ProblemController implements ProblemControllerDocs {

    private final ProblemService problemService;

    @PostMapping
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
}
