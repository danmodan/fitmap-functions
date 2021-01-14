package com.fitmap.function.gymcontext.v1;

import java.util.List;
import java.util.function.Function;

import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.commonfirestore.config.FirestoreConfig;
import com.fitmap.function.commonfirestore.function.SubCollectionsCrudFunction;
import com.fitmap.function.gymcontext.domain.SubscriptionPlan;
import com.fitmap.function.gymcontext.service.SubscriptionPlanService;
import com.fitmap.function.gymcontext.v1.payload.request.CreateRequestDtos;
import com.fitmap.function.gymcontext.v1.payload.request.EditRequestDtos;

import lombok.extern.java.Log;

@Log
public class CrudSubscriptionPlanFunction extends SubCollectionsCrudFunction<SubscriptionPlan, EditRequestDtos.SubscriptionPlan, CreateRequestDtos.SubscriptionPlan> {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    public CrudSubscriptionPlanFunction() {
        super(new SubscriptionPlanService(FirestoreConfig.FIRESTORE));

        log.info("awake function");
    }

    @Override
    protected Class<EditRequestDtos.SubscriptionPlan[]> getEditRequestDtoClass() {
        return EditRequestDtos.SubscriptionPlan[].class;
    }

    @Override
    protected Class<CreateRequestDtos.SubscriptionPlan[]> getCreateRequestDtoClass() {
        return CreateRequestDtos.SubscriptionPlan[].class;
    }

    @Override
    protected Function<List<CreateRequestDtos.SubscriptionPlan>, List<SubscriptionPlan>> mapCreationDtosToSubEntities() {
        return dtos -> SubscriptionPlan.from(dtos, SubscriptionPlan::from);
    }

    @Override
    protected Function<List<EditRequestDtos.SubscriptionPlan>, List<SubscriptionPlan>> mapEditDtosToSubEntities() {
        return dtos -> SubscriptionPlan.from(dtos, SubscriptionPlan::from);
    }

}
