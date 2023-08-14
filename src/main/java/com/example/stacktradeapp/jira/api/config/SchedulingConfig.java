package com.example.stacktradeapp.jira.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@ConditionalOnProperty(name = "scheduling.enabled", matchIfMissing = true)
public class SchedulingConfig {

}
