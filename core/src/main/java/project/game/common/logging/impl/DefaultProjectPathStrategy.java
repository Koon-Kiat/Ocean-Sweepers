package project.game.common.logging.impl;

import java.io.File;

import project.game.common.logging.api.ProjectPathStrategy;
import project.game.common.util.ProjectPaths;

/**
 * Default implementation of ProjectPathStrategy that uses ProjectPaths utility.
 * Ensures that logs are always stored in the project root directory regardless
 * of where the logger is instantiated.
 */
public class DefaultProjectPathStrategy implements ProjectPathStrategy {
    @Override
    public String getProjectRootPath() {
        try {
            // Get the base project root using the utility class
            String baseProjectRoot = ProjectPaths.getProjectRoot();
            File projectDir = new File(baseProjectRoot);

            // Navigate up to the root project directory if we're in a subdirectory
            while (projectDir != null && projectDir.getName().equals("core")) {
                File parent = projectDir.getParentFile();
                if (parent != null && parent.exists()) {
                    projectDir = parent;
                } else {
                    break;
                }
            }

            return projectDir.getAbsolutePath();
        } catch (Exception e) {
            // If we can't determine the project root, return the working directory
            return new File("").getAbsolutePath();
        }
    }
}