# Command Journaling

## An Advanced Command Pattern Implementation with Quarkus

This project showcases an advanced implementation of the **Command Pattern** built on Quarkus, designed for robust, traceable, and maintainable applications. It leverages concepts like command journaling, automatic recovery, idempotency, and composite commands to provide a solid foundation for distributed and asynchronous systems.

The architecture clearly separates the intention of an action (the _command descriptor_) from its execution logic (the _command_ itself), enhancing decoupling and extensibility.

## Architecture Highlights

The project's architecture is structured around two main modules:

### 1. `core` Module

This module encapsulates the foundational framework components and core interfaces, promoting a clean separation of concerns. Key elements include:

- **Command Descriptor (`CommandDescriptor`)**: An immutable, serializable DTO representing the intent to execute an action.
- **Command (`ICommand` / `Command`)**: The object containing the actual business logic to be executed.
- **Command Factory (`ICommandFactory`)**: A centralized builder for command objects based on their descriptors.
- **Command Manager (`ICommandManager`)**: Orchestrates command execution, handling journaling and state transitions.
- **Journaling (`JournalService` & `JournalRepository`)**: Records all command executions and their state changes for auditability and recovery.
- **Automatic Recovery (`CommandRecoveryService`)**: Ensures system consistency by re-executing pending commands after failures.
- **Composite Commands (`CommandComposite`)**: Enables grouping multiple commands into a single, transactional unit.

### 2. `application` Module

This module contains concrete, application-specific implementations of commands, resources, and domain models, built upon the `core` framework. Examples include:

- User management commands (`CreateUserCommand`, `DeleteUserCommand`)
- Todo list operations (`TodoCommand`)
- Sleep commands (`SleepCommand`)
- Specific composite command implementations

## Key Features

- **Decoupling**: Clear separation between command definition (`Descriptor`) and execution (`Command`).
- **Comprehensive Journaling**: Full audit trail of all actions and state changes.
- **Automatic Recovery**: Guarantees system consistency by handling interrupted operations.
- **Idempotency**: Commands are designed for safe re-execution without side effects.
- **Composite Commands**: Orchestrates complex workflows as atomic transactions.
- **Extensibility**: Easily add new commands by defining a descriptor and a command class.

## Getting Started

1.  Ensure Docker is running (for MongoDB via Dev Services).
2.  From the project root, execute:
    ```sh
    ./mvnw quarkus:dev
    ```
    Quarkus Dev Services will automatically start and configure a MongoDB instance.

## Adding a New Command

The architecture is designed for easy extension:

1.  **Create the Descriptor**: Define a new `class` extending `CommandDescriptor` in the `application` module (e.g., `application.user.model`).

    ```java
    public class CreateUserDescriptor extends CommandDescriptor {

    private final String username;
    private final String email;
        // ...
    }
    ```

2.  **Create the Command Class**: Implement the business logic in a new class extending `Command` within the `application` module.
    ```java
    @ApplicationScoped
    public class CreateUserCommand extends Command<String> {
        @Override
        protected String doExecute() {
            // ... implement user creation logic here
            return "User created";
        }
    }
    ```
3.  **Expose via API (Optional)**: If needed, add an endpoint in a `Resource` class in the `application` module to trigger your new command.

The `CommandFactory` will automatically discover and use new commands based on naming conventions, requiring no manual modification to the core.

## API Endpoints (Examples)

- `POST /api/commands` - Executes a generic `CommandDescriptor`.
- `POST /api/composites/{name}` - Executes a predefined composite command.
- `GET /api/journal` - Retrieves all journal entries.
- `GET /api/journal/{commandId}` - Retrieves a specific journal entry.
- `GET /api/statistics/commands` - Provides command execution statistics.

## Requirements

- Java 21
- Maven 3.8+
- Docker (for MongoDB via Dev Services)

## Technologies

- Quarkus
- MongoDB (with Panache)
- Jakarta CDI
- REST (JAX-RS)
