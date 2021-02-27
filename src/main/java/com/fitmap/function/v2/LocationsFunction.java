package com.fitmap.function.v2;

import java.util.Objects;

import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestMethodService;
import com.fitmap.function.service.ResponseService;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationsFunction {

    public static void service(HttpRequest request, HttpResponse response) {

        CheckRequestMethodService.checkGetMethod(request);

        var latOpt = request.getFirstQueryParameter("lat");
        var lngOpt = request.getFirstQueryParameter("lng");
        var radiusOpt = request.getFirstQueryParameter("radius");

        if(latOpt.isEmpty() || lngOpt.isEmpty() || radiusOpt.isEmpty()) {

            throw new TerminalException("The params [lat, lng, radius] are mandatories.", HttpStatus.BAD_REQUEST);
        }

        var latitudeStr = latOpt.get();
        var longitudeStr = lngOpt.get();
        var radiusStr = radiusOpt.get();

        CheckConstraintsRequestBodyService.checkIsDoubleParsables(latitudeStr, longitudeStr, radiusStr);

        var latitude = Double.parseDouble(latOpt.get());
        var longitude = Double.parseDouble(lngOpt.get());
        var radius = Double.parseDouble(radiusOpt.get());

        var found = AddressService.findAddressesNearBy(latitude, longitude, radius);

        found
            .stream()
            .filter(Objects::nonNull)
            .forEach(a -> {

                if(a.getGym() != null) {
                    a.getGym().setAddresses(null);
                    a.getGym().setContacts(null);
                    a.getGym().setEvents(null);
                    a.getGym().setSubscriptionPlans(null);
                    return;
                }

                if(a.getPersonalTrainer() != null) {
                    a.getPersonalTrainer().setAddresses(null);
                    a.getPersonalTrainer().setContacts(null);
                    a.getPersonalTrainer().setEvents(null);
                    a.getPersonalTrainer().setSubscriptionPlans(null);
                    return;
                }

                if(a.getStudent() != null) {
                    a.getStudent().setAddresses(null);
                    a.getStudent().setContacts(null);
                    return;
                }
        });

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

}
