package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Te dane i tak najlepiej dociągać z application.properties
        // Ale tutaj możesz ręcznie ustawić specyficzne parametry serwera
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("twoj-email@gmail.com");
        mailSender.setPassword("twoje-haslo-aplikacji");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false"); // Ustaw na 'true', żeby widzieć logi wysyłki w konsoli
        props.put("mail.smtp.charset", "UTF-8"); // Kluczowe dla polskich znaków!

        return mailSender;
    }
}