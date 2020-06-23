package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccPasswordFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Password filter definition repository
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccPasswordFilterRepository extends AbstractEntityRepository<AccPasswordFilter> {

	AccPasswordFilter findOneByCode(@Param("code") String code);
}
