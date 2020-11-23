package com.fitmap.function.sportcontext.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.sportcontext.domain.Sport;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class SportService {

    private static final String SPORTS_COLLECTION = "sports";

    private final Firestore db;

    public List<Sport> create(List<Sport> sports) {

        var batch = db.batch();

        var collRef = db.collection(SPORTS_COLLECTION);

        var sportsPerDocRef = sports.stream().map(sport -> {
            var ref = collRef.document();
            sport.setId(ref.getId());
            return Pair.of(sport, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(sports);

        sportsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return sports;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public List<Sport> find(List<String> ids) throws InterruptedException, ExecutionException {

        return db.collection(SPORTS_COLLECTION).whereIn(FieldPath.documentId(), ids).get().get().toObjects(Sport.class);

    }

    public List<Sport> findAll() throws InterruptedException, ExecutionException {

        return db.collection(SPORTS_COLLECTION).get().get().toObjects(Sport.class);

    }

    public List<Sport> update(List<Sport> sports) throws InterruptedException, ExecutionException {

        var batch = db.batch();

        var collRef = db.collection(SPORTS_COLLECTION);

        sports.forEach(sport ->  batch.update(collRef.document(sport.getId()), "name", sport.getName()));

        CheckConstraintsRequestBodyService.checkConstraints(sports);

        try {

            batch.commit().get();

            return sports;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    public void remove(List<String> ids) throws InterruptedException, ExecutionException {

        var batch = db.batch();

        var collRef = db.collection(SPORTS_COLLECTION);

        ids.forEach(id -> batch.delete(collRef.document(id)));

        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }


    }

}
