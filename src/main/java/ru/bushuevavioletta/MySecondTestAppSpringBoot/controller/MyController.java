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
import ru.bushuevavioletta.MySecondTestAppSpringBoot.service.AnnualBonusService;
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
    private final AnnualBonusService annualBonusService;

    @Autowired
    public MyController(ValidationService validationService,
                        @Qualifier("ModifySystemTimeResponseService")
                        ModifyResponseService modifyResponseService,
                        @Qualifier("ModifySourceRequestService")
                        ModifyRequestService modifySourceRequestService,
                        @Qualifier("ModifySystemNameRequestService")
                        ModifyRequestService modifySystemNameRequestService,
                        AnnualBonusService annualBonusService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifySourceRequestService = modifySourceRequestService;
        this.modifySystemNameRequestService = modifySystemNameRequestService;
        this.annualBonusService = annualBonusService;
    }

    @PostMapping(value = "/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
                                             BindingResult bindingResult) {

        Date receivedTime = new Date();
        log.info("request: {}", request);

        Response response = buildResponse(request);

        try {
            validationService.isValid(bindingResult);
            if ("123".equals(request.getUid())) {
                throw new UnsupportedCodeException("uid = 123 не поддерживается");
            }
            calculateBonuses(request, response);
            forwardToService2(request, receivedTime);
        } catch (ValidationFailedException e) {
            return error(response, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION,
                    HttpStatus.BAD_REQUEST, e);
        } catch (UnsupportedCodeException e) {
            return error(response, ErrorCodes.UNSUPPORTED_EXCEPTION, ErrorMessages.UNSUPPORTED,
                    HttpStatus.BAD_REQUEST, e);
        } catch (Exception e) {
            return error(response, ErrorCodes.UNKNOWN_EXCEPTION, ErrorMessages.UNKNOWN,
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        modifyResponseService.modify(response);
        log.info("Итоговый response: {}", response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Начальный ответ: success и пустые коды ошибок
    private Response buildResponse(Request request) {
        Response response = Response.builder()
                .uid(request.getUid())
                .operationUid(request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();
        log.info("response создан: {}", response);
        return response;
    }

    // Годовая премия – всем, квартальная – только управленцам
    private void calculateBonuses(Request request, Response response) {
        if (request.getPosition() == null || request.getSalary() == null
                || request.getBonus() == null || request.getWorkDays() == null) {
            log.info("Данные для расчёта премии не переданы, премия не рассчитывается");
            return;
        }
        response.setAnnualBonus(annualBonusService.calculate(request.getPosition(),
                request.getSalary(), request.getBonus(), request.getWorkDays()));
        if (request.getPosition().isManager()) {
            response.setQuarterlyBonus(annualBonusService.calculateQuarterlyBonus(request.getPosition(),
                    request.getSalary(), request.getBonus(), request.getWorkDays()));
        }
        log.info("Премии рассчитаны: годовая = {}, квартальная = {}",
                response.getAnnualBonus(), response.getQuarterlyBonus());
    }

    // Модификация request и пересылка его в Сервис 2
    private void forwardToService2(Request request, Date receivedTime) {
        request.setSystemTime(DateTimeUtil.getCustomFormat().format(receivedTime));
        modifySourceRequestService.modify(request);
        modifySystemNameRequestService.modify(request);
    }

    // Заполнение ответа при ошибке
    private ResponseEntity<Response> error(Response response, ErrorCodes errorCode,
                                           ErrorMessages errorMessage, HttpStatus status,
                                           Exception e) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        response.setCode(Codes.FAILED);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        log.info("response после ошибки: {}", response);
        return new ResponseEntity<>(response, status);
    }
}
