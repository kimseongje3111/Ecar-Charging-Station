package com.ecar.servicestation.modules.ecar.service;

import com.ecar.servicestation.infra.data.dto.EVInfoDto;
import com.ecar.servicestation.infra.data.service.ECarChargingStationInfoProvider;
import io.netty.handler.timeout.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ECarSearchAsyncService {

    private static int MAX_API_REQUESTS = 5;

    private final ECarChargingStationInfoProvider dataProvider;

    @Async
    public CompletableFuture<Set<EVInfoDto>> getSearchResult(String search, int retryCount) {
        while (retryCount < MAX_API_REQUESTS) {
            try {
                return CompletableFuture.completedFuture(dataProvider.getData(search, 1, 20));

            } catch (TimeoutException te) {
                log.error("Timeout error");

                try {
                    Thread.sleep(1000);
                    retryCount++;

                } catch (InterruptedException ie) {
                    log.error("Thread error");

                    throw new RuntimeException(ie);
                }
            }
        }

        return CompletableFuture.completedFuture(new HashSet<>());
    }
}
