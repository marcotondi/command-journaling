package dev.marcotondi.core;

public enum CommandStatus {
    PENDING,
    EXECUTING,
    EXECUTING_ROLL_BACK,
    COMPLETED,
    FAILED,
    ROLLED_BACK
}
