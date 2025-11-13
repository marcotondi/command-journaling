package dev.marcotondi.core.domain;

/**
 * An interface for objects that can be initialized with data.
 * In the command pattern context, this is used to initialize a command object
 * with its specific descriptor.
 *
 * @param <T> The type of data to initialize the object with.
 */
public interface Initializable<T> {

    /**
     * Initializes the object with the given data.
     * @param data The data to initialize the command with.
     */
    void init(T data);
}
