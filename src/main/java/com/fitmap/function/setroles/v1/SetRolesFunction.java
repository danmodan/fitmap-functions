package com.fitmap.function.setroles.v1;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.common.config.FirebaseAuthConfig;
import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.common.service.CheckRequestContentTypeService;
import com.fitmap.function.common.service.CheckRequestMethodService;
import com.fitmap.function.common.service.ReadRequestService;
import com.fitmap.function.common.service.ResponseService;
import com.fitmap.function.setroles.service.SetRolesService;
import com.fitmap.function.setroles.v1.payload.request.SetRolesRequestDto;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetRolesFunction implements HttpFunction {

    private static final Logger logger = Logger.getLogger(SetRolesFunction.class.getName());

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    private final SetRolesService setRolesService;

    public SetRolesFunction() {

        this.setRolesService = new SetRolesService(FirebaseAuthConfig.FIREBASE_AUTH);
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            CheckRequestMethodService.checkPostMethod(request);

            CheckRequestContentTypeService.checkApplicationJsonContentType(request);

            var dto = ReadRequestService.getBody(request, SetRolesRequestDto.class);

            CheckConstraintsRequestBodyService.checkConstraints(dto);

            setRolesService.setRoles(dto.getIdToken(), dto.getUserType().name());

        } catch (TerminalException e) { ResponseService.answerTerminalException(request, response, e); }
          catch (MethodNotAllowedException e) { ResponseService.answerMethodNotAllowed(request, response, e); }
          catch (UnsupportedMediaTypeStatusException e) { ResponseService.answerUnsupportedMediaType(request, response, e); }
          catch (HttpMessageNotReadableException e) { ResponseService.answerBadRequest(request, response, e); }
          catch (ConstraintViolationException e) { ResponseService.answerBadRequest(request, response, e); }
          catch (Exception e) { logger.log(Level.SEVERE, e.getMessage(), e); ResponseService.answerInternalServerError(request, response, e); }

    }

}
