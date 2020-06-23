package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Password filter definition
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Entity
@Table(name = "acc_password_filter", indexes = {
		@Index(name = "ux_acc_pass_filter_code", columnList = "code", unique = true) })
public class AccPasswordFilter extends AbstractEntity implements Codeable, Disableable {

	private static final long serialVersionUID = -3410060867589840750L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;

	@Audited
	@Size(min = 1, max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

	@Audited
	@Column(name = "disabled", nullable = false)
	private boolean disabled;

	@Audited
	@Column(name = "change_in_idm", nullable = false)
	private boolean changeInIdm;

	@Audited
	@Column(name = "timeout")
	private long timeout = 216000;

	@Audited
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "transformation_script")
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
