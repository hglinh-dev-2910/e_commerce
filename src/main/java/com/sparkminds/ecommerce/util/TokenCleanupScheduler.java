package com.sparkminds.ecommerce.util;

import com.sparkminds.ecommerce.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 */30 * * * *") // 30mins
    @Transactional
    public void cleanupExpiredBlacklistedTokens() {

        int deleted = invalidatedTokenRepository.deleteExpiredTokens(new Date());

        if (deleted > 0) {
            log.info(
                    "[BLACKLIST CLEANUP] Deleted {} expired tokens",
                    deleted
            );
        }
    }
}