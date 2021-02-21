package com.fitmap.function.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fitmap.function.domain.Gym;
import com.fitmap.function.mapper.DomainMapper;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.GymService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.AddressCreateRequest;
import com.fitmap.function.v2.payload.request.ContactCreateRequest;
import com.fitmap.function.v2.payload.request.EventCreateRequest;
import com.fitmap.function.v2.payload.request.GymCreateRequest;
import com.fitmap.function.v2.payload.request.GymEditRequest;
import com.fitmap.function.v2.payload.request.SubscriptionPlanCreateRequest;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.MethodNotAllowedException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GymFunction {

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

        var dto = ReadRequestService.getBody(request, GymCreateRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        var created = create(dto, ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static void doPut(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, GymEditRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        update(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void doDelete(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, GymEditRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        remove(dto, ReadRequestService.getUserId(request));

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void update(GymEditRequest dto, String gymId) {

        var sports = Objects.requireNonNullElse(dto.getSports(), new ArrayList<String>());

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setSports(sports);
        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var gym = DomainMapper.from(dto, gymId);

        GymService.updateProps(gym);
    }

    private static void remove(GymEditRequest dto, String gymId) {

        var sports = Objects.requireNonNullElse(dto.getSports(), new ArrayList<String>());

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setSports(sports);
        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var gym = DomainMapper.from(dto, gymId);

        GymService.removeElementsFromArraysProps(gym);
    }

    private static Gym create(GymCreateRequest dto, String gymId) {

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

        var gym = DomainMapper.from(dto, gymId);

        return GymService.create(gym);
    }

    private static List<Gym> find(List<String> ids) {

        if(ids.isEmpty()) {

            return Collections.emptyList();
        }

        return GymService.find(ids);
    }

}
