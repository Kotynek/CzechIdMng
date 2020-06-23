package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.PasswordFilterRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Password filter manager that works with {@link AccPasswordFilterDto} and made
 * password changes and validation brought {@link IdmPasswordPolicyDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface PasswordFilterManager extends ScriptEnabled {

	public static final String SCRIPT_SYSTEM_PARAMETER = "system";
	public static final String SCRIPT_USERNAME_PARAMETER = "username";
	public static final String SCRIPT_LOG_IDENTIFIER_PARAMETER = "logIdentifier";
	public static final String SCRIPT_PASSWORD_FILTER_PARAMETER = "passwordFilter";
	
	public static final String ECHO_CACHE_NAME = AccModuleDescriptor.MODULE_ID + ":password-filter-echo-cache";

	/**
	 * Process validation trought password policies stored in IdM.
	 *
	 * @param request
	 * @return
	 */
	boolean validate(PasswordFilterRequestDto request);

	/**
	 * Process change password with definition stored in {@link AccPasswordFilterDto}.
	 * Password change is processed without validation!
	 *
	 * @param request
	 */
	void change(PasswordFilterRequestDto request);

	/**
	 * Set echo to echo storage implementation.
	 *
	 * @param accountId
	 * @param password
	 */
	void setEcho(UUID accountId, GuardedString password);
}
