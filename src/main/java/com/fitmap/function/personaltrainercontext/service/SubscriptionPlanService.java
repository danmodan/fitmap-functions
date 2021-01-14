package com.fitmap.function.personaltrainercontext.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.fitmap.function.commonfirestore.service.SubCollectionsCrudService;
import com.fitmap.function.personaltrainercontext.domain.SubscriptionPlan;
import com.google.cloud.firestore.Firestore;

public class SubscriptionPlanService extends SubCollectionsCrudService<SubscriptionPlan> {

    public SubscriptionPlanService(Firestore db) {
        super(db);
    }

    @Override
    protected String getSuperCollection() {
        return "personal-trainers";
    }

    @Override
    protected String getSubCollection() {
        return "subscription-plans";
    }

    @Override
    protected Class<SubscriptionPlan> getSubEntityType() {
        return SubscriptionPlan.class;
    }

    @Override
    protected BiConsumer<SubscriptionPlan, String> updateId() {
        return SubscriptionPlan::setId;
    }

    @Override
    protected Function<SubscriptionPlan, String> getId() {
        return SubscriptionPlan::getId;
    }

    @Override
    protected Function<SubscriptionPlan, Map<String, Object>> getFieldsToUpdate() {

        return subscriptionPlan -> {
            var props = new HashMap<String, Object>();

            props.put("name", subscriptionPlan.getName());
            props.put("price", subscriptionPlan.getPrice());
            props.put("numberMonth", subscriptionPlan.getNumberMonth());
            props.put("description", subscriptionPlan.getDescription());

            return props;
        };
    }

}
