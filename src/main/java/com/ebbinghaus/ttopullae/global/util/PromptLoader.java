package com.ebbinghaus.ttopullae.global.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * 프롬프트 템플릿 파일을 로드하고 변수를 치환하는 유틸리티 클래스
 */
@Component
public class PromptLoader {

  private final ResourceLoader resourceLoader;

  public PromptLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  /**
   * 지정된 경로의 프롬프트 템플릿 파일을 읽어온다
   *
   * @param resourcePath 리소스 경로 (예: "classpath:prompts/grading_system_prompt.txt")
   * @return 파일 내용 문자열
   * @throws IOException 파일 읽기 실패 시
   */
  public String loadPromptTemplate(String resourcePath) throws IOException {
    Resource resource = resourceLoader.getResource(resourcePath);
    return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  /**
   * 프롬프트 템플릿의 변수를 실제 값으로 치환한다
   * 템플릿 변수 형식: {{VARIABLE_NAME}}
   *
   * @param template 프롬프트 템플릿 문자열
   * @param variables 치환할 변수 맵 (키: 변수명, 값: 치환할 값)
   * @return 변수가 치환된 최종 프롬프트 문자열
   */
  public String fillTemplate(String template, Map<String, String> variables) {
    String result = template;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      result = result.replace(placeholder, entry.getValue());
    }
    return result;
  }
}
