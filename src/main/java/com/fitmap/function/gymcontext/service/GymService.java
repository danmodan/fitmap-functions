package com.fitmap.function.gymcontext.service;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.gymcontext.domain.Gym;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GymService {

    private static final String GYMS_COLLECTION = "gyms";
    private static final String CONTACTS_COLLECTION = "contacts";
    private static final String ADDRESS_COLLECTION = "addresses";

    private final Firestore db;

    public Gym create(final Gym gym) {

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

        batch.create(gymDocRef, gym);
        addressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        CheckConstraintsRequestBodyService.checkConstraints(gym);

        try {

            batch.commit().get();

            return gym;

        } catch (Exception e) {

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public Gym find(String gymId) throws InterruptedException, ExecutionException {

        var gym = db.collection(GYMS_COLLECTION).document(gymId).get().get().toObject(Gym.class);

        if (gym != null) {

            return gym;
        }

        throw new TerminalException(String.format("Gym, %s, not found.", gymId), HttpStatus.NOT_FOUND);
    }

}