package org.howread.app.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.howread.common.exception.BusinessException;
import org.howread.common.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler 단위 테스트.
 *
 * standaloneSetup을 사용하는 이유:
 * - @WebMvcTest 슬라이스는 Spring Boot 4.x의 Security/JPA 자동 설정 변경으로
 *   컨텍스트 구성이 복잡해졌다.
 * - standaloneSetup은 컨트롤러와 핸들러만 직접 와이어링하여 격리된 테스트를 보장한다.
 * - 이 테스트의 관심사는 "예외가 발생했을 때 응답 구조가 올바른가"이므로
 *   Spring 컨텍스트 전체가 필요하지 않다.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("BusinessException 발생 시 ErrorCode의 HTTP 상태와 에러 구조로 응답한다")
    void handleBusinessException_returnsErrorCodeStatus() throws Exception {
        mockMvc.perform(get("/fake/business-exception")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.RESOURCE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.error.message").value(CommonErrorCode.RESOURCE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("처리되지 않은 예외 발생 시 500과 COMMON_500 코드로 응답한다")
    void handleException_returnsInternalServerError() throws Exception {
        mockMvc.perform(get("/fake/unhandled-exception")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    @DisplayName("@Valid 검증 실패 시 400과 필드별 커스텀 메시지로 응답한다")
    void handleMethodArgumentNotValid_returnsBadRequestWithFieldMessage() throws Exception {
        mockMvc.perform(post("/fake/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.error.message").value("이름은 필수입니다."));
    }

    /**
     * 테스트 전용 더미 컨트롤러.
     * 각 엔드포인트가 특정 예외를 의도적으로 발생시켜 핸들러의 동작을 검증한다.
     */
    @RestController
    static class FakeController {

        @GetMapping("/fake/business-exception")
        public void throwBusinessException() {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        @GetMapping("/fake/unhandled-exception")
        public void throwUnhandledException() {
            throw new RuntimeException("unexpected error");
        }

        @PostMapping("/fake/valid")
        public void validRequest(@RequestBody @Valid FakeRequest request) {
        }
    }

    record FakeRequest(@NotBlank(message = "이름은 필수입니다.") String name) {
    }
}
