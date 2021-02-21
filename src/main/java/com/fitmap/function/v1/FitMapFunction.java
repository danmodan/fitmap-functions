package com.fitmap.function.v1;

import java.time.ZonedDateTime;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.SystemTimeZoneConfig;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.domain.Student;
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

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    public FitMapFunction() {

        log.log(Level.INFO, "init FitMapFunction /api/v1. timestamp=" + ZonedDateTime.now());
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            final var path = request.getPath();

            switch (path) {
                case "/api/v1/set-roles":
                    SetRolesFunction.service(request, response);
                    break;
                case "/api/v1/gym":
                    GymFunction.service(request, response);
                    break;
                case "/api/v1/gym/contacts":
                    ContactFunction.service(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v1/gym/addresses":
                    AddressFunction.service(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v1/gym/events":
                    EventFunction.service(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v1/gym/subscription-plans":
                    SubscriptionPlanFunction.service(request, response, Gym.GYMS_COLLECTION);
                    break;
                case "/api/v1/personal-trainer":
                    PersonalTrainerFunction.service(request, response);
                    break;
                case "/api/v1/personal-trainer/contacts":
                    ContactFunction.service(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
                    break;
                case "/api/v1/personal-trainer/addresses":
                    AddressFunction.service(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
                    break;
                case "/api/v1/personal-trainer/events":
                    EventFunction.service(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
                    break;
                case "/api/v1/personal-trainer/subscription-plans":
                    SubscriptionPlanFunction.service(request, response, PersonalTrainer.PERSONAL_TRAINERS_COLLECTION);
                    break;
                case "/api/v1/student":
                    StudentFunction.service(request, response);
                    break;
                case "/api/v1/student/contacts":
                    ContactFunction.service(request, response, Student.STUDENTS_COLLECTION);
                    break;
                case "/api/v1/student/addresses":
                    AddressFunction.service(request, response, Student.STUDENTS_COLLECTION);
                    break;
                case "/api/v1/sport":
                    SportFunction.service(request, response);
                    break;
                case "/api/v1/locations":
                    LocationsFunction.service(request, response);
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
