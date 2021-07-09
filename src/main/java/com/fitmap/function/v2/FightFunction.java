package com.fitmap.function.v2;

import com.fitmap.function.domain.Fight;
import com.fitmap.function.service.*;
import com.fitmap.function.util.Constants;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MethodNotAllowedException;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FightFunction {

    public static void service(HttpRequest request, HttpResponse response) {

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

        var dto = ReadRequestService.getBody(request, Fight[].class);

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

        FightService.remove(ids);
    }

    private static List<Fight> create(List<String> names) {

        var fights = Objects.requireNonNullElse(names, new ArrayList<String>()).stream().map(name -> new Fight(null, name)).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(fights)) {

            return Collections.emptyList();
        }

        return FightService.create(fights);
    }

    private static List<Fight> update(List<Fight> fights) {

        return FightService.update(fights);
    }

    private static List<Fight> find(List<String> ids)  {

        if(CollectionUtils.isNotEmpty(ids)) {

            return FightService.find(ids);
        }

        return FightService.findAll();
    }

}