package com.studyassistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StudyAssistantApplication {

    private static final Logger log = LoggerFactory.getLogger(StudyAssistantApplication.class);

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(StudyAssistantApplication.class, args);
    }

    /**
     * Automatically loads environment properties from a local .env file if present.
     */
    private static void loadDotEnv() {
        List<Path> candidatePaths = List.of(
            Path.of(".env"),
            Path.of("../.env"),
            Path.of(".env.local"),
            Path.of("../.env.local")
        );

        for (Path path : candidatePaths) {
            if (Files.exists(path)) {
                try {
                    log.info("Loading configuration from: {}", path.toAbsolutePath());
                    List<String> lines = Files.readAllLines(path);
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                            continue;
                        }
                        int equalIdx = trimmed.indexOf('=');
                        if (equalIdx > 0) {
                            String key = trimmed.substring(0, equalIdx).trim();
                            String value = trimmed.substring(equalIdx + 1).trim();
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }
                            if (System.getProperty(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    break;
                } catch (Exception e) {
                    log.warn("Could not read .env file at {}: {}", path, e.getMessage());
                }
            }
        }
    }
}
