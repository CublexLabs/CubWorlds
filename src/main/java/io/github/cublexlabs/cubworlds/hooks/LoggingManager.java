package io.github.cublexlabs.cubworlds.hooks;

import io.github.cublexlabs.cubworlds.Main;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingManager {

    private final Main plugin;
    private final Logger logger;

    public LoggingManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void customLogger(String message, String level) {
        if (plugin.isCublexCoreLoaded() && plugin.getConfig().getBoolean("modules.custom-logger", true)) {
            try {
                Level logLevel = Level.parse(level.toUpperCase());
                logger.log(logLevel, message);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid log level: " + level);
                logger.info(message);
            }
        }
    }

    public void info(String message) {
        customLogger(message, "INFO");
    }

    public void warning(String message) {
        customLogger(message, "WARNING");
    }

    public void severe(String message) {
        customLogger(message, "SEVERE");
    }
}