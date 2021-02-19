package com.fitmap.function.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.domain.Event;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.SubscriptionPlan;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class GymService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    public static Gym create(Gym gym) {

        var now = new Date();
        gym.setCreatedAt(now);
        gym.setUpdatedAt(now);

        var batch = db.batch();

        var gymDocRef = db.collection(Gym.GYMS_COLLECTION).document(gym.getId());

        var addressPerDocRef = gym.getAddresses().stream().map(address -> {
            var ref = gymDocRef.collection(Address.ADDRESSES_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = gym.getContacts().stream().map(contact -> {
            var ref = gymDocRef.collection(Contact.CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        var eventsPerDocRef = gym.getEvents().stream().map(event -> {
            var ref = gymDocRef.collection(Event.EVENTS_COLLECTION).document();
            event.setId(ref.getId());
            return Pair.of(event, ref);
        }).collect(Collectors.toSet());

        var subscriptionPlansPerDocRef = gym.getSubscriptionPlans().stream().map(subscriptionPlan -> {
            var ref = gymDocRef.collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION).document();
            subscriptionPlan.setId(ref.getId());
            return Pair.of(subscriptionPlan, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(gym);

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);
        var masterAddressPerDocRef = new ArrayList<Pair<Address, DocumentReference>>();

        batch.create(gymDocRef, gym);
        addressPerDocRef.forEach(pair -> {
            batch.create(pair.getRight(), pair.getLeft());
            var newAddress = pair.getLeft().withGymId(gym.getId());
            var newAddressDocRef = addressesCollRef.document(newAddress.getId());
            masterAddressPerDocRef.add(Pair.of(newAddress, newAddressDocRef));
        });
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        eventsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        subscriptionPlansPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        masterAddressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return gym;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @SneakyThrows
    public static List<Gym> find(List<String> gymIds) {

        var gyms = new ArrayList<Gym>();

        db
            .collection(Gym.GYMS_COLLECTION)
            .whereIn(FieldPath.documentId(), gymIds)
            .get()
            .get()
            .forEach(queryDocSnapshot -> {

                try {

                    var docRef = queryDocSnapshot.getReference();
                    var contactsColl = docRef.collection(Contact.CONTACTS_COLLECTION).get();
                    var addressColl = docRef.collection(Address.ADDRESSES_COLLECTION).get();
                    var eventsColl = docRef.collection(Event.EVENTS_COLLECTION).get();
                    var subscriptionPlansColl = docRef.collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION).get();

                    var gym = queryDocSnapshot.toObject(Gym.class);
                    var contacts = contactsColl.get().toObjects(Contact.class);
                    var addresses = addressColl.get().toObjects(Address.class);
                    var events = eventsColl.get().toObjects(Event.class);
                    var subscriptionPlans = subscriptionPlansColl.get().toObjects(SubscriptionPlan.class);

                    gym.addContacts(contacts);
                    gym.addAddresses(addresses);
                    gym.addEvents(events);
                    gym.addSubscriptionPlan(subscriptionPlans);
                    gyms.add(gym);

                } catch (Exception e) { }
            });

        return gyms;

    }

    @SneakyThrows
    public static void updateProps(Gym gym) {

        var docRef = db.collection(Gym.GYMS_COLLECTION).document(gym.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put(Gym.UPDATED_AT, new Date());

        if(StringUtils.isNotEmpty(gym.getBiography())) {
            propsToUpdate.put(Gym.BIOGRAPHY, gym.getBiography());
        }

        if(StringUtils.isNotBlank(gym.getProfileName())) {
            propsToUpdate.put(Gym.PROFILE_NAME, gym.getProfileName());
        }

        if(gym.getGalleryPicturesUrls().size() > 0) {
            propsToUpdate.put(Gym.GALLERY_PICTURES_URLS, FieldValue.arrayUnion(gym.getGalleryPicturesUrls().toArray(Object[]::new)));
        }

        if(gym.getSports().size() > 0) {
            propsToUpdate.put(Gym.SPORTS, FieldValue.arrayUnion(gym.getSports().toArray(Object[]::new)));
        }

        docRef.update(propsToUpdate).get();
    }

    @SneakyThrows
    public static void removeElementsFromArraysProps(Gym gym) {

        var docRef = db.collection(Gym.GYMS_COLLECTION).document(gym.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put(Gym.UPDATED_AT, new Date());

        if(gym.getGalleryPicturesUrls().size() > 0) {
            propsToUpdate.put(Gym.GALLERY_PICTURES_URLS, FieldValue.arrayRemove(gym.getGalleryPicturesUrls().toArray(Object[]::new)));
        }

        if(gym.getSports().size() > 0) {
            propsToUpdate.put(Gym.SPORTS, FieldValue.arrayRemove(gym.getSports().toArray(Object[]::new)));
        }

        docRef.update(propsToUpdate).get();
    }

}
