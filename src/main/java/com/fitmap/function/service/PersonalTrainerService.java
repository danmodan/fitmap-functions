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
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.domain.SubscriptionPlan;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class PersonalTrainerService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    public static PersonalTrainer create(PersonalTrainer personalTrainer) {

        var now = new Date();
        personalTrainer.setCreatedAt(now);
        personalTrainer.setUpdatedAt(now);

        var batch = db.batch();

        var personalTrainerDocRef = db.collection(PersonalTrainer.PERSONAL_TRAINERS_COLLECTION).document(personalTrainer.getId());

        var addressPerDocRef = personalTrainer.getAddresses().stream().map(address -> {
            var ref = personalTrainerDocRef.collection(Address.ADDRESSES_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = personalTrainer.getContacts().stream().map(contact -> {
            var ref = personalTrainerDocRef.collection(Contact.CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        var eventsPerDocRef = personalTrainer.getEvents().stream().map(event -> {
            var ref = personalTrainerDocRef.collection(Event.EVENTS_COLLECTION).document();
            event.setId(ref.getId());
            return Pair.of(event, ref);
        }).collect(Collectors.toSet());

        var subscriptionPlansPerDocRef = personalTrainer.getSubscriptionPlans().stream().map(subscriptionPlan -> {
            var ref = personalTrainerDocRef.collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION).document();
            subscriptionPlan.setId(ref.getId());
            return Pair.of(subscriptionPlan, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(personalTrainer);

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);
        var masterAddressPerDocRef = new ArrayList<Pair<Address, DocumentReference>>();

        batch.create(personalTrainerDocRef, personalTrainer);
        addressPerDocRef.forEach(pair -> {
            batch.create(pair.getRight(), pair.getLeft());
            var newAddress = pair.getLeft().withPersonalTrainer(personalTrainer);
            var newAddressDocRef = addressesCollRef.document(newAddress.getId());
            masterAddressPerDocRef.add(Pair.of(newAddress, newAddressDocRef));
        });
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        eventsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        subscriptionPlansPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        masterAddressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return personalTrainer;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @SneakyThrows
    public static List<PersonalTrainer> find(List<String> personalTrainerIds) {

        var personalTrainers = new ArrayList<PersonalTrainer>();

        db
                .collection(PersonalTrainer.PERSONAL_TRAINERS_COLLECTION)
                .whereIn(FieldPath.documentId(), personalTrainerIds)
                .get()
                .get()
                .forEach(queryDocSnapshot -> {

                    try {

                        var docRef = queryDocSnapshot.getReference();
                        var contactsColl = docRef.collection(Contact.CONTACTS_COLLECTION).get();
                        var addressColl = docRef.collection(Address.ADDRESSES_COLLECTION).get();
                        var eventsColl = docRef.collection(Event.EVENTS_COLLECTION).get();
                        var subscriptionPlansColl = docRef.collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION).get();

                        var personalTrainer = queryDocSnapshot.toObject(PersonalTrainer.class);
                        var contacts = contactsColl.get().toObjects(Contact.class);
                        var addresses = addressColl.get().toObjects(Address.class);
                        var events = eventsColl.get().toObjects(Event.class);
                        var subscriptionPlans = subscriptionPlansColl.get().toObjects(SubscriptionPlan.class);

                        personalTrainer.addContacts(contacts);
                        personalTrainer.addAddresses(addresses);
                        personalTrainer.addEvents(events);
                        personalTrainer.addSubscriptionPlan(subscriptionPlans);
                        personalTrainers.add(personalTrainer);

                    } catch (Exception e) { }
                });

        return personalTrainers;

    }

    @SneakyThrows
    public static void updateProps(PersonalTrainer personalTrainer) {

        var docRef = db.collection(PersonalTrainer.PERSONAL_TRAINERS_COLLECTION).document(personalTrainer.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put(PersonalTrainer.UPDATED_AT, new Date());

        if(StringUtils.isNotEmpty(personalTrainer.getBiography())) {
            propsToUpdate.put(PersonalTrainer.BIOGRAPHY, personalTrainer.getBiography());
        }

        if(StringUtils.isNotBlank(personalTrainer.getProfileName())) {
            propsToUpdate.put(PersonalTrainer.PROFILE_NAME, personalTrainer.getProfileName());
        }

        propsToUpdate.put(PersonalTrainer.GALLERY_PICTURES_URLS, personalTrainer.getGalleryPicturesUrls());
        propsToUpdate.put(PersonalTrainer.SPORTS, personalTrainer.getSports());
        propsToUpdate.put(PersonalTrainer.FOCUS, personalTrainer.getFocus());

        if(personalTrainer.getOnlineService() != null) {
            propsToUpdate.put(PersonalTrainer.ONLINE_SERVICE, personalTrainer.getOnlineService());
        }

        if(personalTrainer.getHomeService() != null) {
            propsToUpdate.put(PersonalTrainer.HOME_SERVICE, personalTrainer.getHomeService());
        }

        if(personalTrainer.getBusySchedule() != null) {
            propsToUpdate.put(PersonalTrainer.BUSY_SCHEDULE, personalTrainer.getBusySchedule());
        }

        docRef.update(propsToUpdate).get();

        var found = find(List.of(personalTrainer.getId()));

        var updated = found.get(0);

        var addresses = updated.getAddresses();

        if(CollectionUtils.isEmpty(addresses)) {
            return;
        }

        var batch = db.batch();

        var addressColl = db.collection(Address.ADDRESSES_COLLECTION);

        addresses.forEach(address -> {

            var id = address.getId();

            var addressDocRef = addressColl.document(id);

            batch.update(addressDocRef, Address.PERSONAL_TRAINER, updated);
        });

        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}
