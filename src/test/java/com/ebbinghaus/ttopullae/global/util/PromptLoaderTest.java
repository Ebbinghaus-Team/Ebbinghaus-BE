package com.ebbinghaus.ttopullae.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PromptLoaderTest {

  @Autowired
  private PromptLoader promptLoader;

  @Test
  @DisplayName("프롬프트 템플릿 파일을 성공적으로 로드한다")
  void loadPromptTemplate_Success() throws IOException {
    // when
    String template = promptLoader.loadPromptTemplate(
        "classpath:prompts/grading_system_prompt.txt");

    // then
    assertThat(template).isNotEmpty();
    assertThat(template).contains("{{TOPIC}}");
    assertThat(template).contains("{{QUESTION}}");
    assertThat(template).contains("{{MODEL_ANSWER}}");
    assertThat(template).contains("{{KEYWORDS_LIST}}");
    assertThat(template).contains("{{USER_ANSWER}}");
  }

  @Test
  @DisplayName("템플릿의 변수를 실제 값으로 치환한다")
  void fillTemplate_Success() {
    // given
    String template = "주제: {{TOPIC}}, 질문: {{QUESTION}}";
    Map<String, String> variables = Map.of(
        "TOPIC", "Spring Framework",
        "QUESTION", "IoC란 무엇인가?"
    );

    // when
    String result = promptLoader.fillTemplate(template, variables);

    // then
    assertThat(result).isEqualTo("주제: Spring Framework, 질문: IoC란 무엇인가?");
    assertThat(result).doesNotContain("{{");
    assertThat(result).doesNotContain("}}");
  }

  @Test
  @DisplayName("여러 변수를 한 번에 치환한다")
  void fillTemplate_MultipleVariables() {
    // given
    String template = "{{VAR1}}, {{VAR2}}, {{VAR3}}";
    Map<String, String> variables = Map.of(
        "VAR1", "A",
        "VAR2", "B",
        "VAR3", "C"
    );

    // when
    String result = promptLoader.fillTemplate(template, variables);

    // then
    assertThat(result).isEqualTo("A, B, C");
  }
}
