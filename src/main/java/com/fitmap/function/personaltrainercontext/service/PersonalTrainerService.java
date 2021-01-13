package com.fitmap.function.personaltrainercontext.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.personaltrainercontext.domain.Address;
import com.fitmap.function.personaltrainercontext.domain.Contact;
import com.fitmap.function.personaltrainercontext.domain.PersonalTrainer;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class PersonalTrainerService {

    private static final String PERSONAL_TRAINERS_COLLECTION = "personal-trainers";
    private static final String CONTACTS_COLLECTION = "contacts";
    private static final String ADDRESS_COLLECTION = "addresses";

    private final Firestore db;

    public PersonalTrainer create(PersonalTrainer personalTrainer) {

        var now = new Date();
        personalTrainer.setCreatedAt(now);
        personalTrainer.setUpdatedAt(now);

        var batch = db.batch();

        var personalTrainerDocRef = db.collection(PERSONAL_TRAINERS_COLLECTION).document(personalTrainer.getId());

        var addressPerDocRef = personalTrainer.getAddresses().stream().map(address -> {
            var ref = personalTrainerDocRef.collection(ADDRESS_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = personalTrainer.getContacts().stream().map(contact -> {
            var ref = personalTrainerDocRef.collection(CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(personalTrainer);

        batch.create(personalTrainerDocRef, personalTrainer);
        addressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return personalTrainer;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public List<PersonalTrainer> find(List<String> personalTrainerIds) throws InterruptedException, ExecutionException {

        var personalTrainers = new ArrayList<PersonalTrainer>();

        db
            .collection(PERSONAL_TRAINERS_COLLECTION)
            .whereIn(FieldPath.documentId(), personalTrainerIds)
            .get()
            .get()
            .forEach(queryDocSnapshot -> {

                try {

                    var docRef = queryDocSnapshot.getReference();
                    var contactsColl = docRef.collection(CONTACTS_COLLECTION).get();
                    var addressColl = docRef.collection(ADDRESS_COLLECTION).get();

                    var personalTrainer = queryDocSnapshot.toObject(PersonalTrainer.class);
                    var contacts = contactsColl.get().toObjects(Contact.class);
                    var addresses = addressColl.get().toObjects(Address.class);

                    personalTrainer.addContacts(contacts);
                    personalTrainer.addAddresses(addresses);
                    personalTrainers.add(personalTrainer);

                } catch (Exception e) { }
            });

        return personalTrainers;

    }

    public void updateProps(PersonalTrainer personalTrainer) throws InterruptedException, ExecutionException {

        var docRef = db.collection(PERSONAL_TRAINERS_COLLECTION).document(personalTrainer.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put("updatedAt", new Date());

        if(StringUtils.isNotEmpty(personalTrainer.getBiography())) {
            propsToUpdate.put("biography", personalTrainer.getBiography());
        }

        propsToUpdate.put("galleryPicturesUrls", FieldValue.arrayUnion(personalTrainer.getGalleryPicturesUrls().toArray(new Object[personalTrainer.getGalleryPicturesUrls().size()])));
        propsToUpdate.put("sports", FieldValue.arrayUnion(personalTrainer.getSports().toArray(new Object[personalTrainer.getSports().size()])));

        docRef.update(propsToUpdate).get();
    }

    public void removeElementsFromArraysProps(PersonalTrainer personalTrainer) throws InterruptedException, ExecutionException {

        var docRef = db.collection(PERSONAL_TRAINERS_COLLECTION).document(personalTrainer.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put("updatedAt", new Date());

        propsToUpdate.put("galleryPicturesUrls", FieldValue.arrayRemove(personalTrainer.getGalleryPicturesUrls().toArray(new Object[personalTrainer.getGalleryPicturesUrls().size()])));
        propsToUpdate.put("sports", FieldValue.arrayRemove(personalTrainer.getSports().toArray(new Object[personalTrainer.getSports().size()])));

        docRef.update(propsToUpdate).get();
    }

}
