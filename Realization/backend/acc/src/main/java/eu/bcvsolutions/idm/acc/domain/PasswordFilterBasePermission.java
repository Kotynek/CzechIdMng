package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Password filter specific endpoints base permissions.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public enum PasswordFilterBasePermission implements BasePermission {
	
	CHANGEPASSWORD, // change password
	VALIDATEPASSWORD; // validate password
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getModule() {
		// common base permission without module
		return AccModuleDescriptor.MODULE_ID;
	}
}
