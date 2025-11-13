package dev.marcotondi.infra;

import java.util.concurrent.CompletableFuture;

import dev.marcotondi.domain.api.Command;
import dev.marcotondi.domain.entity.JournalEntry;

public interface CommandManager {

    <R> CompletableFuture<R> dispatchAsync(Command<R> command);

    <R> R dispatch(Command<R> command);

    <R> R dispatch(Command<R> command, JournalEntry entry);

}
