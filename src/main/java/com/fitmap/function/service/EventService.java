package com.fitmap.function.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import org.apache.commons.lang3.tuple.Pair;
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

        var eventsPerAddressIds = events
            .stream()
            .filter(event -> event.getAddress() != null)
            .collect(Collectors.groupingBy(Event::getAddress));

        var eventsPerContactIds = events
            .stream()
            .filter(event -> event.getContact() != null)
            .collect(Collectors.groupingBy(Event::getContact));

        var superIdList = List.of(superEntityId);

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:
                var gyms = GymService.find(superIdList);
                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
                }
                var gym = gyms.get(0);
                eventsPerAddressIds.forEach((address, eventList) -> eventList.forEach(e -> e.setAddress(gym.findAddressById(address.getId()).orElse(null))));
                eventsPerContactIds.forEach((contact, eventList) -> eventList.forEach(e -> e.setContact(gym.findContactById(contact.getId()).orElse(null))));
                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:
                var personalTrainers = PersonalTrainerService.find(superIdList);
                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal Trainer, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
                }
                var personalTrainer = personalTrainers.get(0);
                eventsPerAddressIds.forEach((address, eventList) -> eventList.forEach(e -> e.setAddress(personalTrainer.findAddressById(address.getId()).orElse(null))));
                eventsPerContactIds.forEach((contact, eventList) -> eventList.forEach(e -> e.setContact(personalTrainer.findContactById(contact.getId()).orElse(null))));
                break;
            default:
                throw new TerminalException("Entity, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
        }

        eventsPerAddressIds = events
            .stream()
            .filter(event -> event.getAddress() != null)
            .collect(Collectors.groupingBy(Event::getAddress));

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

        eventsPerAddressIds.forEach((address, eventList) -> {

            var addressDocRef = addressesCollRef.document(address.getId());

            batch.update(addressDocRef, Address.EVENTS, FieldValue.arrayUnion(eventList.toArray(Object[]::new)));
        });

        commit(batch);

        return events;

    }

    public static List<Event> edit(String superEntityId, String superCollection, List<Event> events) {

        var eventsPerAddressIds = events
            .stream()
            .filter(event -> event.getAddress() != null)
            .collect(Collectors.groupingBy(Event::getAddress));

        var eventsPerContactIds = events
            .stream()
            .filter(event -> event.getContact() != null)
            .collect(Collectors.groupingBy(Event::getContact));

        var superEntityIdInList = List.of(superEntityId);

        var currentNewEvents = new ArrayList<Pair<Event, Event>>();

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:

                var gyms = GymService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var gym = gyms.get(0);
                eventsPerAddressIds.forEach((address, eventList) -> eventList.forEach(e -> e.setAddress(gym.findAddressById(address.getId()).orElse(null))));
                eventsPerContactIds.forEach((contact, eventList) -> eventList.forEach(e -> e.setContact(gym.findContactById(contact.getId()).orElse(null))));

                var gymEventPerId = gym.getEvents().stream().collect(Collectors.toMap(Event::getId, Function.identity()));

                for(var newVersion : events) {

                    var currentVersion = gymEventPerId.get(newVersion.getId());
                    currentNewEvents.add(Pair.of(currentVersion, newVersion));
                }

                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:

                var personalTrainers = PersonalTrainerService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal trainer not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var personalTrainer = personalTrainers.get(0);
                eventsPerAddressIds.forEach((address, eventList) -> eventList.forEach(e -> e.setAddress(personalTrainer.findAddressById(address.getId()).orElse(null))));
                eventsPerContactIds.forEach((contact, eventList) -> eventList.forEach(e -> e.setContact(personalTrainer.findContactById(contact.getId()).orElse(null))));

                var personalTrainerEventPerId = personalTrainer.getEvents().stream().collect(Collectors.toMap(Event::getId, Function.identity()));

                for(var newVersion : events) {

                    var currentVersion = personalTrainerEventPerId.get(newVersion.getId());
                    currentNewEvents.add(Pair.of(currentVersion, newVersion));
                }

                break;
        }

        var addressesIdInvolved = new HashSet<String>();

        currentNewEvents.forEach(pair -> {
            var currentVersion = pair.getLeft();
            var newVersion = pair.getRight();

            if(currentVersion.getAddress() != null) {
                addressesIdInvolved.add(currentVersion.getAddress().getId());
            }

            if(newVersion.getAddress() != null) {
                addressesIdInvolved.add(newVersion.getAddress().getId());
            }
        });

        var addressPerId = AddressService.findInSuperAddressCollection(new ArrayList<>(addressesIdInvolved)).stream().collect(Collectors.toMap(Address::getId, Function.identity()));

        var batch = db.batch();

        var eventsCollection = db.collection(superCollection).document(superEntityId).collection(Event.EVENTS_COLLECTION);

        events.forEach(event -> {

            CheckConstraintsRequestBodyService.checkConstraints(event);

            var docRef = eventsCollection.document(event.getId());

            var fields = event.createPropertiesMap();

            batch.update(docRef, fields);
        });

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        currentNewEvents.forEach(pair-> {
            var currentVersion = pair.getLeft();
            var newVersion = pair.getRight();

            var currentAddress = currentVersion.getAddress();
            var newAddress = newVersion.getAddress();

            if(currentAddress == null && newAddress != null) {

                var toAdd = addressPerId.get(newAddress.getId());
                toAdd.addEvent(newVersion);
                batch.set(addressesCollRef.document(newAddress.getId()), toAdd);

            } else if(currentAddress != null && newAddress == null) {

                var toRemove = addressPerId.get(currentAddress.getId());
                toRemove.removeEvent(currentVersion);
                batch.set(addressesCollRef.document(currentAddress.getId()), toRemove);

            } else if(currentAddress != null && newAddress != null && !currentAddress.equals(newAddress)) {

                var toRemove = addressPerId.get(currentAddress.getId());
                toRemove.removeEvent(currentVersion);
                batch.set(addressesCollRef.document(currentAddress.getId()), toRemove);

                var toAdd = addressPerId.get(newAddress.getId());
                toAdd.addEvent(newVersion);
                batch.set(addressesCollRef.document(newAddress.getId()), toAdd);

            }
        });

        commit(batch);

        return events;

    }

    public static void delete(String superEntityId, String superCollection, List<String> eventsIds) {

        var superEntityIdInList = List.of(superEntityId);

        var addressesIds = new HashSet<String>();

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:

                var gyms = GymService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var gym = gyms.get(0);
                var gymAddressesId = gym.getEvents().stream().map(Event::getAddress).filter(Objects::nonNull).map(Address::getId).collect(Collectors.toSet());

                addressesIds.addAll(gymAddressesId);

                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:

                var personalTrainers = PersonalTrainerService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal trainer not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var personalTrainer = personalTrainers.get(0);
                var personalTrainerAddressesId = personalTrainer.getEvents().stream().map(Event::getAddress).filter(Objects::nonNull).map(Address::getId).collect(Collectors.toSet());

                addressesIds.addAll(personalTrainerAddressesId);

                break;
        }

        var addressPerId = AddressService.findInSuperAddressCollection(new ArrayList<>(addressesIds)).stream().collect(Collectors.toMap(Address::getId, Function.identity()));

        addressPerId.forEach((id, address) -> address.getEvents().removeIf(e -> eventsIds.contains(e.getId())));

        var batch = db.batch();

        var eventsCollection = db.collection(superCollection).document(superEntityId).collection(Event.EVENTS_COLLECTION);
        var addressCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        eventsIds.forEach(id -> {

            var docRef = eventsCollection.document(id);

            batch.delete(docRef);
        });

        addressPerId.forEach((id, address) -> batch.set(addressCollRef.document(id), address));

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
