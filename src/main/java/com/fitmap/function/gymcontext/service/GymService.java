package com.fitmap.function.gymcontext.service;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.gymcontext.domain.Address;
import com.fitmap.function.gymcontext.domain.Contact;
import com.fitmap.function.gymcontext.domain.Gym;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class GymService {

    private static final String GYMS_COLLECTION = "gyms";
    private static final String CONTACTS_COLLECTION = "contacts";
    private static final String ADDRESS_COLLECTION = "addresses";

    private final Firestore db;

    public Gym create(Gym gym) {

        var now = new Date();
        gym.setCreatedAt(now);
        gym.setUpdatedAt(now);

        var batch = db.batch();

        var gymDocRef = db.collection(GYMS_COLLECTION).document(gym.getId());

        var addressPerDocRef = gym.getAddresses().stream().map(address -> {
            var ref = gymDocRef.collection(ADDRESS_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = gym.getContacts().stream().map(contact -> {
            var ref = gymDocRef.collection(CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(gym);

        batch.create(gymDocRef, gym);
        addressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return gym;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public Gym find(String gymId) throws InterruptedException, ExecutionException {

        var docRef = db.collection(GYMS_COLLECTION).document(gymId);

        var gymDoc = docRef.get().get();

        if (gymDoc.exists()) {

            var gym = gymDoc.toObject(Gym.class);

            var contactsColl = docRef.collection(CONTACTS_COLLECTION).get().get();

            var contacts = contactsColl.getDocuments().stream().map(doc -> doc.toObject(Contact.class)).collect(Collectors.toList());

            gym.addContacts(contacts);

            var addressColl = docRef.collection(ADDRESS_COLLECTION).get().get();

            var address = addressColl.getDocuments().stream().map(doc -> doc.toObject(Address.class)).collect(Collectors.toList());

            gym.addAddresses(address);

            return gym;
        }

        throw new TerminalException(String.format("Gym, %s, not found.", gymId), HttpStatus.NOT_FOUND);
    }

}