package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Password filter filter :)
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class AccPasswordFilterFilter extends DataFilter implements DisableableFilter {

	public static final String PARAMETER_SYSTEM_ID = "systemId";
	public static final String PARAMETER_CHANGE_IN_IDM = "changeInIdm";

	public AccPasswordFilterFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public AccPasswordFilterFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public AccPasswordFilterFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(AccPasswordFilterDto.class, data, parameterConverter);
	}

	public UUID getSystemId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SYSTEM_ID);
	}

	public void setSystemId(UUID systemId) {
		set(PARAMETER_SYSTEM_ID, systemId);
	}

	public Boolean isChangeInIdm() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_CHANGE_IN_IDM);
	}

	public void setChangeInIdm(Boolean changeInIdm) {
		set(PARAMETER_CHANGE_IN_IDM, changeInIdm);
	}
}
