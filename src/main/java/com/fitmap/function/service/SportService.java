package com.fitmap.function.service;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.Sport;
import com.fitmap.function.exception.TerminalException;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SportService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    public static List<Sport> create(List<Sport> sports) {

        var batch = db.batch();

        var collRef = db.collection(Sport.SPORTS_COLLECTION);

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

    @SneakyThrows
    public static List<Sport> find(List<String> ids) {

        return db.collection(Sport.SPORTS_COLLECTION).whereIn(FieldPath.documentId(), ids).get().get().toObjects(Sport.class);

    }

    @SneakyThrows
    public static List<Sport> findAll() {

        return db.collection(Sport.SPORTS_COLLECTION).get().get().toObjects(Sport.class);

    }

    public static List<Sport> update(List<Sport> sports) {

        var batch = db.batch();

        var collRef = db.collection(Sport.SPORTS_COLLECTION);

        sports.forEach(sport ->  batch
            .update(
                collRef.document(sport.getId()),
                Sport.NAME, sport.getName(),
                Sport.TYPE, sport.getType(),
                Sport.LANGUAGES, sport.getLanguages()
            )
        );

        CheckConstraintsRequestBodyService.checkConstraints(sports);

        try {

            batch.commit().get();

            return sports;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

    public static void remove(List<String> ids) {

        var batch = db.batch();

        var collRef = db.collection(Sport.SPORTS_COLLECTION);

        ids.forEach(id -> batch.delete(collRef.document(id)));

        try {

            batch.commit().get();

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }

    }

}
