package com.certsniper.passstory;

import static org.assertj.core.api.Assertions.assertThat;

import com.certsniper.passstory.dto.PassStoryRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PassStoryRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private boolean valid(String score) {
        BigDecimal passScore = score == null ? null : new BigDecimal(score);
        return validator.validate(new PassStoryRequest("1320", "본문", null, null, null, null, passScore, null)).isEmpty();
    }

    @Test
    void 합격점수는_0부터_1500까지_허용() {
        assertThat(valid(null)).isTrue();
        assertThat(valid("0")).isTrue();
        assertThat(valid("1500")).isTrue();
        assertThat(valid("1500.00")).isTrue();
    }

    @Test
    void 합격점수가_범위를_벗어나면_거부() {
        assertThat(valid("1500.01")).isFalse();
        assertThat(valid("99999")).isFalse();
        assertThat(valid("-0.01")).isFalse();
        assertThat(valid("10.123")).isFalse();
    }
}
