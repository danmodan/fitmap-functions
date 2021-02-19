package com.fitmap.function.service;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.config.FirestoreConfig;
import com.fitmap.function.domain.SubscriptionPlan;
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
public class SubscriptionPlanService {

    private static final Firestore db = FirestoreConfig.FIRESTORE;

    @SneakyThrows
    public static List<SubscriptionPlan> find(String superEntityId, String superCollection) {

        var docRef = db.collection(superCollection).document(superEntityId).collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION);

        return docRef.get().get().toObjects(SubscriptionPlan.class);
    }

	public static List<SubscriptionPlan> create(String superEntityId, String superCollection, List<SubscriptionPlan> subscriptionPlans) {

        var batch = db.batch();

        var collRef = db.collection(superCollection).document(superEntityId).collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION);

        var subEntitiesPerDocRef = subscriptionPlans.stream().map(subscriptionPlan -> {
            var ref = collRef.document();
            subscriptionPlan.setId(ref.getId());
            return Pair.of(subscriptionPlan, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(subscriptionPlans);

        subEntitiesPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        commit(batch);

        return subscriptionPlans;

	}

    public static List<SubscriptionPlan> edit(String superEntityId, String superCollection, List<SubscriptionPlan> subscriptionPlans) {

        var batch = db.batch();

        var subscriptionPlansCollection = db.collection(superCollection).document(superEntityId).collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION);

        subscriptionPlans.forEach(subscriptionPlan -> {

            CheckConstraintsRequestBodyService.checkConstraints(subscriptionPlan);

            var docRef = subscriptionPlansCollection.document(subscriptionPlan.getId());

            var fields = subscriptionPlan.createPropertiesMap();

            batch.update(docRef, fields);
        });

        commit(batch);

        return subscriptionPlans;

    }

    public static void delete(String superEntityId, String superCollection, List<String> subscriptionPlansIds) {

        var batch = db.batch();

        var subscriptionPlansCollection = db.collection(superCollection).document(superEntityId).collection(SubscriptionPlan.SUBSCRIPTION_PLANS_COLLECTION);

        subscriptionPlansIds.forEach(id -> {

            var docRef = subscriptionPlansCollection.document(id);

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
