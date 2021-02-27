package com.fitmap.function.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.mapper.DtoMapper;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.ContactService;
import com.fitmap.function.service.GymService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.GymCreateRequest;
import com.fitmap.function.v2.payload.request.GymEditRequest;
import com.fitmap.function.v2.payload.response.GymResponse;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.apache.commons.collections4.CollectionUtils;
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

    private static void update(GymEditRequest dto, String gymId) {

        var found = GymService.find(List.of(gymId));

        if(CollectionUtils.isEmpty(found)) {

            throw new TerminalException("Gym, " + gymId + ", does not exists.", HttpStatus.NOT_FOUND);
        }

        var current = found.get(0);

        var toUpdate = DtoMapper.from(dto, gymId);

        GymService.updateProps(toUpdate);

        updateAddresses(current, toUpdate);
        updateContacts(current, toUpdate);
    }

    private static void updateAddresses(Gym current, Gym toUpdate) {

        var gymId = current.getId();
        var currentAddresses = current.getAddresses();
        var toUpdateAddresses = toUpdate.getAddresses();

        if(toUpdateAddresses.isEmpty() && !currentAddresses.isEmpty()) {

            var addressesIds = currentAddresses
                .stream()
                .map(Address::getId)
                .collect(Collectors.toList());

            AddressService.delete(gymId, Gym.GYMS_COLLECTION, addressesIds);
            return;
        }

        if(currentAddresses.isEmpty() && !toUpdateAddresses.isEmpty()) {

            toUpdateAddresses.forEach(a -> a.setMainAddress(true));

            AddressService.create(gymId, Gym.GYMS_COLLECTION, toUpdateAddresses);
            return;
        }

        if(!currentAddresses.isEmpty() && !toUpdateAddresses.isEmpty()) {

            toUpdateAddresses.forEach(a -> {
                a.setMainAddress(true);
                a.setId(currentAddresses.get(0).getId());
            });

            AddressService.edit(gymId, Gym.GYMS_COLLECTION, toUpdateAddresses);
            return;
        }

    }

    private static void updateContacts(Gym current, Gym toUpdate) {

        var gymId = current.getId();
        var currentContacts = current.getContacts();
        var toUpdateContacts = toUpdate.getContacts();

        if(toUpdateContacts.isEmpty() && !currentContacts.isEmpty()) {

            var contactsIds = currentContacts
                .stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

            ContactService.delete(gymId, Gym.GYMS_COLLECTION, contactsIds);
            return;
        }

        if(currentContacts.isEmpty() && !toUpdateContacts.isEmpty()) {

            toUpdateContacts.forEach(c -> c.setMainContact(true));

            ContactService.create(gymId, Gym.GYMS_COLLECTION, toUpdateContacts);
            return;
        }

        if(!currentContacts.isEmpty() && !toUpdateContacts.isEmpty()) {

            toUpdateContacts.forEach(c -> {
                c.setMainContact(true);
                c.setId(currentContacts.get(0).getId());
            });

            ContactService.edit(gymId, Gym.GYMS_COLLECTION, toUpdateContacts);
            return;
        }

    }

    private static GymResponse create(GymCreateRequest dto, String gymId) {

        var found = find(List.of(gymId));

        if(CollectionUtils.isNotEmpty(found)) {

            throw new TerminalException("Gym, " + gymId + ", already exists.", HttpStatus.CONFLICT);
        }

        var gym = DtoMapper.from(dto, gymId);
        gym.getEvents().forEach(e -> {
            e.setAddress(null);
            e.setContact(null);
        });

        if(CollectionUtils.isNotEmpty(gym.getContacts())) {

            gym.getContacts().forEach(c -> c.setMainContact(true));
        }

        if(CollectionUtils.isNotEmpty(gym.getAddresses())) {

            gym.getAddresses().forEach(a -> a.setMainAddress(true));
        }

        return DtoMapper.from(GymService.create(gym));
    }

    private static List<GymResponse> find(List<String> ids) {

        if(ids.isEmpty()) {

            Collections.emptyList();
        }

        return DtoMapper.from(GymService.find(ids), DtoMapper::from);
    }

}
