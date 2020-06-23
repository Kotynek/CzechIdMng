package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for connection between {@link AccPasswordFilterDto} and {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccPasswordFilterSystemFilter extends DataFilter {

	public static final String PARAMETER_PASSWORD_FILTER_ID = "passwordFilterId";
	public static final String PARAMETER_SYSTEM_ID = "systemId";
	
	public AccPasswordFilterSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public AccPasswordFilterSystemFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public AccPasswordFilterSystemFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(AccPasswordFilterSystemDto.class, data, parameterConverter);
	}

	public UUID getPasswordFilterId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_PASSWORD_FILTER_ID);
	}

	public void setPasswordFilterId(UUID passwordFilterId) {
		set(PARAMETER_PASSWORD_FILTER_ID, passwordFilterId);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}
}
