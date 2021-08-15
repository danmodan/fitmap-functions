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
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.mapper.DtoMapper;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ContactService;
import com.fitmap.function.service.PersonalTrainerService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.PersonalTrainerCreateRequest;
import com.fitmap.function.v2.payload.request.PersonalTrainerEditRequest;
import com.fitmap.function.v2.payload.response.PersonalTrainerResponse;
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
public class PersonalTrainerFunction implements HttpFunction {

    public PersonalTrainerFunction() {

        log.log(Level.INFO, "init PersonalTrainerFunction. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/personal-trainer":
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

        var dto = ReadRequestService.getBody(request, PersonalTrainerCreateRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        var created = create(dto, ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static void doPut(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, PersonalTrainerEditRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        update(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void update(PersonalTrainerEditRequest dto, String personalTrainerId) {

        var found = PersonalTrainerService.find(List.of(personalTrainerId));

        if(CollectionUtils.isEmpty(found)) {

            throw new TerminalException("Personal Trainer, " + personalTrainerId + ", does not exists.", HttpStatus.NOT_FOUND);
        }

        var current = found.get(0);

        var toUpdate = DtoMapper.from(dto, personalTrainerId);

        PersonalTrainerService.updateProps(toUpdate);

        updateAddresses(current, toUpdate);
        updateContacts(current, toUpdate);
    }

    private static void updateAddresses(PersonalTrainer current, PersonalTrainer toUpdate) {

        var personalTrainerId = current.getId();
        var currentAddresses = current.getAddresses();
        var toUpdateAddresses = toUpdate.getAddresses();

        if(toUpdateAddresses.isEmpty() && !currentAddresses.isEmpty()) {

            var addressesIds = currentAddresses
                .stream()
                .map(Address::getId)
                .collect(Collectors.toList());

            AddressService.delete(personalTrainerId, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION, addressesIds);
            return;
        }

        if(currentAddresses.isEmpty() && !toUpdateAddresses.isEmpty()) {

            toUpdateAddresses.forEach(a -> a.setMainAddress(true));

            AddressService.create(personalTrainerId, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION, toUpdateAddresses);
            return;
        }

        if(!currentAddresses.isEmpty() && !toUpdateAddresses.isEmpty()) {

            toUpdateAddresses.forEach(a -> {
                a.setMainAddress(true);
                a.setId(currentAddresses.get(0).getId());
            });

            AddressService.edit(personalTrainerId, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION, toUpdateAddresses);
            return;
        }

    }

    private static void updateContacts(PersonalTrainer current, PersonalTrainer toUpdate) {

        var personalTrainerId = current.getId();
        var currentContacts = current.getContacts();
        var toUpdateContacts = toUpdate.getContacts();

        if(toUpdateContacts.isEmpty() && !currentContacts.isEmpty()) {

            var contactsIds = currentContacts
                .stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

            ContactService.delete(personalTrainerId, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION, contactsIds);
            return;
        }

        if(currentContacts.isEmpty() && !toUpdateContacts.isEmpty()) {

            toUpdateContacts.forEach(c -> c.setMainContact(true));

            ContactService.create(personalTrainerId, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION, toUpdateContacts);
            return;
        }

        if(!currentContacts.isEmpty() && !toUpdateContacts.isEmpty()) {

            toUpdateContacts.forEach(c -> {
                c.setMainContact(true);
                c.setId(currentContacts.get(0).getId());
            });

            ContactService.edit(personalTrainerId, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION, toUpdateContacts);
            return;
        }

    }

    private static PersonalTrainerResponse create(PersonalTrainerCreateRequest dto, String personalTrainerId) {

        var found = find(List.of(personalTrainerId));

        if(CollectionUtils.isNotEmpty(found)) {

            throw new TerminalException("Personal Trainer, " + personalTrainerId + ", already exists.", HttpStatus.CONFLICT);
        }

        var personalTrainer = DtoMapper.from(dto, personalTrainerId);
        personalTrainer.getEvents().forEach(e -> {
            e.setAddress(null);
            e.setContact(null);
        });

        if(CollectionUtils.isNotEmpty(personalTrainer.getContacts())) {

            personalTrainer.getContacts().forEach(c -> c.setMainContact(true));
        }

        if(CollectionUtils.isNotEmpty(personalTrainer.getAddresses())) {

            personalTrainer.getAddresses().forEach(a -> a.setMainAddress(true));
        }

        return DtoMapper.from(PersonalTrainerService.create(personalTrainer));
    }

    private static List<PersonalTrainerResponse> find(List<String> ids) {

        if(ids.isEmpty()) {

            return Collections.emptyList();
        }

        return DtoMapper.from(PersonalTrainerService.find(ids), DtoMapper::from);
    }

}
