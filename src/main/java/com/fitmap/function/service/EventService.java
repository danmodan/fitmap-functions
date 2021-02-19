package com.fitmap.function.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    @SneakyThrows
    public static List<Event> find(String superEntityId, String superCollection) {

        var docRef = db.collection(superCollection).document(superEntityId).collection(Event.EVENTS_COLLECTION);

        return docRef.get().get().toObjects(Event.class);
    }

    public static List<Event> create(String superEntityId, String superCollection, List<Event> events) {

        var batch = db.batch();

        var collRef = db.collection(superCollection).document(superEntityId).collection(Event.EVENTS_COLLECTION);

        var subEntitiesPerDocRef = events.stream().map(event -> {
            var ref = collRef.document();
            event.setId(ref.getId());
            return Pair.of(event, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(events);

        subEntitiesPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        var eventsPerAddressIds = events
            .stream()
            .filter(event -> StringUtils.isNotBlank(event.getAddressId()))
            .collect(Collectors.groupingBy(Event::getAddressId));

        eventsPerAddressIds.forEach((addressId, eventList) -> {

            var addressDocRef = addressesCollRef.document(addressId);

            var eventsIds = eventList.stream().map(Event::getId).collect(Collectors.toList()).toArray(Object[]::new);

            batch.update(addressDocRef, Address.EVENTS_IDS, FieldValue.arrayUnion(eventsIds));
        });

        commit(batch);

        return events;

	}

    public static List<Event> edit(String superEntityId, String superCollection, List<Event> events) {

        var superEntityIdInList = List.of(superEntityId);

        var editedAddresses = new ArrayList<Triple<String, String, String>>();

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:

                var gyms = GymService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var gym = gyms.get(0);

                var gymEventPerId = gym.getEvents().stream().collect(Collectors.toMap(Event::getId, Function.identity()));

                for(var event : events) {

                    var currentEvent = gymEventPerId.get(event.getId());

                    if(StringUtils.isNotBlank(currentEvent.getAddressId()) && !currentEvent.getAddressId().equals(event.getAddressId())) {
                        editedAddresses.add(Triple.of(currentEvent.getAddressId(), event.getAddressId(), event.getId()));
                    }
                }

                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:

                var personalTrainers = PersonalTrainerService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal trainer not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var personalTrainer = personalTrainers.get(0);

                var personalTrainerEventPerId = personalTrainer.getEvents().stream().collect(Collectors.toMap(Event::getId, Function.identity()));

                for(var event : events) {

                    var currentEvent = personalTrainerEventPerId.get(event.getId());

                    if(StringUtils.isNotBlank(currentEvent.getAddressId()) && !currentEvent.getAddressId().equals(event.getAddressId())) {
                        editedAddresses.add(Triple.of(currentEvent.getAddressId(), event.getAddressId(), event.getId()));
                    }
                }

                break;
        }

        var batch = db.batch();

        var eventsCollection = db.collection(superCollection).document(superEntityId).collection(Event.EVENTS_COLLECTION);

        events.forEach(event -> {

            CheckConstraintsRequestBodyService.checkConstraints(event);

            var docRef = eventsCollection.document(event.getId());

            var fields = event.createPropertiesMap();

            batch.update(docRef, fields);
        });

        if(CollectionUtils.isNotEmpty(editedAddresses)) {

            var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

            var eventsToUnion = new HashMap<String, List<String>>();
            var eventsToRemove = new HashMap<String, List<String>>();

            editedAddresses.forEach(triple -> {
                var currentId = triple.getLeft();
                var newId = triple.getMiddle();
                var eventId = triple.getRight();

                if(StringUtils.isBlank(currentId)) {

                    var eventsIds = eventsToUnion.get(newId);

                    if(eventsIds == null) {
                        eventsToUnion.put(newId, new ArrayList<String>());
                    }

                    eventsIds = eventsToUnion.get(newId);

                    eventsIds.add(eventId);

                } else {

                    var eventsIds = eventsToRemove.get(currentId);

                    if(eventsIds == null) {
                        eventsToUnion.put(currentId, new ArrayList<String>());
                    }

                    eventsIds = eventsToRemove.get(currentId);

                    eventsIds.add(eventId);

                    if(StringUtils.isNotBlank(newId)) {

                        var eventsIds2 = eventsToUnion.get(newId);

                        if(eventsIds2 == null) {
                            eventsToUnion.put(newId, new ArrayList<String>());
                        }
    
                        eventsIds2 = eventsToUnion.get(newId);
    
                        eventsIds2.add(eventId);
    
                    }
                }
            });

            eventsToUnion.forEach((addressId, eventsIds) -> batch.update(addressesCollRef.document(addressId), Address.EVENTS_IDS, FieldValue.arrayUnion(eventsIds.toArray(Object[]::new))));
            eventsToRemove.forEach((addressId, eventsIds) -> batch.update(addressesCollRef.document(addressId), Address.EVENTS_IDS, FieldValue.arrayRemove(eventsIds.toArray(Object[]::new))));
        }

        commit(batch);

        return events;

    }

    public static void delete(String superEntityId, String superCollection, List<String> eventsIds) {

        var batch = db.batch();

        var eventsCollection = db.collection(superCollection).document(superEntityId).collection(Event.EVENTS_COLLECTION);

        eventsIds.forEach(id -> {

            var docRef = eventsCollection.document(id);

            batch.delete(docRef);
        });

        commit(batch);
    }

    private static void commit(WriteBatch batch) {
        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
