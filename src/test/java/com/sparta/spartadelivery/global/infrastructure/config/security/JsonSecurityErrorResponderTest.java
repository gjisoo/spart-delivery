package com.sparta.spartadelivery.global.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

class JsonSecurityErrorResponderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSecurityErrorResponder errorResponder = new JsonSecurityErrorResponder(objectMapper);

    @Test
    @DisplayName("Security 예외 응답을 공통 JSON 형식으로 작성한다")
    void write() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        errorResponder.write(response, 401, "INVALID_TOKEN");
        var responseBody = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(responseBody.get("status").asInt()).isEqualTo(401);
        assertThat(responseBody.get("message").asText()).isEqualTo("INVALID_TOKEN");
        assertThat(responseBody.get("data").isNull()).isTrue();
        assertThat(responseBody.get("errors").isNull()).isTrue();
    }
}
