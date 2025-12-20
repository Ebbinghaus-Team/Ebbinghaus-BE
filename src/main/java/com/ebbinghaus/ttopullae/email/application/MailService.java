package com.ebbinghaus.ttopullae.email.application;

import com.ebbinghaus.ttopullae.email.application.dto.MailSendCommand;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.problem-base-url}")
    private String baseUrl;

    @Async("mailExecutor")
    public void sendEmail(MailSendCommand command) {
        MimeMessage message = mailSender.createMimeMessage();

        try {

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("username", command.username());
            context.setVariable("problems", command.problems());

            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("problem-mail", context);

            helper.setTo(command.to());
            helper.setSubject("[Study Loop] 오늘의 복습 문제 목록이 도착했습니다!");
            helper.setText(htmlContent, true);
            helper.setFrom(command.from(), "Study Loop");

            mailSender.send(message);
            log.info("[MailService] 메일 전송 성공! (Target: {})", command.to());

        } catch (Exception e) {
            log.error("[MailService] 메일 전송 실패 (Target: {})", command.to(), e);
        }
    }

}
