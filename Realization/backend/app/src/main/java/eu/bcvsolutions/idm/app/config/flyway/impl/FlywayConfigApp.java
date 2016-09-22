package eu.bcvsolutions.idm.app.config.flyway.impl;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import eu.bcvsolutions.idm.core.config.flyway.AbstractFlywayConfiguration;
import eu.bcvsolutions.idm.core.config.flyway.IdmFlywayAutoConfiguration;

@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = false)
@AutoConfigureAfter(IdmFlywayAutoConfiguration.IdmFlywayConfiguration.class)
@EnableConfigurationProperties(FlywayProperties.class)
@PropertySource("classpath:/flyway-app.properties")
public class FlywayConfigApp extends AbstractFlywayConfiguration {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlywayConfigApp.class);
	
	@Bean
	@DependsOn("flywayCore")
	@ConditionalOnMissingBean(name = "flywayApp")
	@ConditionalOnExpression("${flyway.enabled:true} && '${flyway.app.locations}'!=''")
	@ConfigurationProperties(prefix = "flyway.app")
	public Flyway flywayApp() {
		Flyway flyway = super.createFlyway();
		log.info("Starting flyway migration for app module [{}]: ", flyway.getTable());
		return flyway;
	}
}
