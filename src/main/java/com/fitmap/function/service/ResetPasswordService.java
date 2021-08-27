package com.fitmap.function.service;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import com.fitmap.function.config.FirebaseAuthConfig;
import com.fitmap.function.config.SendGridConfig;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.v2.payload.request.SendResetPasswordEmailRequest;
import com.google.firebase.auth.UserRecord;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.ASM;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResetPasswordService {

    public static void sendResetPasswordEmail(SendResetPasswordEmailRequest request) {

        var user = getUser(request.getReceiverEmail());

        var locale = request.getLocale();
        var from = createFrom();
        var subject = createSubject(locale);
        var receiverEmail = user.getEmail();
        var receiverName = user.getDisplayName();
        var actionLink = createActionLink(receiverEmail, locale);
        var to = new Email(receiverEmail, receiverName);

        var personalization = createPersonalization(subject, to, receiverName, locale, actionLink);
        var mail = createMail(from, subject, personalization);
        var sendgridRequest = createSendgridRequest(mail);

        var response = sendEmail(sendgridRequest);

        log.info("reset password email response: " + response.getStatusCode());
    }

    private static Response sendEmail(Request request) {

        try {

            return SendGridConfig.SEND_GRID_CLIENT.api(request);
        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);
            throw new TerminalException("Cannot send email", HttpStatus.BAD_REQUEST);
        }
    }

    private static Request createSendgridRequest(Mail mail) {

        try {

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            return request;
        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);
            throw new TerminalException("Cannot create email request", HttpStatus.BAD_REQUEST);
        }
    }

    private static Email createFrom() {

        return new Email(SendGridConfig.SENDGRID_FROM_EMAIL, SendGridConfig.SENDGRID_FROM_NAME);
    }

    private static String createSubject(Locale locale) {

        switch (locale.getLanguage()) {
            case "pt":
                return "Redefina sua senha";
            default:
                return "Reset your password";
        }
    }

    private static Mail createMail(Email from, String subject, Personalization personalization) {

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.setASM(createASM());
        mail.setTemplateId(SendGridConfig.RESET_PASSWORD_TEMPLATE_ID);
        mail.addPersonalization(personalization);

        return mail;
    }

    private static Personalization createPersonalization(String subject, Email to, String receiverName, Locale locale, String actionLink) {

        Personalization personalization = new Personalization();
        personalization.setSubject(subject);
        personalization.addDynamicTemplateData("subject", subject);

        if(StringUtils.isNotBlank(receiverName)) {
            personalization.addDynamicTemplateData("receiver_name", receiverName);
        }

        personalization.addDynamicTemplateData(getTemplateLanguage(locale.getLanguage()), true);
        personalization.addDynamicTemplateData("action_link", actionLink);
        personalization.addTo(to);

        return personalization;
    }

    private static String getTemplateLanguage(String lang) {

        switch (lang) {
            case "pt":
                return "portuguese";
            default:
                return "english";
        }
    }

    private static ASM createASM() {

        var asm = new ASM();
        asm.setGroupId(SendGridConfig.ACCOUNT_ALERTS_UNSUBSCRIBE_GROUP_ID);
        return asm;
    }

    private static String createActionLink(String email, Locale locale) {

        try {

            if(StringUtils.isBlank(email)) {
                throw new IllegalArgumentException("An email is mandatory.");
            }

            var actionLink = FirebaseAuthConfig
                .FIREBASE_AUTH
                .generatePasswordResetLink(email);

            return actionLink.replaceFirst("(lang=[a-z]{2})", "lang=" + locale.getLanguage());
        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException("Cannot create reset password email link for " + email + ".",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private static UserRecord getUser(String userEmail) {

        try {

            var user = FirebaseAuthConfig.FIREBASE_AUTH.getUserByEmail(userEmail);

            if(user == null) {

                throw new NoSuchElementException(String.format("User %s not found.", userEmail));
            }

            return user;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(String.format("User %s not found.", userEmail), HttpStatus.NOT_FOUND);
        }
    }

}