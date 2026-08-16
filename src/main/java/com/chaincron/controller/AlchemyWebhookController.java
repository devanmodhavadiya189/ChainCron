package com.chaincron.controller;

import com.chaincron.dto.webhook.AlchemyWebhookPayload;
import com.chaincron.service.webhook.AlchemyWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/alchemy")
@RequiredArgsConstructor
public class AlchemyWebhookController {

    private static final String ALCHEMY_SIGNATURE_HEADER = "X-Alchemy-Signature";

    private final AlchemyWebhookService webhookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/address-activity")
    public ResponseEntity<Void> handleAddressActivity(
            @RequestHeader(value = ALCHEMY_SIGNATURE_HEADER, required = false) String signature,
            @RequestBody String rawBody
    ) {
        if (signature == null || signature.isBlank()) {
            log.warn("Webhook received without signature header — rejecting");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!webhookService.verifySignature(rawBody, signature)) {
            log.warn("Webhook HMAC verification failed — rejecting request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AlchemyWebhookPayload payload = objectMapper.readValue(rawBody, AlchemyWebhookPayload.class);
            webhookService.processPayload(payload);
        } catch (Exception e) {
            log.error("Failed to parse Alchemy webhook payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok().build();
    }
}
