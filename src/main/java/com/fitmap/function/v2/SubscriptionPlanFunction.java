package com.fitmap.function.v2;

import java.util.Arrays;
import java.util.List;

import com.fitmap.function.domain.SubscriptionPlan;
import com.fitmap.function.mapper.DtoMapper;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.SubscriptionPlanService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.SubscriptionPlanCreateRequest;
import com.fitmap.function.v2.payload.request.SubscriptionPlanEditRequest;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MethodNotAllowedException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionPlanFunction {

    public static void service(HttpRequest request, HttpResponse response, String superCollection) {

        var requestMethod = HttpMethod.resolve(request.getMethod());

        switch (HttpMethod.resolve(request.getMethod())) {
            case GET:
                doGet(request, response, superCollection);
                break;
            case POST:
                doPost(request, response, superCollection);
                break;
            case PUT:
                doPut(request, response, superCollection);
                break;
            case DELETE:
                doDelete(request, response, superCollection);
                break;
            default:
                throw new MethodNotAllowedException(requestMethod, Constants.CRUD_HTTP_METHODS);
        }

    }

    private static void doGet(HttpRequest request, HttpResponse response, String superCollection) {

        var userId = ReadRequestService.getUserId(request);

        var found = find(userId, superCollection);

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

    private static List<SubscriptionPlan> find(String superEntityId, String superCollection) {

        return SubscriptionPlanService.find(superEntityId, superCollection);
    }

    private static void doPost(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, SubscriptionPlanCreateRequest[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        var created = create(Arrays.asList(dto), ReadRequestService.getUserId(request), superCollection);

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static List<SubscriptionPlan> create(List<SubscriptionPlanCreateRequest> dtos, String superEntityId, String superCollection) {

        var subEntities = DtoMapper.from(dtos, DtoMapper::from);

        return SubscriptionPlanService.create(superEntityId, superCollection, subEntities);
    }

    private static void doPut(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, SubscriptionPlanEditRequest[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        edit(Arrays.asList(dto), ReadRequestService.getUserId(request), superCollection);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static List<SubscriptionPlan> edit(List<SubscriptionPlanEditRequest> dtos, String superEntityId, String superCollection) {

        var subEntities = DtoMapper.from(dtos, DtoMapper::from);

        return SubscriptionPlanService.edit(superEntityId, superCollection, subEntities);
    }

    private static void doDelete(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, String[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        var userId = ReadRequestService.getUserId(request);

        delete(Arrays.asList(dto), userId, superCollection);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void delete(List<String> subscriptionPlansIds, String superEntityId, String superCollection) {

        SubscriptionPlanService.delete(superEntityId, superCollection, subscriptionPlansIds);
    }

}
