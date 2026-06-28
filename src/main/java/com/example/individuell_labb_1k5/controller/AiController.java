package com.example.individuell_labb_1k5.controller;


import com.example.individuell_labb_1k5.dto.AiResponseDto;
import com.example.individuell_labb_1k5.service.AiClientService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiClientService aiClientService;
    private final Validator validator;

    public AiController(AiClientService aiClientService, Validator validator) {
        this.aiClientService = aiClientService;
        this.validator = validator;
    }

    @PostMapping
    public AiResponseDto analyzeGame(@RequestBody String gameName) {
        AiResponseDto result = aiClientService.analyzeGame(gameName);

        Set<ConstraintViolation<AiResponseDto>> violations = validator.validate(result);
        if (!violations.isEmpty()) {
            log.warn("AI response failed validation for game '{}'. Violations: {}", gameName, violations);
            return createFallbackResponse(gameName);
        }

        return result;
    }

    private AiResponseDto createFallbackResponse(String gameName) {
        AiResponseDto fallback = new AiResponseDto();
        fallback.setGameName(gameName);
        fallback.setGood(List.of("N/A"));
        fallback.setBad(List.of("N/A"));
        fallback.setScore(0);
        fallback.setSummary("AI response failed validation rules.");
        return fallback;
    }
}
