package com.fitmap.function.common.service;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import com.fitmap.function.common.config.ValidatorConfig;
import com.fitmap.function.common.exception.TerminalException;

import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckConstraintsRequestBodyService {

    private static final Validator VALIDATOR = ValidatorConfig.VALIDATOR;

    public static <T> void checkConstraints(T body) {

        var violations = VALIDATOR.validate(body);

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
}
