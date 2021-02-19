package com.fitmap.function.service;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Contact;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

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

        return contacts;

	}

    public static List<Contact> edit(String superEntityId, String superCollection, List<Contact> contacts) {

        var batch = db.batch();

        var contactsCollection = db.collection(superCollection).document(superEntityId).collection(Contact.CONTACTS_COLLECTION);

        contacts.forEach(contact -> {

            CheckConstraintsRequestBodyService.checkConstraints(contact);

            var docRef = contactsCollection.document(contact.getId());

            var fields = contact.createPropertiesMap();

            batch.update(docRef, fields);
        });

        commit(batch);

        return contacts;

    }

    public static void delete(String superEntityId, String superCollection, List<String> contactsIds) {

        var batch = db.batch();

        var contactsCollection = db.collection(superCollection).document(superEntityId).collection(Contact.CONTACTS_COLLECTION);

        contactsIds.forEach(id -> {

            var docRef = contactsCollection.document(id);

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
