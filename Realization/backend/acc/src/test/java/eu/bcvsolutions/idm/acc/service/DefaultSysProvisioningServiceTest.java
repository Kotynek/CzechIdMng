package eu.bcvsolutions.idm.acc.service;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSysProvisioningService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.repository.IdmFormAttributeDefinitionRepository;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormDefinitionService;
import eu.bcvsolutions.idm.icf.service.api.IcfConfigurationFacade;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorFacade;
import eu.bcvsolutions.idm.security.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Provisioning tests
 * 
 * @author Svanda
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultSysProvisioningServiceTest extends AbstractIntegrationTest {

	private static final String IDENTITY_PASSWORD_ONE = "password_one";
	private static final String IDENTITY_PASSWORD_TWO = "password_two";
	private static final String IDENTITY_USERNAME = "provisioningTestUser";

	@Autowired
	private SysSystemService sysSystemService;

	@Autowired
	private FormService formService;

	@Autowired
	private IdmIdentityService idmIdentityService;

	@Autowired
	private AccIdentityAccountService identityAccoutnService;

	@Autowired
	private AccAccountService accoutnService;

	@Autowired
	private SysProvisioningService provisioningService;

	@Autowired
	private SysSystemEntityHandlingService systemEntityHandlingService;

	@Autowired
	private SysSchemaAttributeHandlingService schemaAttributeHandlingService;

	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	DataSource dataSource;

	@Before
	public void init() {
		loginAsAdmin("admin");
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void doIdentityProvisioningAddAccount() {
		initData();

		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		provisioningService.doIdentityProvisioning(accountIdentityOne.getIdentity());

		TestResource createdAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertNotNull(createdAccount);
		Assert.assertEquals(identity.getFirstName(), createdAccount.getFirstname());
	}

	@Test
	public void doIdentityProvisioningChangeAccount() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);

		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		TestResource createdAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());

		identity.setFirstName("changedFirstName");
		identity = idmIdentityService.save(identity);
		Assert.assertNotEquals(identity.getFirstName(), createdAccount.getFirstname());

		provisioningService.doIdentityProvisioning(identity);
		TestResource changedAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertNotNull(changedAccount);
		Assert.assertEquals(identity.getFirstName(), changedAccount.getFirstname());
	}

	@Test
	public void doIdentityProvisioningChangePassword() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);
		SysSystem system = accountIdentityOne.getAccount().getSystem();

		// Check empty password
		provisioningService.authenticate(accountIdentityOne, system);

		// Create new password one
		PasswordChangeDto passwordChange = new PasswordChangeDto();
		passwordChange.setIdentity(identity.getId().toString());
		passwordChange.setResources(ImmutableList.of(accountIdentityOne.getId().toString()));
		passwordChange.setNewPassword(IDENTITY_PASSWORD_ONE.getBytes());
		// Do change of password for selected accounts
		idmIdentityService.passwordChange(identity, passwordChange);
		accountIdentityOne = identityAccoutnService.get(accountIdentityOne.getId());

		// Check correct password One
		provisioningService.authenticate(accountIdentityOne, system);

		// Check incorrect password
		try {
			accountIdentityOne.getIdentity().setPassword(IDENTITY_PASSWORD_TWO.getBytes());
			provisioningService.authenticate(accountIdentityOne, system);
			fail("Bad credentials exception is expected here!");
		} catch (ResultCodeException ex) {
			//
		}
		// Do change of password for selected accounts
		idmIdentityService.passwordChange(accountIdentityOne.getIdentity(), passwordChange);

		// Check correct password Two
		accountIdentityOne = identityAccoutnService.get(accountIdentityOne.getId());
		provisioningService.authenticate(accountIdentityOne, system);

	}

	@Test
	public void doIdentityProvisioningDeleteAccount() {
		IdmIdentity identity = idmIdentityService.getByName(IDENTITY_USERNAME);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);
		AccIdentityAccount accountIdentityOne = identityAccoutnService.find(filter, null).getContent().get(0);

		// Delete account
		provisioningService.deleteAccount(accountIdentityOne.getAccount());
		TestResource removedAccount = entityManager.find(TestResource.class, accountIdentityOne.getAccount().getUid());
		Assert.assertNull(removedAccount);
	}

	/**
	 * Create test system connected to same database (using configuration from
	 * dataSource)
	 * 
	 * @return
	 */
	private SysSystem createTestSystem() {
		// create owner
		org.apache.tomcat.jdbc.pool.DataSource tomcatDataSource = ((org.apache.tomcat.jdbc.pool.DataSource) dataSource);
		SysSystem system = new SysSystem();
		system.setName("testResource_" + System.currentTimeMillis());
		system.setConnectorKey(new SysConnectorKey(sysSystemService.getTestConnectorKey()));
		sysSystemService.save(system);

		IdmFormDefinition savedFormDefinition = sysSystemService.getConnectorFormDefinition(system.getConnectorKey());

		List<SysSystemFormValue> values = new ArrayList<>();

		SysSystemFormValue jdbcUrlTemplate = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcUrlTemplate"));
		jdbcUrlTemplate.setValue(tomcatDataSource.getUrl());
		values.add(jdbcUrlTemplate);
		SysSystemFormValue jdbcDriver = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("jdbcDriver"));
		jdbcDriver.setValue(tomcatDataSource.getDriverClassName());
		values.add(jdbcDriver);

		SysSystemFormValue user = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("user"));
		user.setValue(tomcatDataSource.getUsername());
		values.add(user);
		SysSystemFormValue password = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("password"));
		password.setValue(tomcatDataSource.getPoolProperties().getPassword());
		values.add(password);
		SysSystemFormValue table = new SysSystemFormValue(savedFormDefinition.getMappedAttributeByName("table"));
		table.setValue("test_resource");
		values.add(table);
		SysSystemFormValue keyColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("keyColumn"));
		keyColumn.setValue("name");
		values.add(keyColumn);
		SysSystemFormValue passwordColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("passwordColumn"));
		passwordColumn.setValue("password");
		values.add(passwordColumn);
		SysSystemFormValue allNative = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("allNative"));
		allNative.setValue(true);
		values.add(allNative);
		SysSystemFormValue rethrowAllSQLExceptions = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("rethrowAllSQLExceptions"));
		rethrowAllSQLExceptions.setValue(true);
		values.add(rethrowAllSQLExceptions);
		SysSystemFormValue statusColumn = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("statusColumn"));
		statusColumn.setValue("status");
		values.add(statusColumn);
		SysSystemFormValue disabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("disabledStatusValue"));
		disabledStatusValue.setValue("disabled");
		values.add(disabledStatusValue);
		SysSystemFormValue enabledStatusValue = new SysSystemFormValue(
				savedFormDefinition.getMappedAttributeByName("enabledStatusValue"));
		enabledStatusValue.setValue("enabled");
		values.add(enabledStatusValue);

		formService.saveValues(system, values);

		return system;
	}

	private void initData() {
		IdmIdentity identity;
		AccAccount accountOne;
		AccIdentityAccount accountIdentityOne;
		SysSystem system;

		// create test system
		system = createTestSystem();

		// generate schema for system
		sysSystemService.generateSchema(system);

		// Create test identity for provisioning test
		identity = new IdmIdentity();
		identity.setUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_USERNAME);
		identity.setLastName(IDENTITY_USERNAME);
		identity = idmIdentityService.save(identity);

		accountOne = new AccAccount();
		accountOne.setSystem(system);
		accountOne.setUid("x" + IDENTITY_USERNAME);
		accountOne.setAccountType(AccountType.PERSONAL);
		accountOne = accoutnService.save(accountOne);

		accountIdentityOne = new AccIdentityAccount();
		accountIdentityOne.setIdentity(identity);
		accountIdentityOne.setOwnership(true);
		accountIdentityOne.setAccount(accountOne);

		accountIdentityOne = identityAccoutnService.save(accountIdentityOne);

		SysSystemEntityHandling entityHandling = new SysSystemEntityHandling();
		entityHandling.setEntityType(SystemEntityType.IDENTITY);
		entityHandling.setOperationType(SystemOperationType.PROVISIONING);
		entityHandling.setSystem(system);
		final SysSystemEntityHandling entityHandlingResult = systemEntityHandlingService.save(entityHandling);

		SchemaAttributeFilter schemaAttributeFilter = new SchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttribute> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if ("__NAME__".equals(schemaAttr.getName())) {
				SysSchemaAttributeHandling attributeHandlingName = new SysSchemaAttributeHandling();
				attributeHandlingName.setUid(true);
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemEntityHandling(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSchemaAttributeHandling attributeHandlingName = new SysSchemaAttributeHandling();
				attributeHandlingName.setIdmPropertyName("firstName");
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemEntityHandling(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSchemaAttributeHandling attributeHandlingName = new SysSchemaAttributeHandling();
				attributeHandlingName.setIdmPropertyName("lastName");
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemEntityHandling(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			} else if (IcfConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				SysSchemaAttributeHandling attributeHandlingName = new SysSchemaAttributeHandling();
				attributeHandlingName.setIdmPropertyName("password");
				attributeHandlingName.setSchemaAttribute(schemaAttr);
				attributeHandlingName.setSystemEntityHandling(entityHandlingResult);
				schemaAttributeHandlingService.save(attributeHandlingName);

			}
		});
	}

}
