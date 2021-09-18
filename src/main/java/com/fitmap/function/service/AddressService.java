package com.fitmap.function.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHashQuery;
import com.firebase.geofire.util.GeoUtils;
import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.domain.Student;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    @SneakyThrows
    public static List<Address> findInSuperAddressCollection(List<String> addressesIds) {

        if(CollectionUtils.isEmpty(addressesIds)) {
            return Collections.emptyList();
        }

        return db
                 .collection(Address.ADDRESSES_COLLECTION)
                 .whereIn(FieldPath.documentId(), addressesIds)
                 .get()
                 .get()
                 .toObjects(Address.class);

    }

    @SneakyThrows
    public static List<Address> findAddressesNearBy(double latitude, double longitude, double radius) {

        if(radius <= 0 || radius > 5) {
            return Collections.emptyList();
        }

        var radiusInM = radius * 1000; // metre convert

        var centerLocation = new GeoLocation(latitude, longitude);

        var geoHashQueries = GeoHashQuery.queriesAtLocation(centerLocation, radiusInM);

        var snapshots = new ArrayList<QuerySnapshot>();

        var orderBy = db.collection(Address.ADDRESSES_COLLECTION).orderBy(Address.GEO_HASH);

        for (var geoQuery : geoHashQueries) {

            var query = orderBy
                .startAt(geoQuery.getStartValue())
                .endAt(geoQuery.getEndValue());

            snapshots.add(query.get().get());
        }

        var matchingDocs = new ArrayList<DocumentSnapshot>();

        snapshots.forEach(snapshot -> {

            for (var doc : snapshot.getDocuments()) {

                String latString = doc.getString(Address.LATITUDE);
                String lngString = doc.getString(Address.LONGITUDE);
                
                if(StringUtils.isNoneBlank(latString, lngString)) {

                    double lat = Double.parseDouble(latString);
                    double lng = Double.parseDouble(lngString);
                    GeoLocation docLocation = new GeoLocation(lat, lng);
                    double distance = GeoUtils.distance(docLocation, centerLocation);
                    if (distance <= radiusInM) {
                        matchingDocs.add(doc);
                    }

                }
            }

        });

        return matchingDocs.stream().map(doc -> doc.toObject(Address.class)).collect(Collectors.toList());
    }

    @SneakyThrows
    public static List<Address> find(String superEntityId, String superCollection) {

        var docRef = db.collection(superCollection).document(superEntityId).collection(Address.ADDRESSES_COLLECTION);

        return docRef.get().get().toObjects(Address.class);
    }

    private static Object findSuperEntity(String superEntityId, String superCollection) {

        var superIdList = List.of(superEntityId);

        switch (superCollection) {
            case Student.STUDENTS_COLLECTION:
                var students = StudentService.find(superIdList);
                if(CollectionUtils.isEmpty(students)) {
                    throw new TerminalException("Student, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
                }
                return students.get(0);
            case Gym.GYMS_COLLECTION:
                var gyms = GymService.find(superIdList);
                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
                }
                return gyms.get(0);
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:
                var personalTrainers = PersonalTrainerService.find(superIdList);
                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal Trainer, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
                }
                return personalTrainers.get(0);
            default:
                throw new TerminalException("Entity, " + superEntityId + ", not found.", HttpStatus.NOT_FOUND);
        }
    }

	public static List<Address> create(String superEntityId, String superCollection, List<Address> addresses) {

        var superEntity = findSuperEntity(superEntityId, superCollection);

        var batch = db.batch();

        var collRef = db.collection(superCollection).document(superEntityId).collection(Address.ADDRESSES_COLLECTION);

        var subEntitiesPerDocRef = addresses.stream().map(address -> {
            var ref = collRef.document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(addresses);

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);
        var masterAddressPerDocRef = new ArrayList<Pair<Address, DocumentReference>>();

        subEntitiesPerDocRef.forEach(pair -> {
            batch.create(pair.getRight(), pair.getLeft());

            Address newAddress = null;
            switch (superCollection) {
                case Student.STUDENTS_COLLECTION:
                    newAddress = pair.getLeft().withStudent((Student) superEntity);
                    break;
                case Gym.GYMS_COLLECTION:
                    newAddress = pair.getLeft().withGym((Gym) superEntity);
                    break;
                case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:
                    newAddress = pair.getLeft().withPersonalTrainer((PersonalTrainer) superEntity);
                    break;
                default:
                    return;
            }

            var newAddressDocRef = addressesCollRef.document(newAddress.getId());

            masterAddressPerDocRef.add(Pair.of(newAddress, newAddressDocRef));
        });
        masterAddressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        commit(batch);

        // ensureOnlyOneMainAddress(superEntityId, superCollection, addresses);

        return addresses;

	}

    private static void ensureOnlyOneMainAddress(String superEntityId, String superCollection, List<Address> addresses) {

        var createdOpt = addresses.stream().filter(Address::isMainAddress).findAny();

        if(createdOpt.isPresent()) {

            var allAddresses = find(superEntityId, superCollection);

            var mainAddresses = allAddresses.stream().filter(Address::isMainAddress).collect(Collectors.toList());

            if(mainAddresses.size() > 1) {

                mainAddresses.remove(createdOpt.get());

                mainAddresses.forEach(a -> a.setMainAddress(false));

                edit(superEntityId, superCollection, mainAddresses);
            } else if(mainAddresses.isEmpty() && !allAddresses.isEmpty()) {

                var updateToMain = allAddresses.get(0);
                updateToMain.setMainAddress(true);
                edit(superEntityId, superCollection, List.of(updateToMain));
            }

        }

    }

    public static Map<Address, List<Event>> getEventsPerAddress(String superEntityId, String superCollection) {

        var superEntityIdInList = List.of(superEntityId);

        var eventsPerAddress = new HashMap<Address, List<Event>>();

        switch (superCollection) {
            case Gym.GYMS_COLLECTION:

                var gyms = GymService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(gyms)) {
                    throw new TerminalException("Gym not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var gym = gyms.get(0);

                eventsPerAddress.putAll(gym.getEvents().stream().filter(e -> e.getAddress() != null).collect(Collectors.groupingBy(Event::getAddress)));

                break;
            case PersonalTrainer.PERSONAL_TRAINERS_COLLECTION:

                var personalTrainers = PersonalTrainerService.find(superEntityIdInList);

                if(CollectionUtils.isEmpty(personalTrainers)) {
                    throw new TerminalException("Personal trainer not found. id=" + superEntityId, HttpStatus.NOT_FOUND);
                }

                var personalTrainer = personalTrainers.get(0);

                eventsPerAddress.putAll(personalTrainer.getEvents().stream().filter(e -> e.getAddress() != null).collect(Collectors.groupingBy(Event::getAddress)));

                break;
        }

        return eventsPerAddress;
    }

    public static List<Address> edit(String superEntityId, String superCollection, List<Address> addresses) {

        var eventsPerAddress = getEventsPerAddress(superEntityId, superCollection);

        var batch = db.batch();

        var superEntityDocRef = db.collection(superCollection).document(superEntityId);
        var subAddressesCollRef = superEntityDocRef.collection(Address.ADDRESSES_COLLECTION);
        var subEventCollRef = superEntityDocRef.collection(Event.EVENTS_COLLECTION);
        var superAddressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);

        addresses.forEach(address -> {

            CheckConstraintsRequestBodyService.checkConstraints(address);

            var subFields = address.createPropertiesMap();
            subFields.remove(Address.GYM);
            subFields.remove(Address.PERSONAL_TRAINER);
            subFields.remove(Address.STUDENT);
            subFields.remove(Address.EVENTS);

            var subDocRef = subAddressesCollRef.document(address.getId());
            var superDocRef = superAddressesCollRef.document(address.getId());
            var events = eventsPerAddress.get(address);

            if(CollectionUtils.isNotEmpty(events)) {

                events.forEach(event -> {
                    event.setAddress(address);
                    var eventDocref = subEventCollRef.document(event.getId());
                    batch.update(eventDocref, Event.ADDRESS, event.getAddress());
                });
            }

            var superFields = address.withEvents(events).createPropertiesMap();
            superFields.remove(Address.GYM);
            superFields.remove(Address.PERSONAL_TRAINER);
            superFields.remove(Address.STUDENT);

            batch.update(subDocRef, subFields);
            batch.update(superDocRef, superFields);

        });

        commit(batch);

        // ensureOnlyOneMainAddress(superEntityId, superCollection, addresses);

        return addresses;

    }

    public static void delete(String superEntityId, String superCollection, List<String> addressesIds) {

        var eventsPerAddress = getEventsPerAddress(superEntityId, superCollection);

        var eventsPerAddressId = eventsPerAddress.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getId(), e -> e.getValue()));

        var batch = db.batch();

        var superEntityDocRef = db.collection(superCollection).document(superEntityId);
        var subAddressesCollRef = superEntityDocRef.collection(Address.ADDRESSES_COLLECTION);
        var subEventCollRef = superEntityDocRef.collection(Event.EVENTS_COLLECTION);
        var superAddressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);;

        addressesIds.forEach(id -> {

            var subDocRef = subAddressesCollRef.document(id);
            var superDocRef = superAddressesCollRef.document(id);
            var events = eventsPerAddressId.get(id);

            if(CollectionUtils.isNotEmpty(events)) {

                events.forEach(event -> {
                    event.setAddress(null);
                    var eventDocref = subEventCollRef.document(event.getId());
                    batch.update(eventDocref, Event.ADDRESS, event.getAddress());
                });
            }

            batch.delete(subDocRef);
            batch.delete(superDocRef);
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
