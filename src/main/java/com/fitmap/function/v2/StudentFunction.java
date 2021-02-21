package com.fitmap.function.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fitmap.function.domain.Student;
import com.fitmap.function.mapper.DomainMapper;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.StudentService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.AddressCreateRequest;
import com.fitmap.function.v2.payload.request.ContactCreateRequest;
import com.fitmap.function.v2.payload.request.StudentCreateRequest;
import com.fitmap.function.v2.payload.request.StudentEditRequest;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MethodNotAllowedException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StudentFunction {

    public static void service(HttpRequest request, HttpResponse response) {

        var requestMethod = HttpMethod.resolve(request.getMethod());

        switch (HttpMethod.resolve(request.getMethod())) {
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

        var ids = Objects.requireNonNullElse(request.getQueryParameters().get("ids"), new ArrayList<String>());

        var found = find(ids);

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

    private static void doPost(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, StudentCreateRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        var created = create(dto, ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static void doPut(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, StudentEditRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        update(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void doDelete(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, StudentEditRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        remove(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void update(StudentEditRequest dto, String studentId) {

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var student = DomainMapper.from(dto, studentId);

        StudentService.updateProps(student);
    }

    private static void remove(StudentEditRequest dto, String studentId) {

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var student = DomainMapper.from(dto, studentId);

        StudentService.removeElementsFromArraysProps(student);
    }

    private static Student create(StudentCreateRequest dto, String studentId) {

        var addresses = Objects.requireNonNullElse(dto.getAddresses(), new ArrayList<AddressCreateRequest>());

        var contacts = Objects.requireNonNullElse(dto.getContacts(), new ArrayList<ContactCreateRequest>());

        dto.setAddresses(addresses);
        dto.setContacts(contacts);

        var student = DomainMapper.from(dto, studentId);

        return StudentService.create(student);
    }

    private static List<Student> find(List<String> ids) {

        if(ids.isEmpty()) {

            return Collections.emptyList();
        }

        return StudentService.find(ids);
    }

}
