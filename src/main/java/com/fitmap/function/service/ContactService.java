package com.fitmap.function.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.exception.TerminalException;
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
public class ContactService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    @SneakyThrows
    public static List<Contact> find(String superEntityId, String superCollection) {

        var docRef = db.collection(superCollection).document(superEntityId).collection(Contact.CONTACTS_COLLECTION);

        return docRef.get().get().toObjects(Contact.class);
    }

	public static List<Contact> create(String superEntityId, String superCollection, List<Contact> contacts) {

        var batch = db.batch();

        var collRef = db.collection(superCollection).document(superEntityId).collection(Contact.CONTACTS_COLLECTION);

        var subEntitiesPerDocRef = contacts.stream().map(contact -> {
            var ref = collRef.document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(contacts);

        subEntitiesPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        commit(batch);

        ensureOnlyOneMainContact(superEntityId, superCollection, contacts);

        return contacts;

	}

    private static void ensureOnlyOneMainContact(String superEntityId, String superCollection, List<Contact> contacts) {

        var createdOpt = contacts.stream().filter(Contact::isMainContact).findAny();

        if(createdOpt.isPresent()) {

            var allContacts = find(superEntityId, superCollection);

            var mainContacts = allContacts.stream().filter(Contact::isMainContact).collect(Collectors.toList());

            if(mainContacts.size() > 1) {

                mainContacts.remove(createdOpt.get());

                mainContacts.forEach(a -> a.setMainContact(false));

                edit(superEntityId, superCollection, mainContacts);
            } else if(mainContacts.isEmpty() && !allContacts.isEmpty()) {

                var updateToMain = allContacts.get(0);
                updateToMain.setMainContact(true);
                edit(superEntityId, superCollection, List.of(updateToMain));
            }

        }

    }

    public static List<Contact> edit(String superEntityId, String superCollection, List<Contact> contacts) {

        var eventsPerContact = getEventsPerContact(superEntityId, superCollection);

        var addressesIds = eventsPerContact
            .values()
            .stream()
            .flatMap(List::stream)
            .filter(e -> e.getAddress() != null)
            .map(e -> e.getAddress().getId())
            .collect(Collectors.toList());

        Map<String, Address> superAddressPerId = AddressService
            .findInSuperAddressCollection(addressesIds)
            .stream()
            .collect(Collectors.toMap(Address::getId, Function.identity()));

        var batch = db.batch();

        var superEntityDocRef = db.collection(superCollection).document(superEntityId);
        var contactsCollection = superEntityDocRef.collection(Contact.CONTACTS_COLLECTION);
        var subEventCollRef = superEntityDocRef.collection(Event.EVENTS_COLLECTION);
        var superAddressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        contacts.forEach(contact -> {

            CheckConstraintsRequestBodyService.checkConstraints(contact);

            var contactDocRef = contactsCollection.document(contact.getId());
            var events = eventsPerContact.get(contact);

            if(CollectionUtils.isNotEmpty(events)) {

                events.forEach(event -> {
                    event.setContact(contact);
                    var eventDocref = subEventCollRef.document(event.getId());
                    batch.update(eventDocref, Event.CONTACT, event.getContact());

                    var superAddressDocRef = superAddressesCollRef.document(event.getAddress().getId());

                    var superAddress = superAddressPerId.get(event.getAddress().getId());
                    superAddress.getEvents().remove(event);
                    superAddress.getEvents().add(event);
                    batch.update(superAddressDocRef, Address.EVENTS, superAddress.getEvents());
                });
            }

            var fields = contact.createPropertiesMap();

            batch.update(contactDocRef, fields);
        });

        commit(batch);

        ensureOnlyOneMainContact(superEntityId, superCollection, contacts);

        return contacts;

    }

    public static Map<Contact, List<Event>> getEventsPerContact(String superEntityId, String superCollection) {

        var superEntityIdInList = List.of(superEntityId);

        var eventsPerContact = new HashMap<Contact, List<Event>>();

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:

                var gyms = GymService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var gym = gyms.get(0);

                eventsPerContact.putAll(gym.getEvents().stream().filter(e -> e.getContact() != null).collect(Collectors.groupingBy(Event::getContact)));

                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:

                var personalTrainers = PersonalTrainerService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal trainer not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var personalTrainer = personalTrainers.get(0);

                eventsPerContact.putAll(personalTrainer.getEvents().stream().filter(e -> e.getContact() != null).collect(Collectors.groupingBy(Event::getContact)));

                break;
        }

        return eventsPerContact;
    }

    public static void delete(String superEntityId, String superCollection, List<String> contactsIds) {

        var eventsPerContactId = getEventsPerContact(superEntityId, superCollection).entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().getId(), Entry::getValue));

        var addressesIds = eventsPerContactId
            .values()
            .stream()
            .flatMap(List::stream)
            .filter(e -> e.getAddress() != null)
            .map(e -> e.getAddress().getId())
            .collect(Collectors.toList());

        Map<String, Address> superAddressPerId = AddressService
            .findInSuperAddressCollection(addressesIds)
            .stream()
            .collect(Collectors.toMap(Address::getId, Function.identity()));

        var batch = db.batch();

        var superEntityDocRef = db.collection(superCollection).document(superEntityId);
        var contactsCollection = superEntityDocRef.collection(Contact.CONTACTS_COLLECTION);
        var subEventCollRef = superEntityDocRef.collection(Event.EVENTS_COLLECTION);
        var superAddressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        contactsIds.forEach(id -> {

            var events = eventsPerContactId.get(id);

            if(CollectionUtils.isNotEmpty(events)) {

                events.forEach(event -> {
                    event.setContact(null);
                    var eventDocref = subEventCollRef.document(event.getId());
                    batch.update(eventDocref, Event.CONTACT, event.getContact());

                    var superAddressDocRef = superAddressesCollRef.document(event.getAddress().getId());

                    var superAddress = superAddressPerId.get(event.getAddress().getId());
                    superAddress.getEvents().remove(event);
                    superAddress.getEvents().add(event);
                    batch.update(superAddressDocRef, Address.EVENTS, superAddress.getEvents());
                });
            }

            batch.delete(contactsCollection.document(id));
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
