package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Password filter definition and connection to {@link SysSystem}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Entity
@Table(name = "acc_password_filter_system", indexes = {
		@Index(name = "ux_acc_pass_fil_id_sys_id", columnList = "system_id,password_filter_id", unique = true),
		@Index(name = "idx_sys_system_id", columnList = "system_id"),
		@Index(name = "idx_acc_password_filter_id", columnList = "password_filter_id") })
public class AccPasswordFilterSystem extends AbstractEntity {

	private static final long serialVersionUID = -7569692160380146896L;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private SysSystem system;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "password_filter_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private AccPasswordFilter passwordFilter;

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public AccPasswordFilter getPasswordFilter() {
		return passwordFilter;
	}

	public void setPasswordFilter(AccPasswordFilter passwordFilter) {
		this.passwordFilter = passwordFilter;
	}

}
