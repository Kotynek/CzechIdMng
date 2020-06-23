package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModelProperty;

/**
 * Password filter definition
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Relation(collectionRelation = "passwordFilters")
public class AccPasswordFilterDto extends AbstractDto implements Codeable, Disableable {

	private static final long serialVersionUID = 1L;

	private String code;
	private String description;
	private boolean disabled;
	@ApiModelProperty(notes = "Change password also in IdM.")
	private boolean changeInIdm;
	@ApiModelProperty(notes = "Timeout in seconds for removing echos.")
	private long timeout = 216000;
	@ApiModelProperty(notes = "Transformation script for transform given username from system to IdmIdentityDto.")
	private String transformationScript;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isChangeInIdm() {
		return changeInIdm;
	}

	public void setChangeInIdm(boolean changeInIdm) {
		this.changeInIdm = changeInIdm;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getTransformationScript() {
		return transformationScript;
	}

	public void setTransformationScript(String transformationScript) {
		this.transformationScript = transformationScript;
	}

}
