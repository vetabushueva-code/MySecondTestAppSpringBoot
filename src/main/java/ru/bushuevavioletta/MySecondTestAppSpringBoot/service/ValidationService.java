package ru.bushuevavioletta.MySecondTestAppSpringBoot.service;

import org.springframework.validation.BindingResult;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.exception.ValidationFailedException;

public interface ValidationService {
    void isValid(BindingResult bindingResult) throws ValidationFailedException;
}