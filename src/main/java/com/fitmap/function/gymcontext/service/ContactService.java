package com.fitmap.function.gymcontext.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.gymcontext.domain.Contact;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ContactService {

    private static final String GYMS_COLLECTION = "gyms";
    private static final String CONTACTS_COLLECTION = "contacts";

    private final Firestore db;

    public List<Contact> find(String gymId) throws InterruptedException, ExecutionException {

        var docRef = db.collection(GYMS_COLLECTION).document(gymId).collection(CONTACTS_COLLECTION);

        var contactsColl = docRef.get().get();

        return contactsColl.getDocuments().stream().map(doc -> doc.toObject(Contact.class)).collect(Collectors.toList());
    }

	public List<Contact> create(String gymId, List<Contact> contacts) {

        var batch = db.batch();

        var contactsCollRef = db.collection(GYMS_COLLECTION).document(gymId).collection(CONTACTS_COLLECTION);

        var contactsPerDocRef = contacts.stream().map(contact -> {
            var ref = contactsCollRef.document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(contacts);

        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        commit(batch);

        return contacts;

	}

    public List<Contact> edit(String gymId, List<Contact> contacts) {

        var batch = db.batch();

        contacts.forEach(contact -> {

            CheckConstraintsRequestBodyService.checkConstraints(contact);

            var docRef = db.collection(GYMS_COLLECTION).document(gymId).collection(CONTACTS_COLLECTION).document(contact.getId());

            Map<String, Object> fields = Map.of(
                "name", contact.getName(),
                "email", contact.getEmail(),
                "phone", contact.getPhone(),
                "whatsapp", contact.getWhatsapp()
            );

            batch.update(docRef, fields);
        });

        commit(batch);

        return contacts;

    }

    public void delete(String gymId, List<String> contactsIds) {

        var batch = db.batch();

        contactsIds.forEach(id -> {

            var docRef = db.collection(GYMS_COLLECTION).document(gymId).collection(CONTACTS_COLLECTION).document(id);

            batch.delete(docRef);
        });

        commit(batch);
    }

    private void commit(WriteBatch batch) {
        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

}