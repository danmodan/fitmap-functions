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
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.SetRolesService;
import com.fitmap.function.v2.payload.request.SetRolesRequest;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.extern.java.Log;

@Log
public class SetRolesFunction implements HttpFunction {

    public SetRolesFunction() {

        log.log(Level.INFO, "init SetRolesFunction. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/set-roles":
                    doService(request, response);
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

    public static void doService(HttpRequest request, HttpResponse response) {

        CheckRequestMethodService.checkPostMethod(request);

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, SetRolesRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        SetRolesService.setRoles(dto.getIdToken(), dto.getUserType().name());

    }

}
