# BUG Fix
1. Recover Composite Command
```
2025-11-07 18:37:57,453 INFO  [dev.mar.ser.CommandRecoveryService] (Quarkus Main Thread) Found 1 interrupted commands to recover.
2025-11-07 18:37:57,455 INFO  [dev.mar.ser.CommandRecoveryService] (Quarkus Main Thread) Attempting to recover command ID: 7916309a-7e73-4049-9a6c-0dff6a071c18 (dev.marcotondi.domain.model.CompositeCommand)
2025-11-07 18:37:57,656 ERROR [dev.mar.ser.CommandRecoveryService] (Quarkus Main Thread) Failed to recover command 7916309a-7e73-4049-9a6c-0dff6a071c18. Manual intervention may be required.: jakarta.json.bind.JsonbException: Unable to deserialize property 'commands' because of: Cannot infer a type for unmarshalling into: dev.marcotondi.domain.model.Command
        at org.eclipse.yasson.internal.deserializer.JsonbCreatorDeserializer.deserialize(JsonbCreatorDeserializer.java:91)
        at org.eclipse.yasson.internal.deserializer.JsonbCreatorDeserializer.deserialize(JsonbCreatorDeserializer.java:38)
```