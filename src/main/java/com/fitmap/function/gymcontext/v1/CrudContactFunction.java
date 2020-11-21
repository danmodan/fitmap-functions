package com.fitmap.function.gymcontext.v1;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.common.config.FirestoreConfig;
import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.common.service.CheckRequestContentTypeService;
import com.fitmap.function.common.service.ReadRequestService;
import com.fitmap.function.common.service.ResponseService;
import com.fitmap.function.gymcontext.domain.Contact;
import com.fitmap.function.gymcontext.service.ContactService;
import com.fitmap.function.gymcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.gymcontext.v1.payload.request.EditRequestDtos;
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
public class CrudContactFunction implements HttpFunction {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    private final ContactService contactService;

    public CrudContactFunction() {

        this.contactService = new ContactService(FirestoreConfig.FIRESTORE);
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
                case DELETE:
                    doDelete(request, response);
                    break;
                case PUT:
                    doPut(request, response);
                    break;
                default:
                    throw new MethodNotAllowedException(requestMethod, Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT));
            }

        } catch (TerminalException e) {ResponseService.answerTerminalException(request, response, e);}
          catch (MethodNotAllowedException e) {ResponseService.answerMethodNotAllowed(request, response, e);}
          catch (UnsupportedMediaTypeStatusException e) {ResponseService.answerUnsupportedMediaType(request, response, e);}
          catch (HttpMessageNotReadableException e) {ResponseService.answerBadRequest(request, response, e);}
          catch (ConstraintViolationException e) {ResponseService.answerBadRequest(request, response, e);}
          catch (Exception e) { log.log(Level.SEVERE, e.getMessage(), e); ResponseService.answerInternalServerError(request, response, e); }

    }

    private void doGet(HttpRequest request, HttpResponse response) throws Exception {

        var found = find(ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

    private void doPut(HttpRequest request, HttpResponse response) throws Exception {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, EditRequestDtos.Contact[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        edit(Arrays.asList(dto), ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private void doDelete(HttpRequest request, HttpResponse response) throws Exception {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, String[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        delete(Arrays.asList(dto), ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private void doPost(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, CreateRequestDtos.Contact[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        var created = create(Arrays.asList(dto), ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private List<Contact> create(final List<CreateRequestDtos.Contact> dtos, final String gymId) {

        var contacts = Contact.from(dtos, Contact::from);
        return contactService.create(gymId, contacts);
    }

    private List<Contact> edit(final List<EditRequestDtos.Contact> dtos, final String gymId) {

        var contacts = Contact.from(dtos, Contact::from);
        return contactService.edit(gymId, contacts);
    }

    private List<Contact> find(final String gymId) throws Exception {

        return contactService.find(gymId);
    }

    private void delete(List<String> contactsIds, String gymId) {

        contactService.delete(gymId, contactsIds);
    }

}
