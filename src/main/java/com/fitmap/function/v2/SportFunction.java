package com.fitmap.function.v2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.SystemTimeZoneConfig;
import com.fitmap.function.domain.Sport;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.SportService;
import com.fitmap.function.util.Constants;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.extern.java.Log;

@Log
public class SportFunction implements HttpFunction {

    public SportFunction() {

        log.log(Level.INFO, "init SportFunction. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/sport":
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

        var requestMethod = HttpMethod.resolve(request.getMethod());

        switch (requestMethod) {
            case GET:
                doGet(request, response);
                break;
            case POST:
                doPost(request, response);
                break;
            case PUT:
                doPut(request, response);
                break;
            case DELETE:
                doDelete(request, response);
                break;
            default:
                throw new MethodNotAllowedException(requestMethod, Constants.CRUD_HTTP_METHODS);
        }

    }

    private static void doGet(HttpRequest request, HttpResponse response) {

        var found = find(request.getQueryParameters().get("ids"));

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

    private static void doPost(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, String[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        var created = create(Arrays.asList(dto));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static void doPut(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, Sport[].class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        update(Arrays.asList(dto));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void doDelete(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, String[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        delete(Arrays.asList(dto));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void delete(List<String> ids) {

        if(CollectionUtils.isEmpty(ids)) {

            return;
        }

        SportService.remove(ids);
    }

    private static List<Sport> create(List<String> names) {

        var sports = Objects.requireNonNullElse(names, new ArrayList<String>()).stream().map(name -> new Sport(null, name, null)).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(sports)) {

            return Collections.emptyList();
        }

        return SportService.create(sports);
    }

    private static List<Sport> update(List<Sport> sports) {

        return SportService.update(sports);
    }

    private static List<Sport> find(List<String> ids)  {

        if(CollectionUtils.isNotEmpty(ids)) {

            return SportService.find(ids);
        }

        return SportService.findAll();
    }

}