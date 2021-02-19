package com.fitmap.function.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.mapper.DomainMapper;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.PersonalTrainerService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v1.payload.request.AddressCreateRequest;
import com.fitmap.function.v1.payload.request.ContactCreateRequest;
import com.fitmap.function.v1.payload.request.EventCreateRequest;
import com.fitmap.function.v1.payload.request.PersonalTrainerCreateRequest;
import com.fitmap.function.v1.payload.request.PersonalTrainerEditRequest;
import com.fitmap.function.v1.payload.request.SubscriptionPlanCreateRequest;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MethodNotAllowedException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PersonalTrainerFunction {

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

    private static void doDelete(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, PersonalTrainerEditRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        remove(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void update(PersonalTrainerEditRequest dto, String personalTrainerId) {

        var sports = Objects.requireNonNullElse(dto.getSports(), new ArrayList<String>());

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setSports(sports);
        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var personalTrainer = DomainMapper.from(dto, personalTrainerId);

        PersonalTrainerService.updateProps(personalTrainer);
    }

    private static void remove(PersonalTrainerEditRequest dto, String personalTrainerId) {

        var sports = Objects.requireNonNullElse(dto.getSports(), new ArrayList<String>());

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setSports(sports);
        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var personalTrainer = DomainMapper.from(dto, personalTrainerId);

        PersonalTrainerService.removeElementsFromArraysProps(personalTrainer);
    }

    private static PersonalTrainer create(PersonalTrainerCreateRequest dto, String personalTrainerId) {

        var addresses = Objects.requireNonNullElse(dto.getAddresses(), new ArrayList<AddressCreateRequest>());

        var contacts = Objects.requireNonNullElse(dto.getContacts(), new ArrayList<ContactCreateRequest>());

        var events = Objects.requireNonNullElse(dto.getEvents(), new ArrayList<EventCreateRequest>());
        events.forEach(event -> {
            event.setAddressId(null);
            event.setContactId(null);
        });

        var subscriptionPlans = Objects.requireNonNullElse(dto.getSubscriptionPlans(), new ArrayList<SubscriptionPlanCreateRequest>());

        var sports = Objects.requireNonNullElse(dto.getSports(), new ArrayList<String>());

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setAddresses(addresses);
        dto.setContacts(contacts);
        dto.setEvents(events);
        dto.setSubscriptionPlans(subscriptionPlans);
        dto.setSports(sports);
        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var personalTrainer = DomainMapper.from(dto, personalTrainerId);

        return PersonalTrainerService.create(personalTrainer);
    }

    private static List<PersonalTrainer> find(List<String> ids) {

        if(ids.isEmpty()) {

            return Collections.emptyList();
        }

        return PersonalTrainerService.find(ids);
    }

}
