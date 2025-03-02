package project.game.common.logging.api;

/**
 * Strategy interface for determining project root path.
 * Follows the Strategy pattern to allow different implementations.
 */
public interface ProjectPathStrategy {
    /**
     * Gets the project root path for storing logs.
     * 
     * @return the absolute path to the project root directory
     * @throws Exception if the path cannot be determined
     */
    String getProjectRootPath() throws Exception;
}