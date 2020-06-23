package eu.bcvsolutions.idm.acc.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModelProperty;

/**
 * Password filter request for validate or change password. DTO is not persisted
 * to database. DTO is used only for transfer information from password filter
 * to IdM.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class PasswordFilterRequestDto implements Serializable {

	private static final long serialVersionUID = 4717439614769746981L;

	@JsonDeserialize(using = GuardedStringDeserializer.class)
	@ApiModelProperty(notes = "New password.", dataType = "string")
	private GuardedString password;

	@NotEmpty
	@ApiModelProperty(notes = "Identifier of identity on end system. The attribute may be sAMAccountName.", dataType = "string")
	private String username;

	@NotEmpty
	@ApiModelProperty(notes = "Resource system identifier.", dataType = "string")
	private String resource;

	@ApiModelProperty(notes = "Log identifier that connect request from end system to IdM.", dataType = "string")
	private String logIdentifier;

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getLogIdentifier() {
		return logIdentifier;
	}

	public void setLogIdentifier(String logIdentifier) {
		this.logIdentifier = logIdentifier;
	}

}
