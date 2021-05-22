package com.fitmap.function.v2;

import java.util.stream.Collectors;

import com.fitmap.function.domain.Address;
import com.fitmap.function.domain.Gym;
import com.fitmap.function.domain.PersonalTrainer;
import com.fitmap.function.domain.Student;
import com.fitmap.function.domain.UserType;
import com.fitmap.function.exception.TerminalException;
import com.fitmap.function.service.AddressService;
import com.fitmap.function.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.service.CheckRequestMethodService;
import com.fitmap.function.service.GymService;
import com.fitmap.function.service.PersonalTrainerService;
import com.fitmap.function.service.ResponseService;
import com.fitmap.function.service.StudentService;
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

        var addressesPerEntityType = found
            .stream()
            .filter(a -> {

                if(a == null) {
                    return false;
                }

                if(a.getGym() == null && a.getPersonalTrainer() == null && a.getStudent() == null) {
                    return false;
                }

                return true;
            })
            .collect(Collectors.groupingBy(a -> {
                if(a.getGym() != null) {
                    return UserType.GYM;
                }

                if(a.getPersonalTrainer() != null) {
                    return UserType.PERSONAL_TRAINER;
                }

                if(a.getStudent() != null) {
                    return UserType.STUDENT;
                }

                throw new RuntimeException("address must contain at least a gym, personal trainer or student. addressId = " + a.getId());
            }));

        var gymsInAddress = addressesPerEntityType.get(UserType.GYM);

        if(gymsInAddress != null) {

            var addressesPerGym = gymsInAddress
                .stream()
                .filter(a -> a.getGym() != null)
                .collect(Collectors.groupingBy(Address::getGym));

            var gymIds = addressesPerGym
                .keySet()
                .stream()
                .map(Gym::getId)
                .collect(Collectors.toList());

            var foundGyms = GymService.find(gymIds);

            foundGyms.forEach(gym -> addressesPerGym.get(gym).forEach(a -> a.setGym(gym)));
        }

        var personalTrainersInAddress = addressesPerEntityType.get(UserType.PERSONAL_TRAINER);

        if(personalTrainersInAddress != null) {

            var addressesPerPersonalTrainer = personalTrainersInAddress
                .stream()
                .filter(a -> a.getPersonalTrainer() != null)
                .collect(Collectors.groupingBy(Address::getPersonalTrainer));

            var personalTrainerIds = addressesPerPersonalTrainer
                .keySet()
                .stream()
                .map(PersonalTrainer::getId)
                .collect(Collectors.toList());

            var foundPersonalTrainers = PersonalTrainerService.find(personalTrainerIds);

            foundPersonalTrainers.forEach(personalTrainer -> addressesPerPersonalTrainer.get(personalTrainer).forEach(a -> a.setPersonalTrainer(personalTrainer)));
        }

        var studentsInAddress = addressesPerEntityType.get(UserType.STUDENT);

        if(studentsInAddress != null) {

            var addressesPerStudent = studentsInAddress
                .stream()
                .filter(a -> a.getStudent() != null)
                .collect(Collectors.groupingBy(Address::getStudent));

            var studentIds = addressesPerStudent
                .keySet()
                .stream()
                .map(Student::getId)
                .collect(Collectors.toList());

            var foundStudents = StudentService.find(studentIds);

            foundStudents.forEach(student -> addressesPerStudent.get(student).forEach(a -> a.setStudent(student)));
        }

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

}
