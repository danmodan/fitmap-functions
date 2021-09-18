package com.fitmap.function.v2;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.SystemTimeZoneConfig;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.mapper.DtoMapper;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.EventService;
import com.fitmap.function.service.GymService;
import com.fitmap.function.service.PersonalTrainerService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.util.Constants;
import com.fitmap.function.v2.payload.request.EventCreateRequest;
import com.fitmap.function.v2.payload.request.EventEditRequest;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.extern.java.Log;

@Log
public class EventFunction implements HttpFunction {

    public EventFunction() {

        log.log(Level.INFO, "init EventFunction. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/gym/events":
                    doService(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v2/personal-trainer/events":
                    doService(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
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

    public static void doService(HttpRequest request, HttpResponse response, String superCollection) {

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

    private static List<Event> find(String superEntityId, String superCollection) {

        return EventService.find(superEntityId, superCollection);
    }

    private static void doPost(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, EventCreateRequest[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        var created = create(Arrays.asList(dto), ReadRequestService.getUserId(request), superCollection);

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private static List<Event> create(List<EventCreateRequest> dtos, String superEntityId, String superCollection) {

        createAddresses(dtos, superEntityId, superCollection);

        var subEntities = DtoMapper.from(dtos, DtoMapper::from);

        return EventService.create(superEntityId, superCollection, subEntities);
    }

    private static void doPut(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, EventEditRequest[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        for(var item : dto) {
            CheckConstraintsRequestBodyService.checkConstraints(item);
        }

        edit(Arrays.asList(dto), ReadRequestService.getUserId(request), superCollection);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static List<Event> edit(List<EventEditRequest> dtos, String superEntityId, String superCollection) {

        editAddresses(dtos, superEntityId, superCollection);

        var subEntities = DtoMapper.from(dtos, DtoMapper::from);

        var updatedEvents = EventService.edit(superEntityId, superCollection, subEntities);

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:
                var gym = GymService.find(List.of(superEntityId)).get(0);
                removeUnusedAddress(superEntityId, superCollection, gym.findUnusedAddress());
                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:
                var personalTrainer = PersonalTrainerService.find(List.of(superEntityId)).get(0);
                removeUnusedAddress(superEntityId, superCollection, personalTrainer.findUnusedAddress());
                break;
        }

        return updatedEvents;
    }

    private static void removeUnusedAddress(String superEntityId, String superCollection, List<Address> unusedAddress) {

        if(CollectionUtils.isEmpty(unusedAddress)) {
            return;
        }

        var addressesIds = unusedAddress
            .stream()
            .map(Address::getId)
            .collect(Collectors.toList());

        AddressService.delete(superEntityId, superCollection, addressesIds);
    }

    private static void doDelete(HttpRequest request, HttpResponse response, String superCollection) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, String[].class);

        CheckConstraintsRequestBodyService.checkNotEmpty(dto);

        var userId = ReadRequestService.getUserId(request);

        delete(Arrays.asList(dto), userId, superCollection);

        ResponseService.fillResponseWithStatus(response, HttpStatus.NO_CONTENT);
    }

    private static void delete(List<String> eventsIds, String superEntityId, String superCollection) {

        EventService.delete(superEntityId, superCollection, eventsIds);
    }

    private static void createAddresses(List<EventCreateRequest> dtos, String superEntityId, String superCollection) {

        if(CollectionUtils.isEmpty(dtos)) {
            return;
        }

        for(var dto : dtos) {

            var address = DtoMapper.from(dto.getAddress());

            if(address == null || BooleanUtils.toBoolean(dto.getIsOnline())) {
                dto.setAddressId(null);
                continue;
            }

            var created = AddressService.create(superEntityId, superCollection, List.of(address));

            if(CollectionUtils.isEmpty(created)) {
                continue;
            }

            dto.setAddressId(created.get(0).getId());
        }
    }

    private static void editAddresses(List<EventEditRequest> dtos, String superEntityId, String superCollection) {

        if(CollectionUtils.isEmpty(dtos)) {
            return;
        }

        for(var dto : dtos) {

            var address = DtoMapper.from(dto.getAddress());
            var currentAddressId = dto.getAddressId();

            if(BooleanUtils.toBoolean(dto.getIsOnline()) || (StringUtils.isBlank(currentAddressId) && address == null)) {
                dto.setAddressId(null);
                continue;
            }

            if(StringUtils.isBlank(currentAddressId) && address != null) {
                var created = AddressService.create(superEntityId, superCollection, List.of(address));

                if(CollectionUtils.isEmpty(created)) {
                    continue;
                }

                dto.setAddressId(created.get(0).getId());
                continue;
            }

            if(StringUtils.isNotBlank(currentAddressId) && address != null) {

                var created = AddressService.create(superEntityId, superCollection, List.of(address));

                AddressService.delete(superEntityId, superCollection, List.of(currentAddressId));

                if(CollectionUtils.isEmpty(created)) {
                    continue;
                }

                dto.setAddressId(created.get(0).getId());
                continue;
            }
        }
    }
}
