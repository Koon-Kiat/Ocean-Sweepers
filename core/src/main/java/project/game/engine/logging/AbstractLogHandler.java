package project.game.engine.logging;

import project.game.engine.api.logging.ILogEvent;
import project.game.engine.api.logging.ILogHandler;

/**
 * Abstract base implementation of ILogHandler.
 * Provides common functionality for log handlers.
 */
public abstract class AbstractLogHandler implements ILogHandler {
    private boolean enabled = true;
    private ILogHandler nextHandler = null;

    /**
     * Handles a log event if this handler is enabled.
     *
     * @param event the log event to handle
     * @return true if the event was handled and should not be passed to
     *         subsequent handlers; false otherwise
     */
    @Override
    public final boolean handle(ILogEvent event) {
        if (!enabled || event == null) {
            return false;
        }
        return doHandle(event);
    }

    /**
     * Template method to be implemented by concrete handlers.
     *
     * @param event the log event to handle
     * @return true if the event was handled and should not be passed to
     *         subsequent handlers; false otherwise
     */
    protected abstract boolean doHandle(ILogEvent event);

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ILogHandler getNext() {
        return nextHandler;
    }

    @Override
    public void setNext(ILogHandler next) {
        this.nextHandler = next;
    }

    @Override
    public abstract void close();

    @Override
    public abstract void flush();
}