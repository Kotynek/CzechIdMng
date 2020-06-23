package eu.bcvsolutions.idm.acc.config.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import eu.bcvsolutions.idm.acc.service.impl.DefaultPasswordFiltertManager;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for password filter definitions
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface PasswordFilterConfiguration extends Configurable {

	/**
	 * Configuration for initialize {@link SCryptPasswordEncoder}. Default values are from standard constructor.
	 * {@link PasswordEncoder} is initialized int {@link DefaultPasswordFiltertManager} in constructor! All configuration options can
	 * be changed only with restart!
	 */
	String PROPERTY_SCRYPT_CPU_COST = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.passFilter.scrypt.cpuCost";
	int DEFAULT_SCRYPT_CPU_COST = 16384;

	String PROPERTY_SCRYPT_MEMORY_COST = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.passFilter.scrypt.memoryCost";
	int DEFAULT_SCRYPT_MEMORY_COST = 8;

	String PROPERTY_SCRYPT_PARALLELIZATION = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.passFilter.scrypt.parallelization";
	int DEFAULT_SCRYPT_PARALLELIZATION = 1;

	String PROPERTY_SCRYPT_KEY_LENGTH = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.passFilter.scrypt.keyLength";
	int DEFAULT_SCRYPT_KEY_LENGTH = 32;

	String PROPERTY_SCRYPT_SALT_LENGTH = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "acc.passFilter.scrypt.saltLength";
	int DEFAULT_SCRYPT_SALT_LENGTH = 64;
	

	@Override
	default String getConfigurableType() {
		// Password filter is there with shortcut because string password made all configuration properties confidential
		return "passFilter";
	}

	@Override
	default boolean isDisableable() {
		return false;
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(10);

		properties.add(PROPERTY_SCRYPT_CPU_COST);
		properties.add(PROPERTY_SCRYPT_MEMORY_COST);
		properties.add(PROPERTY_SCRYPT_PARALLELIZATION);
		properties.add(PROPERTY_SCRYPT_KEY_LENGTH);
		properties.add(PROPERTY_SCRYPT_SALT_LENGTH);
		return properties;
	}

	/**
	 * Get CPU cost for Scrypt. For change the property is required restart whole application.
	 *
	 * @return
	 */
	int getScryptCpuCost();

	/**
	 * Get memory cost for Scrypt. For change the property is required restart whole application.
	 * @return
	 */
	int getScryptMemoryCost();

	/**
	 * Get parallelization for Scrypt. For change the property is required restart whole application.
	 * @return
	 */
	int getScryptParallelization();

	/**
	 * Get key length for Scrypt. For change the property is required restart whole application.
	 * @return
	 */
	int getScryptKeyLength();

	/**
	 * Get salt length for Scrypt. For change the property is required restart whole application.
	 * @return
	 */
	int getScryptSaltLength();
}
