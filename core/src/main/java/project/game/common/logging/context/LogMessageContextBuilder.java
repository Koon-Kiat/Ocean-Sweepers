package project.game.common.logging.context;

/**
 * Builder for creating LogMessageContext instances with a fluent API.
 * This builder makes it easy to create and customize log contexts.
 */
public class LogMessageContextBuilder {
    private final LogMessageContext context;

    /**
     * Creates a new LogMessageContextBuilder.
     */
    public LogMessageContextBuilder() {
        context = new LogMessageContext();
    }

    /**
     * Sets the operation name.
     * 
     * @param operationName the operation name
     * @return this builder
     */
    public LogMessageContextBuilder operation(String operationName) {
        context.setOperationName(operationName);
        return this;
    }

    /**
     * Sets the transaction ID.
     * 
     * @param transactionId the transaction ID
     * @return this builder
     */
    public LogMessageContextBuilder transaction(String transactionId) {
        context.setTransactionId(transactionId);
        return this;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId the user ID
     * @return this builder
     */
    public LogMessageContextBuilder user(String userId) {
        context.setUserId(userId);
        return this;
    }

    /**
     * Adds a context value.
     * 
     * @param key   the context key
     * @param value the context value
     * @return this builder
     */
    public LogMessageContextBuilder with(String key, Object value) {
        context.addContextValue(key, value);
        return this;
    }

    /**
     * Adds multiple context values from an array of key-value pairs.
     * The array must have an even number of elements, with keys at even indices
     * and values at odd indices.
     * 
     * @param keyValuePairs the key-value pairs
     * @return this builder
     * @throws IllegalArgumentException if the array length is odd
     */
    public LogMessageContextBuilder withAll(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide an even number of arguments (key/value pairs)");
        }

        for (int i = 0; i < keyValuePairs.length; i += 2) {
            context.addContextValue(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
        }
        return this;
    }

    /**
     * Builds the LogMessageContext.
     * 
     * @return the built context
     */
    public LogMessageContext build() {
        return context;
    }
}