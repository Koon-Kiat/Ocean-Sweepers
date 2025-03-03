package project.game.engine.api.logging;

/**
 * Interface for handling log events.
 * Handlers are responsible for processing log events, such as
 * writing them to a file or console, sending them to a network, etc.
 * This follows the Chain of Responsibility pattern.
 */
public interface ILogHandler {
    /**
     * Handles a log event.
     *
     * @param event the log event to handle
     * @return true if the event was handled and should not be passed to other
     *         handlers
     */
    boolean handle(ILogEvent event);

    /**
     * Gets the next handler in the chain.
     *
     * @return the next handler, or null if none
     */
    ILogHandler getNext();

    /**
     * Sets the next handler in the chain.
     *
     * @param next the next handler
     */
    void setNext(ILogHandler next);

    /**
     * Closes this handler, releasing any resources.
     */
    void close();

    /**
     * Flushes any buffered logs.
     */
    void flush();

    /**
     * Sets whether this handler is enabled.
     *
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);

    /**
     * Checks if this handler is enabled.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}