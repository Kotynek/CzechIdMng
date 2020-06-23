package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter;
import eu.bcvsolutions.idm.acc.repository.AccPasswordFilterRepository;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of {@link AccPasswordFilterService}. The service is
 * used only for standard CRUD operations.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Service("accPasswordFilterService")
public class DefaultAccPasswordFilterService
		extends AbstractEventableDtoService<AccPasswordFilterDto, AccPasswordFilter, AccPasswordFilterFilter>
		implements AccPasswordFilterService {

	private final AccPasswordFilterRepository repository;

	@Autowired
	public DefaultAccPasswordFilterService(AccPasswordFilterRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);

		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.PASSWORDFILTER, getEntityClass());
	}

	@Override
	public AccPasswordFilterDto getByCode(String code) {
		return this.toDto(repository.findOneByCode(code));
	}
}
