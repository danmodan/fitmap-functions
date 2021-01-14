package com.fitmap.function.gymcontext.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
import com.fitmap.function.gymcontext.domain.Event;
import com.google.cloud.firestore.Firestore;

public class EventService extends SubCollectionsCrudService<Event> {

    public EventService(Firestore db) {
        super(db);
    }

    @Override
    protected String getSuperCollection() {
        return "gyms";
    }

    @Override
    protected String getSubCollection() {
        return "events";
    }

    @Override
    protected Class<Event> getSubEntityType() {
        return Event.class;
    }

    @Override
    protected BiConsumer<Event, String> updateId() {
        return Event::setId;
    }

    @Override
    protected Function<Event, String> getId() {
        return Event::getId;
    }

    @Override
    protected Function<Event, Map<String, Object>> getFieldsToUpdate() {

        return event -> {
            var props = new HashMap<String, Object>();

            props.put("name", event.getName());
            props.put("pictureUrl", event.getPictureUrl());
            props.put("description", event.getDescription());
            props.put("eventType", event.getEventType());
            props.put("beginAt", event.getBeginAt());
            props.put("endAt", event.getEndAt());
            props.put("eventCoach", event.getEventCoach());
            props.put("addressId", event.getAddressId());
            props.put("currentEventValue", event.getCurrentEventValue());
            props.put("originalEventValue", event.getOriginalEventValue());
            props.put("contactId", event.getContactId());
            props.put("showPhoneContact", event.getShowPhoneContact());
            props.put("showEmailContact", event.getShowEmailContact());

            return props;
        };
    }

}
