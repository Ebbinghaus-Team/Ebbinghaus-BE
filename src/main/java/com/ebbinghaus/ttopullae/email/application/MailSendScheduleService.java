package com.ebbinghaus.ttopullae.email.application;

import com.ebbinghaus.ttopullae.email.application.dto.MailSendCommand;
import com.ebbinghaus.ttopullae.problem.domain.repository.ProblemReviewStateRepository;
import com.ebbinghaus.ttopullae.problem.domain.repository.dto.TodayMailProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MailSendScheduleService {

    private final MailService mailService;
    private final ProblemReviewStateRepository problemReviewStateRepository;
    private final String fromEmail;

    public MailSendScheduleService(MailService mailService,
                                   ProblemReviewStateRepository problemReviewStateRepository,
                                   @Value("${spring.mail.username}") String fromEmail) {
        this.mailService = mailService;
        this.problemReviewStateRepository = problemReviewStateRepository;
        this.fromEmail = fromEmail;
    }

    @Scheduled(cron = "0 30 9 * * *")
    public void sendTodayProblem() {
        log.info("[MailSchedule] 메일 전송 스케줄러 시작");

        LocalDate today = LocalDate.now();
        List<TodayMailProjection> todayProblems = problemReviewStateRepository.findAllTodayReviewProblemMails(today);
        if (todayProblems.isEmpty()) {
            return;
        }

        Map<String, List<TodayMailProjection>> mailData = todayProblems.stream()
                .collect(Collectors.groupingBy(TodayMailProjection::getEmail));

        sendMail(mailData);
        log.info("[MailSchedule] 메일 전송 요청 종료 (총 대상자: {}명 요청 완료)", mailData.size());
    }

    private void sendMail(Map<String, List<TodayMailProjection>> mailData) {
        mailData.forEach((email, list) -> {
            MailSendCommand command = createMailSendCommand(email, list);
            mailService.sendEmail(command);
        });
    }

    private MailSendCommand createMailSendCommand(String userEmail, List<TodayMailProjection> mailInfo) {
        List<String> questions = mailInfo.stream()
                .map(TodayMailProjection::getQuestion)
                .toList();

        String username = mailInfo.getFirst().getUsername();

        return new MailSendCommand(
                userEmail,
                fromEmail,
                username,
                questions
        );
    }
}
