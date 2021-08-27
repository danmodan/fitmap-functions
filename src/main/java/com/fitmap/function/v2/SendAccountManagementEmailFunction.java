package com.fitmap.function.v2;

import java.time.ZonedDateTime;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.SystemTimeZoneConfig;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.CheckRequestMethodService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResetPasswordService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.SignUpEmailVerifyService;
import com.fitmap.function.v2.payload.request.SendResetPasswordEmailRequest;
import com.fitmap.function.v2.payload.request.SendVerificationEmailRequest;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.extern.java.Log;

@Log
public class SendAccountManagementEmailFunction implements HttpFunction {

    public SendAccountManagementEmailFunction() {

        log.log(Level.INFO, "init SendAccountManagementEmailFunction. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/send-sign-up-verify-email":
                    sendVerificationEmail(request, response);
                    break;
                case "/api/v2/send-reset-password-email":
                    sendResetPasswordEmail(request, response);
                    break;
                default:
                    throw new TerminalException("No mapping found for HTTP request path [" + path + "]", HttpStatus.NOT_FOUND);
            }

        } catch (TerminalException e) { ResponseService.answerTerminalException(request, response, e); }
          catch (MethodNotAllowedException e) { ResponseService.answerMethodNotAllowed(request, response, e); }
          catch (UnsupportedMediaTypeStatusException e) { ResponseService.answerUnsupportedMediaType(request, response, e); }
          catch (HttpMessageNotReadableException e) { ResponseService.answerBadRequest(request, response, e); }
          catch (ConstraintViolationException e) { ResponseService.answerBadRequest(request, response, e); }
          catch (Exception e) { log.log(Level.SEVERE, e.getMessage(), e); ResponseService.answerInternalServerError(request, response, e); }

    }

    public static void sendResetPasswordEmail(HttpRequest request, HttpResponse response) {

        CheckRequestMethodService.checkPostMethod(request);

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var clientLocale = ReadRequestService.getAcceptLanguage(request);

        var dto = ReadRequestService.getBody(request, SendResetPasswordEmailRequest.class);
        dto.setLocale(clientLocale);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        ResetPasswordService.sendResetPasswordEmail(dto);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    public static void sendVerificationEmail(HttpRequest request, HttpResponse response) {

        CheckRequestMethodService.checkPostMethod(request);

        var userIdToken = ReadRequestService.getUserIdToken(request);
        var clientLocale = ReadRequestService.getAcceptLanguage(request);

        var dto = new SendVerificationEmailRequest();
        dto.setIdToken(userIdToken);
        dto.setLocale(clientLocale);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        SignUpEmailVerifyService.sendVerificationEmail(dto);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

}
