package com.markfest.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

@RestController
@RequestMapping("/notify")
public class NotificationController {
    private final JavaMailSender mailSender;

    public NotificationController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostMapping("/email")
    public ResponseEntity<?> sendEmail(@RequestBody Map<String,String> body) {
        String to = body.get("to");
        String subject = body.getOrDefault("subject","No subject");
        String text = body.getOrDefault("text","");
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            return ResponseEntity.ok(Map.of("status","sent"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }
}
