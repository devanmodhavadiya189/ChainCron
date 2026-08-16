package com.chaincron.service.webhook;

import com.chaincron.config.AppProperties;
import com.chaincron.domain.entity.User;
import com.chaincron.domain.repository.UserRepository;
import com.chaincron.dto.webhook.AlchemyWebhookPayload;
import com.chaincron.service.credit.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.utils.Numeric;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlchemyWebhookService {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final CreditService creditService;

    public boolean verifySignature(String rawBody, String signatureHeader) {
        try {
            String secret = appProperties.getAlchemy().getWebhookAuthToken();
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computedHex = HexFormat.of().formatHex(computed);
            return computedHex.equalsIgnoreCase(signatureHeader);
        } catch (Exception e) {
            log.error("HMAC verification failed", e);
            return false;
        }
    }

    public void processPayload(AlchemyWebhookPayload payload) {
        if (!"ADDRESS_ACTIVITY".equals(payload.getType())) {
            return;
        }

        List<AlchemyWebhookPayload.Activity> activities = payload.getEvent().getActivity();
        if (activities == null || activities.isEmpty()) {
            return;
        }

        String platformWallet = appProperties.getEthereum().getPlatformWalletAddress();

        for (AlchemyWebhookPayload.Activity activity : activities) {
            if (!"ETH".equals(activity.getAsset())) {
                continue;
            }
            if (!platformWallet.equalsIgnoreCase(activity.getToAddress())) {
                continue;
            }
            processDeposit(activity);
        }
    }

    private void processDeposit(AlchemyWebhookPayload.Activity activity) {
        String fromAddress = activity.getFromAddress();
        String txHash = activity.getHash();
        BigDecimal amountWei = resolveAmountWei(activity);

        if (amountWei.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Webhook deposit with zero/negative amount: txHash={}", txHash);
            return;
        }

        Optional<User> userOpt = userRepository.findByWalletAddressIgnoreCase(fromAddress);
        if (userOpt.isEmpty()) {
            log.warn("Webhook: no user found for fromAddress={} txHash={} — deposit unmatched", fromAddress, txHash);
            return;
        }

        User user = userOpt.get();
        creditService.deposit(user.getId(), amountWei, txHash);
        log.info("Webhook deposit processed: userId={} amount={} wei txHash={}", user.getId(), amountWei, txHash);
    }

    private BigDecimal resolveAmountWei(AlchemyWebhookPayload.Activity activity) {
        try {
            if (activity.getRawContract() != null && activity.getRawContract().getRawValue() != null) {
                BigInteger rawWei = Numeric.decodeQuantity(activity.getRawContract().getRawValue());
                return new BigDecimal(rawWei);
            }
        } catch (Exception e) {
            log.warn("Failed to parse rawValue, falling back to float value field");
        }
        if (activity.getValue() != null) {
            BigDecimal ethValue = activity.getValue();
            return ethValue.multiply(new BigDecimal("1000000000000000000"));
        }
        return BigDecimal.ZERO;
    }
}
