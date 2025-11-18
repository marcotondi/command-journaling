package dev.marcotondi.core.api;

import java.util.concurrent.CompletableFuture;

public interface ICommandManager {

    <R> CompletableFuture<R> dispatchAsync(ICommand<R> command);

    <R> R dispatch(ICommand<R> command);
}
