package com.instantupi.offline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This class is used for application-level configuration.
 *
 * It mainly enables scheduling support so that we can run background tasks
 * (like cleanup jobs, periodic checks, etc.) automatically.
 */
@Configuration   // Marks this class as a configuration class (like XML config in old Spring)
@EnableScheduling // Enables Spring's scheduling feature (@Scheduled methods will work)
public class AppConfig {

}