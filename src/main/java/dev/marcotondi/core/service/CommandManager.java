package dev.marcotondi.core.service;

import java.util.concurrent.CompletableFuture;

import dev.marcotondi.core.api.Command;
import dev.marcotondi.journal.domain.JournalEntry;

public interface CommandManager {
    <R> CompletableFuture<R> dispatchAsync(Command<R> command);
    <R> R dispatch(Command<R> command);
    <R> R executeInTransaction(Command<R> command, JournalEntry entry);
}
