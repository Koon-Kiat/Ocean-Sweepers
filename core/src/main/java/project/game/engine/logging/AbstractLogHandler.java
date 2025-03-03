package project.game.engine.logging;

import project.game.engine.api.logging.ILoggerEvent;
import project.game.engine.api.logging.ILoggerHandler;

/**
 * Abstract base implementation of ILogHandler.
 * Provides common functionality for log handlers.
 */
public abstract class AbstractLogHandler implements ILoggerHandler {
    private boolean enabled = true;
    private ILoggerHandler nextHandler = null;

    @Override
    public final boolean handle(ILoggerEvent event) {
        if (!enabled || event == null) {
            return false;
        }
        return doHandle(event);
    }

    protected abstract boolean doHandle(ILoggerEvent event);

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ILoggerHandler getNext() {
        return nextHandler;
    }

    @Override
    public void setNext(ILoggerHandler next) {
        this.nextHandler = next;
    }

    @Override
    public abstract void close();

    @Override
    public abstract void flush();
}