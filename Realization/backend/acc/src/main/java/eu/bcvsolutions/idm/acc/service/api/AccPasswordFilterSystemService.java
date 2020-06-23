package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterSystemFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for connection between {@link AccPasswordFilterDto} and {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccPasswordFilterSystemService extends//
		EventableDtoService<AccPasswordFilterSystemDto, AccPasswordFilterSystemFilter>,
		AuthorizableService<AccPasswordFilterSystemDto> {

	boolean isAccountManaged(UUID accountId);

	boolean isSystemManaged(UUID systemId);
}
