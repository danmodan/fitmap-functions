package com.fitmap.function.personaltrainercontext.v1;

import java.util.List;
import java.util.function.Function;

import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.commonfirestore.config.FirestoreConfig;
import com.fitmap.function.commonfirestore.function.SubCollectionsCrudFunction;
import com.fitmap.function.gymcontext.domain.Event;
import com.fitmap.function.gymcontext.service.EventService;
import com.fitmap.function.gymcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.gymcontext.v1.payload.request.EditRequestDtos;

import lombok.extern.java.Log;

@Log
public class CrudEventFunction extends SubCollectionsCrudFunction<Event, EditRequestDtos.Event, CreateRequestDtos.Event> {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    public CrudEventFunction() {
        super(new EventService(FirestoreConfig.FIRESTORE));

        log.info("awake function");
    }

    @Override
    protected Class<EditRequestDtos.Event[]> getEditRequestDtoClass() {
        return EditRequestDtos.Event[].class;
    }

    @Override
    protected Class<CreateRequestDtos.Event[]> getCreateRequestDtoClass() {
        return CreateRequestDtos.Event[].class;
    }

    @Override
    protected Function<List<CreateRequestDtos.Event>, List<Event>> mapCreationDtosToSubEntities() {
        return dtos -> Event.from(dtos, Event::from);
    }

    @Override
    protected Function<List<EditRequestDtos.Event>, List<Event>> mapEditDtosToSubEntities() {
        return dtos -> Event.from(dtos, Event::from);
    }

}
