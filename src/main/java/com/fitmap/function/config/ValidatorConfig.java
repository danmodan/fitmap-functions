package com.fitmap.function.config;

import javax.validation.Validation;
import javax.validation.Validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorConfig {

    public static final Validator VALIDATOR;

    static {

        VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    }

}
