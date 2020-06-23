package eu.bcvsolutions.idm.acc.config.cache;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterEchoItemDto;
import eu.bcvsolutions.idm.acc.dto.AttributeValueWrapperDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.impl.AbstractSynchronizationExecutor;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.core.api.config.cache.DistributedIdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.LocalIdMCacheConfiguration;

/**
 * Define caches which are used in acc module
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Configuration
public class AccCacheConfiguration {

	/**
	 * Define local only {@link IdMCacheConfiguration} used in {@link AbstractSynchronizationExecutor}.
	 * This cache has to be local only, because of how synchronization in IdM works.
	 *
	 * @return IdMCacheConfiguration for {@link AbstractSynchronizationExecutor}
	 */
	@Bean
	public IdMCacheConfiguration attributeMappingCacheConfiguration() {
		return LocalIdMCacheConfiguration.<AttributeValueWrapperDto, Object> builder()
				.withName(AbstractSynchronizationExecutor.CACHE_NAME)
				.withKeyType(AttributeValueWrapperDto.class)
				.withValueType(Object.class)
				.build();
	}

	/**
	 * Define distributed cache for {@link DefaultSysProvisioningBreakConfigService}. This enables provisioning brake feature
	 * to work properly in distributed environment with multiple IdM instances.
	 *
	 * @return IdMCacheConfiguration for {@link DefaultSysProvisioningBreakConfigService}
	 */
	@Bean
	public IdMCacheConfiguration provisioningBrakeCacheConfig() {
		return DistributedIdMCacheConfiguration.<UUID, SysProvisioningBreakItems> builder()
				.withName(DefaultSysProvisioningBreakConfigService.CACHE_NAME)
				.withKeyType(UUID.class)
				.withValueType(SysProvisioningBreakItems.class)
				.build();
	}
	
	/**
	 * Initialized synchronization executors.
	 * 
	 * @return
	 */
	@Bean
	public IdMCacheConfiguration syncExecutorCacheConfiguration() {
		return LocalIdMCacheConfiguration.<UUID, SynchronizationEntityExecutor> builder()
				.withName(SynchronizationService.SYNC_EXECUTOR_CACHE_NAME)
				.withKeyType(UUID.class)
				.withValueType(SynchronizationEntityExecutor.class)
				.build();
	}

	/**
	 * Define distributed cache for {@link AccPasswordFilterEchoItemDto}. The cache stores echos for password filters.
	 *
	 * @return {@link IdMCacheConfiguration} for {@link AccPasswordFilterEchoItemDto}
	 */
	@Bean
	public IdMCacheConfiguration passwordFilterEchoCacheConfig() {
		return DistributedIdMCacheConfiguration.<UUID, AccPasswordFilterEchoItemDto> builder()
				.withName(PasswordFilterManager.ECHO_CACHE_NAME)
				.withKeyType(UUID.class)
				.withValueType(AccPasswordFilterEchoItemDto.class)
				.build();
	}
}
