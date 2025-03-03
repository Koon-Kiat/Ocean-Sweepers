package project.game.engine.api.constant;

import java.util.Map;

import project.game.engine.constant.AbstractConfigurableConstants;

/**
 * Interface for loading and saving configuration data
 */
public interface IConfigurationLoader {
    /**
     * Load configuration from a source
     */
    boolean loadConfiguration(String source, String profileName, AbstractConfigurableConstants constants);

    /**
     * Save configuration to a destination
     */
    boolean saveConfiguration(String destination, AbstractConfigurableConstants constants);

    /**
     * Read configuration data from the source
     */
    Map<String, Object> readConfigurationData(String source) throws Exception;

    /**
     * Write configuration data to the destination
     */
    boolean writeConfigurationData(String destination, Map<String, Object> data) throws Exception;
}