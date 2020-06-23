package eu.bcvsolutions.idm.acc.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Default implementation of {@link PasswordFilterConfiguration}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component("passwordFilterConfiguration")
public class DefaultPasswordFilterConfiguration extends AbstractConfiguration implements PasswordFilterConfiguration {

	@Override
	public int getScryptCpuCost() {
		return getConfigurationService().getIntegerValue(PROPERTY_SCRYPT_CPU_COST, DEFAULT_SCRYPT_CPU_COST);
	}

	@Override
	public int getScryptMemoryCost() {
		return getConfigurationService().getIntegerValue(PROPERTY_SCRYPT_MEMORY_COST, DEFAULT_SCRYPT_MEMORY_COST);
	}

	@Override
	public int getScryptParallelization() {
		return getConfigurationService().getIntegerValue(PROPERTY_SCRYPT_PARALLELIZATION, DEFAULT_SCRYPT_PARALLELIZATION);
	}

	@Override
	public int getScryptKeyLength() {
		return getConfigurationService().getIntegerValue(PROPERTY_SCRYPT_KEY_LENGTH, DEFAULT_SCRYPT_KEY_LENGTH);
	}

	@Override
	public int getScryptSaltLength() {
		return getConfigurationService().getIntegerValue(PROPERTY_SCRYPT_SALT_LENGTH, DEFAULT_SCRYPT_SALT_LENGTH);
	}

	
}
