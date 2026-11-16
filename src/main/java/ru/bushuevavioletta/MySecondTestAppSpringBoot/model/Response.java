package ru.bushuevavioletta.MySecondTestAppSpringBoot.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {

    /**
     * Уникальный идентификатор сообщения
     */
    private String uid;

    /**
     * Уникальный идентификатор операции
     */
    private String operationUid;

    /**
     * Время создания ответного сообщения
     */
    private String systemTime;

    /**
     * Результат обработки запроса: success или failed
     */
    private Codes code;

    /**
     * Код ошибки
     */
    private ErrorCodes errorCode;

    /**
     * Сообщение об ошибке
     */
    private ErrorMessages errorMessage;

    /**
     * Годовая премия сотрудника
     */
    private Double annualBonus;

    /**
     * Квартальная премия (рассчитывается только для управленцев)
     */
    private Double quarterlyBonus;
}
