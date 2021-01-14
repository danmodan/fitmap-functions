package com.fitmap.function.studentcontext.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.common.service.CheckRequestContentTypeService;
import com.fitmap.function.common.service.ReadRequestService;
import com.fitmap.function.common.service.ResponseService;
import com.fitmap.function.commonfirestore.config.FirestoreConfig;
import com.fitmap.function.studentcontext.domain.Student;
import com.fitmap.function.studentcontext.service.StudentService;
import com.fitmap.function.studentcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.studentcontext.v1.payload.request.EditRequestDtos;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class CrudStudentFunction implements HttpFunction {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    private final StudentService studentService;

    public CrudStudentFunction() {
        this.studentService = new StudentService(FirestoreConfig.FIRESTORE);

        log.info("awake function");
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

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
                throw new MethodNotAllowedException(requestMethod, Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE));
            }

        } catch (TerminalException e) {ResponseService.answerTerminalException(request, response, e);}
          catch (MethodNotAllowedException e) {ResponseService.answerMethodNotAllowed(request, response, e);}
          catch (UnsupportedMediaTypeStatusException e) {ResponseService.answerUnsupportedMediaType(request, response, e);}
          catch (HttpMessageNotReadableException e) {ResponseService.answerBadRequest(request, response, e);}
          catch (ConstraintViolationException e) {ResponseService.answerBadRequest(request, response, e);}
          catch (Exception e) { log.log(Level.SEVERE, e.getMessage(), e); ResponseService.answerInternalServerError(request, response, e); }

    }

    private void doGet(HttpRequest request, HttpResponse response) throws Exception {

        var ids = Objects.requireNonNullElse(request.getQueryParameters().get("ids"), new ArrayList<String>());

        var found = find(ids);

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

    private void doPost(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, CreateRequestDtos.Student.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        var created = create(dto, ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private void doPut(HttpRequest request, HttpResponse response) throws Exception {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, EditRequestDtos.Student.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        update(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private void doDelete(HttpRequest request, HttpResponse response) throws Exception {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, EditRequestDtos.Student.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        remove(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private void update(EditRequestDtos.Student dto, String studentId) throws Exception {

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var student = Student.from(dto, studentId);

        studentService.updateProps(student);
    }

    private void remove(EditRequestDtos.Student dto, String studentId) throws Exception {

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var student = Student.from(dto, studentId);

        studentService.removeElementsFromArraysProps(student);
    }

    private Student create(final CreateRequestDtos.Student dto, final String studentId) {

        var addresses = Objects.requireNonNullElse(dto.getAddresses(), new ArrayList<CreateRequestDtos.Address>());

        var contacts = Objects.requireNonNullElse(dto.getContacts(), new ArrayList<CreateRequestDtos.Contact>());

        dto.setAddresses(addresses);
        dto.setContacts(contacts);

        var student = Student.from(dto, studentId);

        return studentService.create(student);
    }

    private List<Student> find(final List<String> ids) throws Exception {

        if(ids.isEmpty()) {

            return Collections.emptyList();
        }

        return studentService.find(ids);
    }

}