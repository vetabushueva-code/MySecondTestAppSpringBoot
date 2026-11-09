package ru.bushuevavioletta.MySecondTestAppSpringBoot.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.exception.UnsupportedCodeException;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.exception.ValidationFailedException;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.Codes;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.ErrorCodes;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.ErrorMessages;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.Request;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.Response;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.service.ModifyRequestService;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.service.ModifyResponseService;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.service.ValidationService;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.util.DateTimeUtil;

import java.util.Date;

@Slf4j
@RestController
public class MyController {

    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;
    private final ModifyRequestService modifySourceRequestService;
    private final ModifyRequestService modifySystemNameRequestService;

    @Autowired
    public MyController(ValidationService validationService,
                        @Qualifier("ModifySystemTimeResponseService")
                        ModifyResponseService modifyResponseService,
                        @Qualifier("ModifySourceRequestService")
                        ModifyRequestService modifySourceRequestService,
                        @Qualifier("ModifySystemNameRequestService")
                        ModifyRequestService modifySystemNameRequestService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifySourceRequestService = modifySourceRequestService;
        this.modifySystemNameRequestService = modifySystemNameRequestService;
    }

    @PostMapping(value = "/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
                                             BindingResult bindingResult) {

        // Время получения request Сервисом 1 – нужно для замера времени до Сервиса 2
        Date receivedTime = new Date();

        log.info("request: {}", request);

        Response response = Response.builder()
                .uid(request.getUid())
                .operationUid(request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();

        log.info("response создан: {}", response);

        try {
            validationService.isValid(bindingResult);
            log.info("Валидация запроса uid={} прошла успешно", request.getUid());

            if ("123".equals(request.getUid())) {
                throw new UnsupportedCodeException("uid = 123 не поддерживается");
            }

            // Модифицируем request и пересылаем его в Сервис 2
            request.setSystemTime(DateTimeUtil.getCustomFormat().format(receivedTime));
            log.info("systemTime в request заменён на время получения Сервисом 1: {}",
                    request.getSystemTime());
            modifySourceRequestService.modify(request);
            modifySystemNameRequestService.modify(request);

        } catch (ValidationFailedException e) {
            log.error("ValidationFailedException: {}", e.getMessage());
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.VALIDATION_EXCEPTION);
            response.setErrorMessage(ErrorMessages.VALIDATION);
            log.info("response изменён после ошибки валидации: {}", response);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (UnsupportedCodeException e) {
            log.error("UnsupportedCodeException: {}", e.getMessage());
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNSUPPORTED_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNSUPPORTED);
            log.info("response изменён после UnsupportedCodeException: {}", response);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: {}", e.getMessage(), e);
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNKNOWN_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNKNOWN);
            log.info("response изменён после непредвиденной ошибки: {}", response);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        modifyResponseService.modify(response);
        log.info("Итоговый response: {}", response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
