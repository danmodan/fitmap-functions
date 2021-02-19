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
import com.fitmap.function.domain.Student;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StudentService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    public static Student create(Student student) {

        var now = new Date();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);

        var batch = db.batch();

        var studentDocRef = db.collection(Student.STUDENTS_COLLECTION).document(student.getId());

        var addressPerDocRef = student.getAddresses().stream().map(address -> {
            var ref = studentDocRef.collection(Address.ADDRESSES_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = student.getContacts().stream().map(contact -> {
            var ref = studentDocRef.collection(Contact.CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(student);

        var addressesCollRef = db.collection(Address.ADDRESSES_COLLECTION);
        var masterAddressPerDocRef = new ArrayList<Pair<Address, DocumentReference>>();

        batch.create(studentDocRef, student);
        addressPerDocRef.forEach(pair -> {
            batch.create(pair.getRight(), pair.getLeft());
            var newAddress = pair.getLeft().withStudentId(student.getId());
            var newAddressDocRef = addressesCollRef.document(newAddress.getId());
            masterAddressPerDocRef.add(Pair.of(newAddress, newAddressDocRef));
        });
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        masterAddressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return student;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    @SneakyThrows
    public static List<Student> find(List<String> studentIds) {

        var students = new ArrayList<Student>();

        db
            .collection(Student.STUDENTS_COLLECTION)
            .whereIn(FieldPath.documentId(), studentIds)
            .get()
            .get()
            .forEach(queryDocSnapshot -> {

                try {

                    var docRef = queryDocSnapshot.getReference();
                    var contactsColl = docRef.collection(Contact.CONTACTS_COLLECTION).get();
                    var addressColl = docRef.collection(Address.ADDRESSES_COLLECTION).get();

                    var student = queryDocSnapshot.toObject(Student.class);
                    var contacts = contactsColl.get().toObjects(Contact.class);
                    var addresses = addressColl.get().toObjects(Address.class);

                    student.addContacts(contacts);
                    student.addAddresses(addresses);
                    students.add(student);

                } catch (Exception e) { }
            });

        return students;

    }

    @SneakyThrows
    public static void updateProps(Student student) {

        var docRef = db.collection(Student.STUDENTS_COLLECTION).document(student.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put(Student.UPDATED_AT, new Date());

        if(StringUtils.isNotBlank(student.getProfileName())) {
            propsToUpdate.put(Student.PROFILE_NAME, student.getProfileName());
        }

        if(student.getGalleryPicturesUrls().size() > 0) {
            propsToUpdate.put(Student.GALLERY_PICTURES_URLS, FieldValue.arrayUnion(student.getGalleryPicturesUrls().toArray(Object[]::new)));
        }

        docRef.update(propsToUpdate).get();
    }

    @SneakyThrows
    public static void removeElementsFromArraysProps(Student student) {

        var docRef = db.collection(Student.STUDENTS_COLLECTION).document(student.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put(Student.UPDATED_AT, new Date());

        if(student.getGalleryPicturesUrls().size() > 0) {
            propsToUpdate.put(Student.GALLERY_PICTURES_URLS, FieldValue.arrayRemove(student.getGalleryPicturesUrls().toArray(Object[]::new)));
        }

        docRef.update(propsToUpdate).get();
    }

}
