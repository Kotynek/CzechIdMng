package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Repository for connection between {@link AccPasswordFilterDto} and {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccPasswordFilterSystemRepository extends AbstractEntityRepository<AccPasswordFilterSystem> {

}
