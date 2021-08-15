package com.fitmap.function.v2;

import java.time.ZonedDateTime;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.SystemTimeZoneConfig;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.service.ResponseService;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.extern.java.Log;

@Log
public class FitMapFunction implements HttpFunction {

    public FitMapFunction() {

        log.log(Level.INFO, "init FitMapFunction /api/v2. timestamp=" + ZonedDateTime.now());
        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v2/set-roles":
                    SetRolesFunction.doService(request, response);
                    break;
                case "/api/v2/gym":
                    GymFunction.doService(request, response);
                    break;
                case "/api/v2/gym/events":
                    EventFunction.doService(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v2/gym/subscription-plans":
                    SubscriptionPlanFunction.doService(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v2/personal-trainer":
                    PersonalTrainerFunction.doService(request, response);
                    break;
                case "/api/v2/personal-trainer/events":
                    EventFunction.doService(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
                    break;
                case "/api/v2/personal-trainer/subscription-plans":
                    SubscriptionPlanFunction.doService(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
                    break;
                case "/api/v2/student":
                    StudentFunction.doService(request, response);
                    break;
                case "/api/v2/sport":
                    SportFunction.doService(request, response);
                    break;
                case "/api/v2/fight":
                    FightFunction.doService(request, response);
                    break;
                case "/api/v2/focus":
                    FocusFunction.doService(request, response);
                    break;
                case "/api/v2/locations":
                    LocationsFunction.doService(request, response);
                    break;
                default:
                    throw new TerminalException("No mapping found for HTTP request path [" + path + "]", HttpStatus.NOT_FOUND);
            }

        } catch (TerminalException e) { ResponseService.answerTerminalException(request, response, e); }
          catch (MethodNotAllowedException e) { ResponseService.answerMethodNotAllowed(request, response, e); }
          catch (UnsupportedMediaTypeStatusException e) { ResponseService.answerUnsupportedMediaType(request, response, e); }
          catch (HttpMessageNotReadableException e) { ResponseService.answerBadRequest(request, response, e); }
          catch (ConstraintViolationException e) { ResponseService.answerBadRequest(request, response, e); }
          catch (Exception e) { log.log(Level.SEVERE, e.getMessage(), e); ResponseService.answerInternalServerError(request, response, e); }

    }

}
