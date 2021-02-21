package com.fitmap.function.v2;

import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestContentTypeService;
import com.fitmap.function.service.CheckRequestMethodService;
import com.fitmap.function.service.ReadRequestService;
import com.fitmap.function.service.SetRolesService;
import com.fitmap.function.v2.payload.request.SetRolesRequest;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SetRolesFunction {

    public static void service(HttpRequest request, HttpResponse response) {

        CheckRequestMethodService.checkPostMethod(request);

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, SetRolesRequest.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        SetRolesService.setRoles(dto.getIdToken(), dto.getUserType().name());

    }

}
