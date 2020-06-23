package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Password filter definition. Used only for standard crud operation.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccPasswordFilterService extends//
		EventableDtoService<AccPasswordFilterDto, AccPasswordFilterFilter>,
		AuthorizableService<AccPasswordFilterDto>,
		CodeableService<AccPasswordFilterDto> {

}
