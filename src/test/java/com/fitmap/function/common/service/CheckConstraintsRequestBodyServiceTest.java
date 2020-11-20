package com.fitmap.function.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

class CheckConstraintsRequestBodyServiceTest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class POJOClassTest {

        @NotNull
        private String prop;
    }

    @Test
    @DisplayName(value = "when there are constraint violation a ConstraintViolationException should be thrown")
    void when_there_are_constraint_violation_a_ConstraintViolationException_should_be_thrown() {

        var request = new POJOClassTest();

        assertThatThrownBy(() -> CheckConstraintsRequestBodyService.checkConstraints(request))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessage("There are fields errors.");
    }

    @Test
    @DisplayName(value = "when there are not constraint violation, nothing must happen")
    void when_there_are_constraint_violation_nothing_must_happen() {

        var request = new POJOClassTest("qwert");

        CheckConstraintsRequestBodyService.checkConstraints(request);

        assertThat(Boolean.TRUE).isTrue();
    }
}
