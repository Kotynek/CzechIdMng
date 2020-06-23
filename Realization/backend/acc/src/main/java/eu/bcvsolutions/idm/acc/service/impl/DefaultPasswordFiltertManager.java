package eu.bcvsolutions.idm.acc.service.impl;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.config.domain.PasswordFilterConfiguration;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterEchoItemDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterSystemDto;
import eu.bcvsolutions.idm.acc.dto.PasswordFilterRequestDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilterSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccPasswordFilterSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterService;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordFilterSystemService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.script.evaluator.DefaultSystemScriptEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Default implementation of {@link DefaultPasswordFiltertManager}.
 * Storage implementation can be changed by override protected method:<br/ ><br/ >
 * - {@link #isEchoForAccount(UUID, GuardedString, long)}	- check echo in storage<br/ >
 * - {@link #setEcho(UUID, GuardedString)}					- set echo to storage<br/ >
 * - {@link #getPasswordEncoder()}							- get default password hash function implementation<br/ >
 * - {@link #hashPassword(GuardedString)}					- hash password (default {@link SCryptPasswordEncoder})<br/ >
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Service("passwordFiltertManager")
public class DefaultPasswordFiltertManager implements PasswordFilterManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultPasswordFiltertManager.class);

	@Autowired
	private AccPasswordFilterService passwordFilterService;
	@Autowired
	private AccPasswordFilterSystemService passwordFilterSystemService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmPasswordPolicyService policyService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private DefaultSystemScriptEvaluator systemScriptEvaluator;
	@Autowired
	private GroovyScriptService groovyScriptService;
	@Autowired
	private IdmCacheManager idmCacheManager; // For default implementation storage of ECHO's
	@Autowired
	private ProvisioningService provisioningService;
	@Autowired
	private IdmPasswordService passwordService;
	@Autowired
	private PasswordFilterConfiguration passwordFilterConfiguration;
	private PasswordEncoder encoder;

	public DefaultPasswordFiltertManager() {
	}
	
	@Override
	public boolean validate(PasswordFilterRequestDto request) {
		SysSystemDto system = getSystem(request.getResource());
		AccPasswordFilterDto passwordFilterDto = getActivePasswordFilterDefinition(system);
		final GuardedString password = request.getPassword();

		IdmIdentityDto identityDto = evaluateUsernameToIdentity(system, request, passwordFilterDto);

		List<AccAccountDto> accounts = getFinalAccounts(identityDto, system, passwordFilterDto, password);

		if (accounts.isEmpty()) {
			// Only echos
			LOG.info("Request for identity identifier [{}] and system identifier [{}] has only echos. Log identifier [{}].",
					request.getUsername(), system.getId(), StringUtils.defaultString(request.getLogIdentifier()));
			return true;
		}

		// Get systems for accounts
		List<SysSystemDto> systemsForAccounts = getSystemForAccounts(accounts);
		// Get managed system from password filter definition
		List<SysSystemDto> systemsManaged = getSystemsForFilter(passwordFilterDto);

		// Remove not managed systems
		systemsForAccounts.removeIf(sys -> {
			// Remove if system isn't in managed
			return !systemsManaged.contains(sys);
		});

		// Get password policies from managed systems
		List<IdmPasswordPolicyDto> policies = getPasswordPolicy(systemsForAccounts);

		// Default password policy must be also added when is setup change trough IdM
		if (passwordFilterDto.isChangeInIdm()) {
			IdmPasswordPolicyDto defaultPasswordPolicy = policyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			// Password policy can be added by some system check for duplicate
			if (!policies.contains(defaultPasswordPolicy)) {
				policies.add(defaultPasswordPolicy);
			}
		}

		// Compose validation request for IdM
		IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
		passwordValidationDto.setPassword(request.getPassword());
		passwordValidationDto.setIdentity(identityDto);

		try {
			LOG.info("Start validation for system identifier [{}]. Log identifier [{}].",
					request.getUsername(), StringUtils.defaultString(request.getLogIdentifier()));
			policyService.validate(passwordValidationDto , policies);
		} catch (Exception e) {
			LOG.error("Validation didn't pass! Error message: [{}]. Log identifier [{}].",
					StringUtils.defaultString(e.getMessage()),
					StringUtils.defaultString(request.getLogIdentifier()));
			return false;
		}

		// Password valid
		return true;
	}

	@Override
	public void change(PasswordFilterRequestDto request) {
		SysSystemDto system = getSystem(request.getResource());
		AccPasswordFilterDto passwordFilterDto = getActivePasswordFilterDefinition(system);
		final GuardedString password = request.getPassword();
		boolean changeInIdm = passwordFilterDto.isChangeInIdm();

		IdmIdentityDto identityDto = evaluateUsernameToIdentity(system, request, passwordFilterDto);

		List<AccAccountDto> accounts = getFinalAccounts(identityDto, system, passwordFilterDto, password);

		if (accounts.isEmpty()) {
			LOG.info("For identifier [{}] (identity: [{}]) and resource [{}] wasn't found any account for change. Log identifier [{}].",
					request.getUsername(), identityDto.getUsername(), request.getResource(), StringUtils.defaultString(request.getLogIdentifier()));
			return;
		} else {
			LOG.info("For identifier [{}] (identity: [{}]) and resource [{}]  was found these accounts for change [{}]. Log identifier [{}].",
					request.getUsername(), identityDto.getUsername(), request.getResource(),
					Strings.join(accounts, ','),
					StringUtils.defaultString(request.getLogIdentifier()));
		}

		// Set echos for accounts
		List<String> accountsIds = Lists.newArrayList();
		accounts.forEach(account -> {
			accountsIds.add(account.getId().toString());
			
			setEcho(account.getId(), password);
		});

		// Prepare request for password change
		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(false);
		passwordChangeDto.setIdm(changeInIdm);
		passwordChangeDto.setAccounts(accountsIds);
		passwordChangeDto.setNewPassword(password);

		// If is changed made also trought IdM, it is required setup max password age.
		if (changeInIdm) {
			IdmPasswordPolicyDto defaultValidatePolicy = policyService
					.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			if (defaultValidatePolicy != null && defaultValidatePolicy.getMaxPasswordAge() != null) {
				// put new valid till by default password policy
				passwordChangeDto.setMaxPasswordAge(ZonedDateTime.now().plusDays(defaultValidatePolicy.getMaxPasswordAge()));
			} else {
				passwordChangeDto.setMaxPasswordAge(null);
				LOG.info("Default validate password policy not exists or max password age is not filled."
						+ " For identity username [{}] will be valid till null.", identityDto.getUsername());
			}
		}

		// Check echo for IdM and process new password thought IdM and also set ECHO for IdM
		if (changeInIdm && !isEchoForAccount(identityDto.getId(), password, passwordFilterDto.getTimeout())) {
			// Change in IdM.
			setEcho(identityDto.getId(), password);
			passwordService.save(identityDto, passwordChangeDto);
		}

		// Change for systems
		provisioningService.changePassword(identityDto, passwordChangeDto);
	}

	@Override
	public void setEcho(UUID accountId, GuardedString password) {
		AccPasswordFilterEchoItemDto echo = new AccPasswordFilterEchoItemDto(hashPassword(password), accountId);
		
		idmCacheManager.cacheValue(ECHO_CACHE_NAME, accountId, echo);
	}

	/**
	 * Process all given information from {@link PasswordFilterRequestDto} and {@link AccPasswordFilterDto} and then
	 * evaluate {@link IdmIdentityDto} thought very defensive behavior:
	 * 
	 *  1. - check script for identity transformation,
	 *  2. - check if exist UID in given system,
	 *  3. - check identities username's.
	 *
	 * @param system
	 * @param request
	 * @param passwordFilter
	 * @return
	 */
	protected IdmIdentityDto evaluateUsernameToIdentity(SysSystemDto system, PasswordFilterRequestDto request, AccPasswordFilterDto passwordFilter) {
		String script = passwordFilter.getTransformationScript();
		String usernameRequest = request.getUsername();

		if (StringUtils.isBlank(script)) {
			LOG.warn("Transfromation script for password filter id [{}] doesn't exist. For request for username [{}] and system [{}] will be used fallback behavior - account first. Log identifier [{}].",
					passwordFilter.getId(), usernameRequest, system.getId(), StringUtils.defaultString(request.getLogIdentifier()));

			// First we will try find account by uid
			AccAccountDto account = accountService.getAccount(usernameRequest, system.getId());
			if (account == null) {
				LOG.warn("First fallback failed! Account doesn't found for request for username [{}] and system [{}] will be used fallback behavior - identity. Log identifier [{}].",
						usernameRequest, system.getId(), StringUtils.defaultString(request.getLogIdentifier()));

				// Second we will try find direct identity by username
				IdmIdentityDto identityDto = identityService.getByUsername(usernameRequest);
				if (identityDto == null) {
					LOG.error("Identity for request for username [{}] and system [{}] cannot be found. Log identifier [{}].",
							usernameRequest, system.getId(), StringUtils.defaultString(request.getLogIdentifier()));
				}
				return identityDto;
			}
			
			IdmIdentityDto identityDto = identityService.get(account.getTargetEntityId());
			if (identityDto == null) {
				LOG.error("Identity for request for username [{}], system [{}] and account id [{}] cannot be found. Log identifier [{}].",
						usernameRequest, system.getId(), account.getId(), StringUtils.defaultString(request.getLogIdentifier()));
			}
			return identityDto;
		}
		
		// Standard behavior with script
		Map<String, Object> variables = new HashMap<>();
		variables.put(SCRIPT_SYSTEM_PARAMETER, system);
		variables.put(SCRIPT_USERNAME_PARAMETER, request.getUsername());
		variables.put(SCRIPT_LOG_IDENTIFIER_PARAMETER, request.getLogIdentifier());
		variables.put(SCRIPT_PASSWORD_FILTER_PARAMETER, passwordFilter);

		// Add system script evaluator for call another scripts
		variables.put(AbstractScriptEvaluator.SCRIPT_EVALUATOR,	systemScriptEvaluator);

		// Add access for script evaluator
		List<Class<?>> extraClass = new ArrayList<>();
		extraClass.add(AbstractScriptEvaluator.Builder.class);
		extraClass.add(IdmIdentityDto.class);
		extraClass.add(SysSystemDto.class);
		extraClass.add(AccPasswordFilterDto.class);


		Object result = groovyScriptService.evaluate(script, variables, extraClass);
		if (result instanceof IdmIdentityDto) {
			return (IdmIdentityDto) result;
		} else {
			throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_BE_DELETED_IS_PROTECTED, "TODO");
		}
	}

	/**
	 * Get all accounts for given identities. The method executes real DB query.
	 *
	 * @param identity
	 * @return
	 */
	private List<AccAccountDto> getAccounts(IdmIdentityDto identity) {
		AccAccountFilter filter = new AccAccountFilter();
		filter.setIdentityId(identity.getId());

		// Send as new list - unmodifiable
		return Lists.newArrayList(accountService.find(filter, null).getContent());
	}

	/**
	 * Prepare final list of accounts for identity without echos and original resource.
	 *
	 * @param identity
	 * @param originalResource
	 * @param passwordFilterDto
	 * @param password
	 * @return
	 */
	private List<AccAccountDto> getFinalAccounts(IdmIdentityDto identity, SysSystemDto originalResource, AccPasswordFilterDto passwordFilterDto, GuardedString password) {
		// All accounts for identity. TODO: performance improvements - search only account for list of systems
		List<AccAccountDto> accounts = getAccounts(identity);
		
		// Remove not managed accounts, ECHO's and system itself
		final long timeout = passwordFilterDto.getTimeout();
		accounts.removeIf(account -> {
			return passwordFilterSystemService.isAccountManaged(account.getId()) || // Not managed system
					isEchoForAccount(account.getId(), password, timeout) || // ECHO
					account.getSystem().equals(originalResource.getId()); // System itself
		});

		return accounts;
	}

	/**
	 * Get all validate password policies for given list of systems. Method use
	 * embedded object not real DB query.
	 *
	 * @param systems
	 * @return
	 */
	private List<IdmPasswordPolicyDto> getPasswordPolicy(List<SysSystemDto> systems) {
		List<IdmPasswordPolicyDto> policies = Lists.newArrayList();

		systems.forEach(system -> {
			IdmPasswordPolicyDto policy = DtoUtils.getEmbedded(system, SysSystem_.passwordPolicyValidate, IdmPasswordPolicyDto.class);
			if (!policies.contains(policy)) {
				policies.add(policy);
			}
		});

		return policies;
	}

	/**
	 * Get system from given list accounts. Method use embedded object
	 * not real DB query.
	 *
	 * @param accounts
	 * @return
	 */
	private List<SysSystemDto> getSystemForAccounts(List<AccAccountDto> accounts) {
		List<SysSystemDto> systems = Lists.newArrayList();

		accounts.forEach(account -> {
			SysSystemDto systemDto = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);

			// For one system is possible more accounts. 
			if (!systems.contains(systemDto)) {
				systems.add(systemDto);
			}
		});

		return systems;
	}

	/**
	 * Find all defined system fot given password filter definition.
	 * Method us real DB query.
	 *
	 * @param passwordFilter
	 * @return
	 */
	private List<SysSystemDto> getSystemsForFilter(AccPasswordFilterDto passwordFilter) {
		List<SysSystemDto> systems = Lists.newArrayList();
		
		AccPasswordFilterSystemFilter filter = new AccPasswordFilterSystemFilter();
		filter.setPasswordFilterId(passwordFilter.getId());
		
		List<AccPasswordFilterSystemDto> content = passwordFilterSystemService.find(filter, null).getContent();
		content.forEach(pfs -> {
			// Check for duplicity isn't needed. System can be only once in all filters 
			systems.add(DtoUtils.getEmbedded(pfs, AccPasswordFilterSystem_.system, SysSystemDto.class));
		});

		return systems;
	}

	/**
	 * Get system by given ID or name/code (standard lookup behavior).
	 *
	 * @param resource
	 * @return
	 */
	private SysSystemDto getSystem(String resource) {
		Assert.notNull(resource, "Resource cannot be null!");
		SysSystemDto system = (SysSystemDto) lookupService.lookupDto(SysSystemDto.class, resource);
		Assert.notNull(system, "System not found!");
		return system;
	}

	/**
	 * Get active password filter definition for given system. System can be now only in
	 * one password definition.
	 * TODO: this behavior can be changed in future.
	 *
	 * @param systemDto
	 * @return
	 */
	private AccPasswordFilterDto getActivePasswordFilterDefinition(SysSystemDto systemDto) {
		AccPasswordFilterFilter filter = new AccPasswordFilterFilter();
		filter.setSystemId(systemDto.getId());
		filter.setDisabled(false);
		List<AccPasswordFilterDto> filters = passwordFilterService.find(filter, null).getContent();
		
		Assert.notEmpty(filters, "For system doesn't exists password filter definition!");

		// For system is now possible set only one filter definition.
		return filters.get(0);
	}

	/**
	 * Check if exists valid echo record for given account id, password with timeout.
	 *
	 * @param accountId
	 * @param password
	 * @param timeout
	 * @return
	 */
	protected boolean isEchoForAccount(UUID accountId, GuardedString password, long timeout) {
		ValueWrapper value = idmCacheManager.getValue(ECHO_CACHE_NAME, accountId);
		if (value == null) {
			return false;
		}

		Object echoAsObject = value.get();
		if (echoAsObject == null) {
			return false;
		}

		AccPasswordFilterEchoItemDto echo = (AccPasswordFilterEchoItemDto) echoAsObject;
		return echo.isEcho(password, getPasswordEncoder(), timeout);
	}

	/**
	 * Get password encoder implementation.
	 *
	 * @return
	 */
	protected PasswordEncoder getPasswordEncoder() {
		// Cannot be initialized in constructor because password filter configuration are autowired
		if (this.encoder == null) {
			this.encoder = new SCryptPasswordEncoder(
					passwordFilterConfiguration.getScryptCpuCost(),
					passwordFilterConfiguration.getScryptMemoryCost(),
					passwordFilterConfiguration.getScryptParallelization(),
					passwordFilterConfiguration.getScryptKeyLength(),
					passwordFilterConfiguration.getScryptSaltLength());
		}

		return this.encoder;
	}

	/**
	 * Implementation of hashing password to echos storage.
	 *
	 * @param password
	 * @return
	 */
	protected String hashPassword(GuardedString password) {
		return getPasswordEncoder().encode(password.asString());
	}
}
