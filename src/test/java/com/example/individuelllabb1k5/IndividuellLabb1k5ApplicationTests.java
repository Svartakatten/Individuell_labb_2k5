package com.example.individuelllabb1k5;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.individuell_labb_1k5.dto.AiResponseDto;
import com.example.individuell_labb_1k5.service.AiClientService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class IndividuellLabb1k5ApplicationTests {

    @Autowired
    private AiClientService aiClientService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // Initierar en MockRestServiceServer för att intercepta anrop.
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient testClient = builder.baseUrl("https://api.openai.com/v1").build();
        ReflectionTestUtils.setField(aiClientService, "restClient", testClient);
    }

    @Test
    void forceHallucination_InvalidJsonResponse_ReturnsFallbackDto() {
        String brokenJson = "Sure, here is your summary: It's a great game!";

        // Simulerar ett syntaktiskt ogiltigt svar från AI:n
        mockServer.expect(ExpectedCount.once(), requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withSuccess(brokenJson, MediaType.APPLICATION_JSON));

        AiResponseDto result = aiClientService.analyzeGame("Test Game");

        // Verifierar att try-catch-blocket i parseResponse fångar felet och returnerar fallback-DTON
        assertEquals("Unknown", result.getGameName());
        assertEquals(0, result.getScore());
        assertEquals("Unable to analyze game due to AI service error.", result.getSummary());
        mockServer.verify();
    }

    @Test
    void force429RateLimit_ExecutesRetryLoopAndThrowsException() {
        // Simulerar HTTP 429 Too Many Requests tre gånger i rad
        mockServer.expect(ExpectedCount.times(3), requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            aiClientService.analyzeGame("Test Game");
        });

        // Verifierar att exponentiell backoff körs slut och kastar korrekt undantag
        assertTrue(exception.getMessage().contains("Failed to get response from AI after retries"));
        mockServer.verify();
    }

    @Test
    void forceTimeout_ThrowsExceptionOnTimeoutStatus() {
        // Simulerar ett timeout-fel från servern
        mockServer.expect(ExpectedCount.once(), requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT));

        assertThrows(Exception.class, () -> {
            aiClientService.analyzeGame("Test Game");
        });
        mockServer.verify();
    }
}