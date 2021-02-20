package com.fitmap.function.v1;

import java.util.Arrays;
import java.util.List;

import com.fitmap.function.domain.Address;
import com.fitmap.function.mapper.DomainMapper;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v1.payload.request.AddressCreateRequest;
import com.fitmap.function.v1.payload.request.AddressEditRequest;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MethodNotAllowedException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressFunction {

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

    private static List<Address> find(String superEntityId, String superCollection) {

        return AddressService.find(superEntityId, superCollection);
    }

    private static void doPost(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, AddressCreateRequest[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        var addresses = Arrays.asList(dto);

        CheckConstraintsRequestBodyService.checkOnlyOneMainElement(addresses, AddressCreateRequest::isMainAddress);

        var created = create(addresses, ReadRequestService.getUserId(request), superCollection);

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static List<Address> create(List<AddressCreateRequest> dtos, String superEntityId, String superCollection) {

        var subEntities = DomainMapper.from(dtos, DomainMapper::from);

        return AddressService.create(superEntityId, superCollection, subEntities);
    }

    private static void doPut(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, AddressEditRequest[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        var addresses = Arrays.asList(dto);

        CheckConstraintsRequestBodyService.checkOnlyOneMainElement(addresses, AddressEditRequest::isMainAddress);

        edit(addresses, ReadRequestService.getUserId(request), superCollection);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static List<Address> edit(List<AddressEditRequest> dtos, String superEntityId, String superCollection) {

        var subEntities = DomainMapper.from(dtos, DomainMapper::from);

        return AddressService.edit(superEntityId, superCollection, subEntities);
    }

    private static void doDelete(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, String[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        var userId = ReadRequestService.getUserId(request);

        delete(Arrays.asList(dto), userId, superCollection);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void delete(List<String> addressesIds, String superEntityId, String superCollection) {

        AddressService.delete(superEntityId, superCollection, addressesIds);
    }

}
