package ru.bushuevavioletta.MySecondTestAppSpringBoot.service;

import org.junit.jupiter.api.Test;
import ru.bushuevavioletta.MySecondTestAppSpringBoot.model.Positions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class AnnualBonusServiceImplTest {

    private final AnnualBonusServiceImpl service = new AnnualBonusServiceImpl();

    @Test
    void calculate() {

        // given
        Positions position = Positions.HR;
        double bonus = 2.0;
        int workDays = 243;
        double salary = 100000.00;

        // when
        double result = service.calculate(position, salary, bonus, workDays, 2023);

        // then
        double expected = 360493.8271604938;
        assertThat(result).isCloseTo(expected, within(0.000001));
    }

    @Test
    void calculateInLeapYear() {

        // given: 2024 – високосный год, 366 дней
        Positions position = Positions.HR;

        // when
        double result = service.calculate(position, 100000.00, 2.0, 243, 2024);

        // then
        double expected = 100000.00 * 2.0 * 366 * 1.2 / 243;
        assertThat(result).isCloseTo(expected, within(0.000001));
    }

    @Test
    void getDaysInYear() {
        assertThat(service.getDaysInYear(2023)).isEqualTo(365);
        assertThat(service.getDaysInYear(2024)).isEqualTo(366);
        assertThat(service.getDaysInYear(1900)).isEqualTo(365);
        assertThat(service.getDaysInYear(2000)).isEqualTo(366);
    }

    @Test
    void calculateQuarterlyBonusForManager() {

        // given
        Positions position = Positions.TL;
        double bonus = 2.0;
        int workDays = 243;
        double salary = 100000.00;

        // when
        double result = service.calculateQuarterlyBonus(position, salary, bonus, workDays, 2023);

        // then: четверть годовой премии
        double expected = 195267.48971193415;
        assertThat(result).isCloseTo(expected, within(0.000001));
    }

    @Test
    void calculateQuarterlyBonusForNotManager() {
        assertThatThrownBy(() ->
                service.calculateQuarterlyBonus(Positions.DEV, 100000.00, 2.0, 243, 2023))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("только управленцам");
    }
}