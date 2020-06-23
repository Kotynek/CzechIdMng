package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Connection password filter and systems.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Relation(collectionRelation = "passwordFilterSystems")
public class AccPasswordFilterSystemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = AccPasswordFilterDto.class)
	private UUID passwordFilter;

	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;

	public UUID getPasswordFilter() {
		return passwordFilter;
	}

	public void setPasswordFilter(UUID passwordFilter) {
		this.passwordFilter = passwordFilter;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

}
