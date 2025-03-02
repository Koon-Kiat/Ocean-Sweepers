package project.game.common.logging.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents contextual information that can be attached to log messages.
 * This class helps separate abstract logging from context-specific information.
 */
public class LogMessageContext {
    private Map<String, Object> contextData = new HashMap<>();
    private String operationName;
    private String transactionId;
    private long timestamp;
    private String userId;

    /**
     * Creates a new LogMessageContext with the current timestamp.
     */
    public LogMessageContext() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Creates a new LogMessageContext with the specified operation name.
     * 
     * @param operationName the name of the operation being logged
     */
    public LogMessageContext(String operationName) {
        this();
        this.operationName = operationName;
    }

    /**
     * Sets the operation name.
     * 
     * @param operationName the operation name
     * @return this context instance for chaining
     */
    public LogMessageContext setOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    /**
     * Gets the operation name.
     * 
     * @return the operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Sets the transaction ID.
     * 
     * @param transactionId the transaction ID
     * @return this context instance for chaining
     */
    public LogMessageContext setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    /**
     * Gets the transaction ID.
     * 
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId the user ID
     * @return this context instance for chaining
     */
    public LogMessageContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the user ID.
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the timestamp when this context was created.
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Adds a context value.
     * 
     * @param key   the context key
     * @param value the context value
     * @return this context instance for chaining
     */
    public LogMessageContext addContextValue(String key, Object value) {
        contextData.put(key, value);
        return this;
    }

    /**
     * Gets a context value.
     * 
     * @param key the context key
     * @return the context value or null if not found
     */
    public Object getContextValue(String key) {
        return contextData.get(key);
    }

    /**
     * Gets all context data as an unmodifiable map.
     * 
     * @return the context data map
     */
    public Map<String, Object> getAllContextData() {
        return Collections.unmodifiableMap(contextData);
    }

    /**
     * Creates a copy of this context.
     * 
     * @return a new context with the same values
     */
    public LogMessageContext copy() {
        LogMessageContext copy = new LogMessageContext();
        copy.operationName = this.operationName;
        copy.transactionId = this.transactionId;
        copy.timestamp = this.timestamp;
        copy.userId = this.userId;
        copy.contextData.putAll(this.contextData);
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        if (operationName != null) {
            sb.append("operation=").append(operationName).append(", ");
        }

        if (transactionId != null) {
            sb.append("txId=").append(transactionId).append(", ");
        }

        if (userId != null) {
            sb.append("user=").append(userId).append(", ");
        }

        if (!contextData.isEmpty()) {
            sb.append("context={");
            boolean first = true;
            for (Map.Entry<String, Object> entry : contextData.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("}");
        } else if (sb.length() > 1) {
            // Remove trailing comma and space
            sb.setLength(sb.length() - 2);
        }

        sb.append("]");
        return sb.toString();
    }
}