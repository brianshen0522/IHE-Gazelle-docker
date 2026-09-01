package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.validation.gateway.evs.business.exception.ValidationPendingException;
import net.ihe.gazelle.validation.gateway.evs.business.model.AsyncReport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AsyncReportState {

    private final ConcurrentMap<String, CompletableFuture<AsyncReport>> asyncValidations = new ConcurrentHashMap<>();

    public void register(String requestOid, CompletableFuture<AsyncReport> future) {
        asyncValidations.put(requestOid, future);
    }

    public AsyncReport resolve(String oid) {
        CompletableFuture<AsyncReport> future = asyncValidations.get(oid);
        if (future == null) {
            return null;
        }
        if (!future.isDone()) {
            throw new ValidationPendingException("validation-pending");
        }
        try {
            return future.join();
        } catch (CompletionException e) {
            throw new IllegalStateException("Validation execution failed", e.getCause());
        }
    }
}
