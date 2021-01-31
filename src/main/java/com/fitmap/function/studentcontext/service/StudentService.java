package com.fitmap.function.studentcontext.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.studentcontext.domain.Address;
import com.fitmap.function.studentcontext.domain.Contact;
import com.fitmap.function.studentcontext.domain.Student;
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
public class StudentService {

    private static final String STUDENTS_COLLECTION = "students";
    private static final String CONTACTS_COLLECTION = "contacts";
    private static final String ADDRESS_COLLECTION = "addresses";

    private final Firestore db;

    public Student create(Student student) {

        var now = new Date();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);

        var batch = db.batch();

        var studentDocRef = db.collection(STUDENTS_COLLECTION).document(student.getId());

        var addressPerDocRef = student.getAddresses().stream().map(address -> {
            var ref = studentDocRef.collection(ADDRESS_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = student.getContacts().stream().map(contact -> {
            var ref = studentDocRef.collection(CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(student);

        batch.create(studentDocRef, student);
        addressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return student;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public List<Student> find(List<String> studentIds) throws InterruptedException, ExecutionException {

        var students = new ArrayList<Student>();

        db
            .collection(STUDENTS_COLLECTION)
            .whereIn(FieldPath.documentId(), studentIds)
            .get()
            .get()
            .forEach(queryDocSnapshot -> {

                try {

                    var docRef = queryDocSnapshot.getReference();
                    var contactsColl = docRef.collection(CONTACTS_COLLECTION).get();
                    var addressColl = docRef.collection(ADDRESS_COLLECTION).get();

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

    public void updateProps(Student student) throws InterruptedException, ExecutionException {

        var docRef = db.collection(STUDENTS_COLLECTION).document(student.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put("updatedAt", new Date());

        if(StringUtils.isNotBlank(student.getProfileName())) {
            propsToUpdate.put("profileName", student.getProfileName());
        }

        if(student.getGalleryPicturesUrls().size() > 0) {
            propsToUpdate.put("galleryPicturesUrls", FieldValue.arrayUnion(student.getGalleryPicturesUrls().toArray(new Object[student.getGalleryPicturesUrls().size()])));
        }

        docRef.update(propsToUpdate).get();
    }

    public void removeElementsFromArraysProps(Student student) throws InterruptedException, ExecutionException {

        var docRef = db.collection(STUDENTS_COLLECTION).document(student.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put("updatedAt", new Date());

        if(student.getGalleryPicturesUrls().size() > 0) {
            propsToUpdate.put("galleryPicturesUrls", FieldValue.arrayRemove(student.getGalleryPicturesUrls().toArray(new Object[student.getGalleryPicturesUrls().size()])));
        }

        docRef.update(propsToUpdate).get();
    }

}
