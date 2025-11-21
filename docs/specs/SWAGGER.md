# SWAGGER.md

## AI Agent Swagger 문서 작성 가이드라인

> 이 문서는 AI Agent가 API 문서 형식을 유지하며 Swagger(OpenAPI) 어노테이션을 자동 생성할 수 있도록 하기 위한 규칙입니다.

---

## 1. 기본 원칙

1. **StudyRoomControllerDocs 예시 스타일을 기준으로 Swagger 어노테이션을 작성한다.**
2. 모든 API는 다음 요소를 반드시 포함한다:

    * `@Operation(summary, description)`
    * `@ApiResponses`
    * 성공 Response 예제
    * 에러 Response 예제 (`ErrorResponse` 기반)
3. 예시는 모두 **JSON 문자열 Block**(`""" ... """`)을 사용한다.
4. **ResponseEntity<> 타입을 사용하며, 반환 DTO를 Swagger에도 명시**한다.
5.  모든 JSON 예시는 실제 API 구조에 맞게 상세하게 작성한다.
5. Validation 실패 · NotFound · ServerError 등은 반드시 포함한다.
6. 스웨거 명세는 한글로 작성한다.

---

## 2. 파일 구조 규칙

Swagger 문서는 다음과 같은 인터페이스 기반 구조에서 작성한다.

```
예시: StudyRoomControllerDocs.java    
```

* ControllerDocs 라는 네이밍을 사용한다.
* `@Tag`를 통해 API 카테고리를 정의한다.

---

## 3. 기본 템플릿

### 3.1 API 메서드 명세 템플릿

AI가 Swagger 문서를 생성할 때 아래 템플릿을 따른다.

```java
@Operation(
        summary = "{요약 설명}",
        description = "{자세한 설명}"
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "{성공 설명}",
                content = @Content(
                        schema = @Schema(implementation = {성공 Response DTO}.class),
                        examples = @ExampleObject(
                                name = "{성공 예시 제목}",
                                value = """
                                        {
                                          "key": "value"
                                        }
                                        """
                        )
                )
        ),

        @ApiResponse(responseCode = "400", description = "잘못된 요청",
                content = @Content(
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(
                                        name = "{상황 설명}",
                                        value = """
                                                {
                                                  "title": "유효하지 않은 입력값",
                                                  "status": 400,
                                                  "detail": "{에러 상세 메시지}",
                                                  "instance": "{API 경로}"
                                                }
                                                """
                                )
                        }
                )
        ),

        @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                content = @Content(
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "{상황 설명}",
                                value = """
                                        {
                                          "title": "리소스를 찾을 수 없음",
                                          "status": 404,
                                          "detail": "{상세 설명}",
                                          "instance": "{API 경로}"
                                        }
                                        """
                        )
                )
        ),

        @ApiResponse(responseCode = "500", description = "서버 오류",
                content = @Content(
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                name = "{상황 설명}",
                                value = """
                                        {
                                          "title": "서버 내부 오류",
                                          "status": 500,
                                          "detail": "{상세 설명}",
                                          "instance": "{API 경로}"
                                        }
                                        """
                        """
                        )
                )
        )
})
@PostMapping("{path}")
ResponseEntity<{DTO}> methodName(
        @Valid @RequestBody {RequestDTO} request
);
```
- 위와 같이 해당 API에서 응답 될 수 있는 상황에 대해서 ApiResponse 를 작성한다. 
