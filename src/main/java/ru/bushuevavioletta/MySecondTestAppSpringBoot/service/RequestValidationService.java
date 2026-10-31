package ru.bushuevavioletta.MySecondTestAppSpringBoot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.exception.ValidationFailedException;

@Slf4j
@Service
public class RequestValidationService implements ValidationService {

    @Override
    public void isValid(BindingResult bindingResult) throws ValidationFailedException {
        if (bindingResult.hasErrors()) {
            // Выводим в лог каждую ошибку валидации из bindingResult
            bindingResult.getFieldErrors().forEach(error ->
                    log.error("Ошибка валидации: поле '{}' - {}, переданное значение: {}",
                            error.getField(), error.getDefaultMessage(), error.getRejectedValue()));
            throw new ValidationFailedException(bindingResult.getFieldError().toString());
        }
    }
}