package com.ebbinghaus.ttopullae.email.application.dto;

import java.util.List;

public record MailSendCommand(
        String to,
        String from,
        String username,
        List<String> problems
) {

}
