package com.fitmap.function.service;

import java.util.Collection;
import java.util.function.Predicate;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.config.ValidatorConfig;
import com.fitmap.function.exception.TerminalException;

import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckConstraintsRequestBodyService {

    public static <T> void checkConstraints(T body) {

        var violations = ValidatorConfig.VALIDATOR.validate(body);

        if (CollectionUtils.isEmpty(violations)) {

            return;
        }

        throw new ConstraintViolationException("There are fields errors.", violations);
    }

    public static <T> void checkNotEmpty(T[] array) {

        if (array == null || array.length == 0) {

            throw new TerminalException("Request list cannot be empty.", HttpStatus.BAD_REQUEST);
        }

    }

    public static <T> void checkOnlyOneMainElement(Collection<T> coll, Predicate<T> isMainElement) {

        if (CollectionUtils.isEmpty(coll)) {
            return;
        }

        var amount = 0;

        for (var e : coll) {
            if (isMainElement.test(e)) {
                amount++;
            }
        }

        if (amount > 1) {
            throw new TerminalException("Cannot exist more than one main element.", HttpStatus.BAD_REQUEST);
        }
    }

}
