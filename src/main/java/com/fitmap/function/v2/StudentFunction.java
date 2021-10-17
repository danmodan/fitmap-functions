package com.fitmap.function.v2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.SystemTimeZoneConfig;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.Student;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.mapper.DtoMapper;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ContactService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.StudentService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.StudentCreateRequest;
import com.fitmap.function.v2.payload.request.StudentEditRequest;
import com.fitmap.function.v2.payload.response.StudentResponse;
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
public class StudentFunction implements HttpFunction {

    public StudentFunction() {

        log.log(Level.INFO, "init StudentFunction. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/student":
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

    private static void update(StudentEditRequest dto, String studentId) {

        var found = StudentService.find(List.of(studentId));

        if(CollectionUtils.isEmpty(found)) {

            throw new TerminalException("Student, " + studentId + ", does not exists.", HttpStatus.NOT_FOUND);
        }

        var current = found.get(0);

        var toUpdate = DtoMapper.from(dto, studentId);

        StudentService.updateProps(toUpdate);

        updateAddresses(current, toUpdate);
        updateContacts(current, toUpdate);

    }

    private static void updateAddresses(Student current, Student toUpdate) {

        var studentId = current.getId();
        var mainAddress = current.findMainAddress();
        var toUpdateAddresses = toUpdate.getAddresses();

        if(toUpdateAddresses.isEmpty() && mainAddress.isPresent()) {

            AddressService.delete(studentId, Student.STUDENTS_COLLECTION, List.of(mainAddress.get().getId()));
            return;
        }

        if(mainAddress.isEmpty() && !toUpdateAddresses.isEmpty()) {

            toUpdateAddresses.forEach(a -> a.setMainAddress(true));

            AddressService.create(studentId, Student.STUDENTS_COLLECTION, toUpdateAddresses);
            return;
        }

        if(mainAddress.isPresent() && !toUpdateAddresses.isEmpty()) {

            toUpdateAddresses.forEach(a -> {
                a.setMainAddress(true);
                a.setId(mainAddress.get().getId());
            });

            AddressService.edit(studentId, Student.STUDENTS_COLLECTION, toUpdateAddresses);
            return;
        }

    }

    private static void updateContacts(Student current, Student toUpdate) {

        var studentId = current.getId();
        var currentContacts = current.getContacts();
        var toUpdateContacts = toUpdate.getContacts();

        if(toUpdateContacts.isEmpty() && !currentContacts.isEmpty()) {

            var contactsIds = currentContacts
                .stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

            ContactService.delete(studentId, Student.STUDENTS_COLLECTION, contactsIds);
            return;
        }

        if(currentContacts.isEmpty() && !toUpdateContacts.isEmpty()) {

            toUpdateContacts.forEach(c -> c.setMainContact(true));

            ContactService.create(studentId, Student.STUDENTS_COLLECTION, toUpdateContacts);
            return;
        }

        if(!currentContacts.isEmpty() && !toUpdateContacts.isEmpty()) {

            toUpdateContacts.forEach(c -> {
                c.setMainContact(true);
                c.setId(currentContacts.get(0).getId());
            });

            ContactService.edit(studentId, Student.STUDENTS_COLLECTION, toUpdateContacts);
            return;
        }

    }

    private static StudentResponse create(StudentCreateRequest dto, String studentId) {

        var found = find(List.of(studentId));

        if(CollectionUtils.isNotEmpty(found)) {

            throw new TerminalException("Student, " + studentId + ", already exists.", HttpStatus.CONFLICT);
        }

        var student = DtoMapper.from(dto, studentId);

        if(CollectionUtils.isNotEmpty(student.getContacts())) {

            student.getContacts().forEach(c -> c.setMainContact(true));
        }

        if(CollectionUtils.isNotEmpty(student.getAddresses())) {

            student.getAddresses().forEach(a -> a.setMainAddress(true));
        }

        return DtoMapper.from(StudentService.create(student));
    }

    private static List<StudentResponse> find(List<String> ids) {

        if(ids.isEmpty()) {

            return Collections.emptyList();
        }

        return DtoMapper.from(StudentService.find(ids), DtoMapper::from);
    }

}
