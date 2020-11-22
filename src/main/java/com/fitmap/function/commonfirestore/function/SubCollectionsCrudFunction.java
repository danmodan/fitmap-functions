package com.fitmap.function.commonfirestore.function;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.common.service.CheckRequestContentTypeService;
import com.fitmap.function.common.service.ReadRequestService;
import com.fitmap.function.common.service.ResponseService;
import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
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
public abstract class SubCollectionsCrudFunction<T, E, C> implements HttpFunction {

    private final SubCollectionsCrudService<T> service;

    protected abstract Class<E[]> getEditRequestDtoClass();
    protected abstract Class<C[]> getCreateRequestDtoClass();
    protected abstract Function<List<C>, List<T>> mapCreationDtosToSubEntities();
    protected abstract Function<List<E>, List<T>> mapEditDtosToSubEntities();

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

        var dto = ReadRequestService.getBody(request, getEditRequestDtoClass());

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

        var dto = ReadRequestService.getBody(request, getCreateRequestDtoClass());

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        var created = create(Arrays.asList(dto), ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private List<T> create(final List<C> dtos, final String superEntityId) {

        var subEntities = mapCreationDtosToSubEntities().apply(dtos);
        return service.create(superEntityId, subEntities);
    }

    private List<T> edit(final List<E> dtos, final String superEntityId) {

        var subEntities = mapEditDtosToSubEntities().apply(dtos);
        return service.edit(superEntityId, subEntities);
    }

    private List<T> find(final String superEntityId) throws Exception {

        return service.find(superEntityId);
    }

    private void delete(List<String> subEntitiesIds, String superEntityId) {

        service.delete(superEntityId, subEntitiesIds);
    }

}
