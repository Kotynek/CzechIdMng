package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningBreakConfiguration;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysBlockedOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakRecipientDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for provisioning break
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class ProvisioningBreakProcessorTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper testHelper;

	@Autowired
	private SysProvisioningBreakRecipientService provisioningBreakRecipient;

	@Autowired
	private SysProvisioningBreakConfigService provisioningBreakConfig;

	@Autowired
	private SysSystemService systemService;

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private AccAccountService accountService;

	@Autowired
	private AccIdentityAccountService identityAccoutnService;

	@Autowired
	private ProvisioningService provisioningService;

	@Autowired
	private IdmNotificationLogService notificationLogService;
	
	@Autowired
	private SysProvisioningBatchService batchService;
	
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	
	@Autowired
	private ProvisioningBreakConfiguration provisioningBreakConfiguration;
	
	@Autowired
	private ConfigurationService configurationService;

	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}

	@After
	public void logout() {
		clearProvisioningBreakConfiguration();
		super.logout();
	}

	@Test
	public void testRecipientIdentityIntegrity() {
		SysSystemDto systemDto = testHelper.createSystem(TestResource.TABLE_NAME);
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, null, null,
				ProvisioningEventType.CREATE, systemDto.getId());
		//
		IdmIdentityDto identityDto = testHelper.createIdentity();
		//
		this.createRecipient(breakConfig.getId(), identityDto.getId(), null);
		//
		int size = provisioningBreakRecipient.findAllByBreakConfig(breakConfig.getId()).size();
		assertEquals(1, size);
		//
		identityService.delete(identityDto);
		//
		size = provisioningBreakRecipient.findAllByBreakConfig(breakConfig.getId()).size();
		assertEquals(0, size);
	}

	@Test
	public void testRecipientRoleIntegrity() {
		SysSystemDto systemDto = testHelper.createSystem(TestResource.TABLE_NAME);
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, null, null,
				ProvisioningEventType.CREATE, systemDto.getId());
		//
		IdmRoleDto roleDto = testHelper.createRole();
		//
		this.createRecipient(breakConfig.getId(), null, roleDto.getId());
		//
		int size = provisioningBreakRecipient.findAllByBreakConfig(breakConfig.getId()).size();
		assertEquals(1, size);
		//
		roleService.delete(roleDto);
		//
		size = provisioningBreakRecipient.findAllByBreakConfig(breakConfig.getId()).size();
		assertEquals(0, size);
	}

	@Test
	public void testRecipientSystemntegrity() {
		SysSystemDto systemDto = testHelper.createSystem(TestResource.TABLE_NAME);
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, null, null,
				ProvisioningEventType.CREATE, systemDto.getId());
		//
		SysProvisioningBreakConfigDto breakConfigFounded = provisioningBreakConfig.get(breakConfig.getId());
		assertNotNull(breakConfigFounded);
		//
		systemService.delete(systemDto);
		//
		breakConfigFounded = provisioningBreakConfig.get(breakConfig.getId());
		assertNull(breakConfigFounded);
	}

	@Test
	public void testWarningUpdateOperation() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, null, 2, ProvisioningEventType.UPDATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNull(system.getBlockedOperation());
	}

	@Test
	public void testDisableUpdateOperation() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 2, null, ProvisioningEventType.UPDATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNotNull(system.getBlockedOperation());
		assertEquals(Boolean.TRUE, system.getBlockedOperation().getUpdateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getCreateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getDeleteOperation());
	}

	@Test
	public void testWarningCreateOperation() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto identity2 = testHelper.createIdentity();
		IdmIdentityDto identity3 = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, null, 3, ProvisioningEventType.CREATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		this.createAccount(system, identity2);
		this.createAccount(system, identity3);
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity2);
		provisioningService.doProvisioning(identity3);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNull(system.getBlockedOperation());
	}

	@Test
	public void testDisableCreateOperation() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto identity2 = testHelper.createIdentity();
		IdmIdentityDto identity3 = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 3, null, ProvisioningEventType.CREATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		this.createAccount(system, identity2);
		this.createAccount(system, identity3);
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity2);
		provisioningService.doProvisioning(identity3);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNotNull(system.getBlockedOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getUpdateOperation());
		assertEquals(Boolean.TRUE, system.getBlockedOperation().getCreateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getDeleteOperation());
	}
	
	@Test
	public void testWarningDeleteOperation() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto identity2 = testHelper.createIdentity();
		IdmIdentityDto identity3 = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, null, 3, ProvisioningEventType.DELETE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		this.createAccount(system, identity2);
		this.createAccount(system, identity3);
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity2);
		provisioningService.doProvisioning(identity3);
		//
		identityService.delete(identity);
		identityService.delete(identity2);
		identityService.delete(identity3);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNull(system.getBlockedOperation());
	}

	@Test
	public void testDisableDeleteOperation() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto identity2 = testHelper.createIdentity();
		IdmIdentityDto identity3 = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 3, null, ProvisioningEventType.DELETE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		this.createAccount(system, identity2);
		this.createAccount(system, identity3);
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity2);
		provisioningService.doProvisioning(identity3);
		//
		identityService.delete(identity);
		identityService.delete(identity2);
		identityService.delete(identity3);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNotNull(system.getBlockedOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getUpdateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getCreateOperation());
		assertEquals(Boolean.TRUE, system.getBlockedOperation().getDeleteOperation());
	}
	
	@Test
	public void testUpdateOperationCombination() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 4, 2, ProvisioningEventType.UPDATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity); // warning
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNull(system.getBlockedOperation());
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity); // block
		//
		filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		content = notificationLogService.find(filter, null).getContent();
		assertEquals(4, content.size()); // four notification (notification + parent) + previous
		//
		system = systemService.get(system.getId());
		assertNotNull(system.getBlockedOperation());
		assertEquals(Boolean.TRUE, system.getBlockedOperation().getUpdateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getCreateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getDeleteOperation());
	}
	
	@Test
	public void testBlockSystemCreate() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		SysBlockedOperationDto blockedOperationDto = new SysBlockedOperationDto();
		blockedOperationDto.blockCreate();
		system.setBlockedOperation(blockedOperationDto);
		system = systemService.save(system);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
		createProvisioningBreak(20l, 3, 2, ProvisioningEventType.CREATE,
				system.getId());
		//
		this.createAccount(system, identity);
		//
		provisioningService.doProvisioning(identity); // create block
		//
		SysProvisioningBatchDto batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		List<SysProvisioningOperationDto> content = provisioningOperationService.findByBatchId(batch.getId(), null).getContent();
		assertEquals(1, content.size());
		SysProvisioningOperationDto sysProvisioningOperationDto = content.get(0);
		//
		assertEquals(ProvisioningEventType.CREATE, sysProvisioningOperationDto.getOperationType());
		assertEquals(OperationState.BLOCKED, sysProvisioningOperationDto.getResult().getState());
	}
	
	@Test
	public void testBlockSystemUpdateCombination() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		//
		IdmIdentityDto identity = testHelper.createIdentity();
		createProvisioningBreak(20l, 2, null, ProvisioningEventType.UPDATE,
				system.getId());
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		//
		SysProvisioningBatchDto batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		assertNull(batch);
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity); // block
		//
		batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		List<SysProvisioningOperationDto> content = provisioningOperationService.findByBatchId(batch.getId(), null).getContent();
		assertEquals(1, content.size());
		//
		SysProvisioningOperationDto sysProvisioningOperationDto = content.get(0);
		//
		assertEquals(ProvisioningEventType.UPDATE, sysProvisioningOperationDto.getOperationType());
		assertEquals(OperationState.BLOCKED, sysProvisioningOperationDto.getResult().getState());
	}
	
	@Test
	public void testPeriodOld() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 3, null, ProvisioningEventType.UPDATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		SysProvisioningBatchDto batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		assertNull(batch);
		//
		SysProvisioningBreakItems cacheProcessedItems = provisioningBreakConfig.getCacheProcessedItems(system.getId());
		//
		// subtrack 25 minutes from all items
		long subtrackMinutes = 1500000;
		List<Long> execudedItems = cacheProcessedItems.getExecudedItems(ProvisioningEventType.UPDATE);
		// it isn't possible use foreEach or another stream function (reference)
		for (Long item : execudedItems) {
			execudedItems.set(execudedItems.indexOf(item), item - subtrackMinutes);
		}
		//
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		assertNull(batch);
	}
	
	@Test
	public void testPeriod() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 3, null, ProvisioningEventType.UPDATE,
				system.getId());
		IdmIdentityDto recipient = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient.getId(), null);
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		SysProvisioningBreakItems cacheProcessedItems = provisioningBreakConfig.getCacheProcessedItems(system.getId());
		//
		// subtrack only 19 minutes from all items
		long subtrackMinutes = 1140000;
		List<Long> execudedItems = cacheProcessedItems.getExecudedItems(ProvisioningEventType.UPDATE);
		// it isn't possible use foreEach or another stream function (reference)
		for (Long item : execudedItems) {
			execudedItems.set(execudedItems.indexOf(item), item - subtrackMinutes);
		}
		//
		provisioningService.doProvisioning(identity); // block
		//
		SysProvisioningBatchDto batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		assertNotNull(batch);
		//
		List<SysProvisioningOperationDto> content = provisioningOperationService.findByBatchId(batch.getId(), null).getContent();
		assertEquals(1, content.size());
		//
		SysProvisioningOperationDto sysProvisioningOperationDto = content.get(0);
		//
		assertEquals(ProvisioningEventType.UPDATE, sysProvisioningOperationDto.getOperationType());
		assertEquals(OperationState.BLOCKED, sysProvisioningOperationDto.getResult().getState());
	}
	
	@Test
	public void testClearCache() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		createProvisioningBreak(20l, 3, null, ProvisioningEventType.UPDATE,
				system.getId());
		//
		this.createAccount(system, identity);
		//
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		SysProvisioningBatchDto batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		assertNull(batch);
		//
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.blockUpdate();
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system); // block system
		//
		blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setUpdateOperation(Boolean.FALSE);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system); // unblock system, clear cache
		//
		provisioningService.doProvisioning(identity); 
		provisioningService.doProvisioning(identity);
		//
		batch = batchService.findBatch(system.getId(), identity.getId(), identity.getUsername());
		//
		assertNull(batch);
	}
	
	@Test
	public void testGlobalConfigurationSettings() {
		IdmIdentityDto recipient = testHelper.createIdentity();
		IdmIdentityDto recipient2 = testHelper.createIdentity();
		IdmRoleDto roleRecipient = testHelper.createRole();
		//
		createGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_DELETE_OPERATION, false, 2, 3, 20l, null, roleRecipient);
		String prefix = ProvisioningBreakConfiguration.GLOBAL_BREAK_PREFIX + ProvisioningBreakConfiguration.GLOBAL_BREAK_DELETE_OPERATION;
		configurationService.setValue(prefix + ProvisioningBreakConfiguration.PROPERTY_IDENTITY_RECIPIENTS, recipient.getUsername() + ",   " + recipient2.getUsername());
		//
		// check non existing configuration
		ProvisioningEventType eventType = ProvisioningEventType.UPDATE;
		Object value = provisioningBreakConfiguration.getDisabled(eventType);
		assertNull(value);
		value = provisioningBreakConfiguration.getDisableLimit(eventType);
		assertNull(value);
		List<IdmIdentityDto> recipients = provisioningBreakConfiguration.getIdentityRecipients(eventType);
		assertTrue(recipients.isEmpty());
		value = provisioningBreakConfiguration.getPeriod(eventType);
		assertNull(value);
		//
		// check existing
		eventType = ProvisioningEventType.DELETE;
		Boolean disabled = provisioningBreakConfiguration.getDisabled(eventType);
		Integer warningLimit = provisioningBreakConfiguration.getWarningLimit(eventType);
		Integer disableLimit = provisioningBreakConfiguration.getDisableLimit(eventType);
		Long period = provisioningBreakConfiguration.getPeriod(eventType);
		List<IdmIdentityDto> identityRecipients = provisioningBreakConfiguration.getIdentityRecipients(eventType);
		List<IdmRoleDto> roleRecipients = provisioningBreakConfiguration.getRoleRecipients(eventType);
		//
		assertEquals(Boolean.FALSE, disabled);
		assertEquals(Integer.valueOf(2), warningLimit);
		assertEquals(Integer.valueOf(3), disableLimit);
		assertEquals(Long.valueOf(20l), period);
		assertEquals(2, identityRecipients.size());
		assertEquals(1, roleRecipients.size());
		//
		IdmIdentityDto foundedRecipient = identityRecipients.stream().filter(rec -> rec.getUsername().equals(recipient.getUsername())).findFirst().get();
		assertNotNull(foundedRecipient);
		assertEquals(recipient.getId(), foundedRecipient.getId());
		//
		foundedRecipient = identityRecipients.stream().filter(rec -> rec.getUsername().equals(recipient2.getUsername())).findFirst().get();
		assertNotNull(foundedRecipient);
		assertEquals(recipient2.getId(), foundedRecipient.getId());
		//
		IdmRoleDto foundedRole = roleRecipients.get(0);
		assertNotNull(foundedRole);
		assertEquals(roleRecipient.getId(), foundedRole.getId());
	}
	
	@Test
	public void testGlobalConfigurationDisable() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto recipient = testHelper.createIdentity();
		//
		// create global configuration
		createGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_UPDATE_OPERATION, false, null, 3, 20l, recipient, null);
		//
		this.createAccount(system, identity);
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity); // block
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNotNull(system.getBlockedOperation());
		assertEquals(Boolean.TRUE, system.getBlockedOperation().getUpdateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getCreateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getDeleteOperation());
	}
	
	@Test
	public void testGlobalConfigurationDisabled() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto recipient = testHelper.createIdentity();
		//
		// create global configuration
		createGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_UPDATE_OPERATION, true, null, 2, 20l, recipient, null);
		//
		this.createAccount(system, identity);
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(0, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNull(system.getBlockedOperation());
	}
	
	@Test
	public void testGlobalConfigurationOverride() {
		SysSystemDto system = testHelper.createTestResourceSystem(true);
		IdmIdentityDto identity = testHelper.createIdentity();
		IdmIdentityDto recipient = testHelper.createIdentity();
		//
		// create global configuration
		createGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_UPDATE_OPERATION, false, null, 1, 20l, recipient, null);
		//
		SysProvisioningBreakConfigDto breakConfig = createProvisioningBreak(20l, 4, null, ProvisioningEventType.UPDATE, system.getId());
		IdmIdentityDto recipient2 = testHelper.createIdentity();
		createRecipient(breakConfig.getId(), recipient2.getId(), null);
		//
		this.createAccount(system, identity);
		//
		provisioningService.doProvisioning(identity); // create
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		provisioningService.doProvisioning(identity);
		//
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		List<IdmNotificationLogDto> content = notificationLogService.find(filter, null).getContent();
		assertEquals(0, content.size());
		//
		system = systemService.get(system.getId());
		assertNull(system.getBlockedOperation());
		//
		provisioningService.doProvisioning(identity); // block
		//
		filter = new IdmNotificationFilter();
		filter.setRecipient(recipient.getUsername());
		content = notificationLogService.find(filter, null).getContent();
		assertEquals(0, content.size());
		//
		filter = new IdmNotificationFilter();
		filter.setRecipient(recipient2.getUsername());
		content = notificationLogService.find(filter, null).getContent();
		assertEquals(2, content.size()); // two notification (notification + parent)
		//
		system = systemService.get(system.getId());
		assertNotNull(system.getBlockedOperation());
		assertEquals(Boolean.TRUE, system.getBlockedOperation().getUpdateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getCreateOperation());
		assertNotEquals(Boolean.TRUE, system.getBlockedOperation().getDeleteOperation());
	}
	
	/**
	 * Create global configuration for provisioning break defined in application properties
	 * 
	 * @param operation
	 * @param disable
	 * @param warningLimit
	 * @param disableLimit
	 * @param period
	 * @param recipient
	 * @param roleRecipient
	 */
	private void createGlobalConfiguration(String operation, Boolean disable, Integer warningLimit,
			Integer disableLimit, Long period, IdmIdentityDto recipient, IdmRoleDto roleRecipient) {
		//
		String prefix = ProvisioningBreakConfiguration.GLOBAL_BREAK_PREFIX
				+ operation;
		if (disable != null) {
			configurationService.setBooleanValue(prefix + ProvisioningBreakConfiguration.PROPERTY_DISABLED, disable);
		}
		if (warningLimit != null) {
			configurationService.setValue(prefix + ProvisioningBreakConfiguration.PROPERTY_WARNING_LIMIT,
					String.valueOf(warningLimit));
		}
		if (disableLimit != null) {
			configurationService.setValue(prefix + ProvisioningBreakConfiguration.PROPERTY_DISABLE_LIMIT,
					String.valueOf(disableLimit));
		}
		if (period != null) {
			configurationService.setValue(prefix + ProvisioningBreakConfiguration.PROPERTY_PERIOD, String.valueOf(period));
		}
		if (recipient != null) {
			configurationService.setValue(prefix + ProvisioningBreakConfiguration.PROPERTY_IDENTITY_RECIPIENTS,
					recipient.getUsername());
		}
		if (roleRecipient != null) {
			configurationService.setValue(prefix + ProvisioningBreakConfiguration.PROPERTY_ROLE_RECIPIENTS,
					roleRecipient.getCode());
		}
	}

	/**
	 * Method create recipient for provisioning break
	 * 
	 * @param breakConfigId
	 * @param identityId
	 * @param roleId
	 * @return
	 */
	private SysProvisioningBreakRecipientDto createRecipient(UUID breakConfigId, UUID identityId, UUID roleId) {
		SysProvisioningBreakRecipientDto breakRecipientDto = new SysProvisioningBreakRecipientDto();
		breakRecipientDto.setBreakConfig(breakConfigId);
		if (identityId != null) {
			breakRecipientDto.setIdentity(identityId);
		} else {
			breakRecipientDto.setRole(roleId);
		}
		//
		return provisioningBreakRecipient.save(breakRecipientDto);
	}

	/**
	 * Create provisioning break for given system with given attributes
	 * 
	 * @param period
	 * @param disableLimit
	 * @param warningLimit
	 * @param eventType
	 * @param systemId
	 * @return
	 */
	private SysProvisioningBreakConfigDto createProvisioningBreak(Long period, Integer disableLimit,
			Integer warningLimit, ProvisioningEventType eventType, UUID systemId) {
		SysProvisioningBreakConfigDto config = new SysProvisioningBreakConfigDto();
		config.setOperationType(eventType);
		config.setPeriod(period);
		config.setDisableLimit(disableLimit);
		config.setWarningLimit(warningLimit);
		config.setSystem(systemId);
		return provisioningBreakConfig.save(config);
	}

	/**
	 * Create {@link AccAccountDto} and {@link AccIdentityAccountDto} for system and identity
	 * 
	 * @param system
	 * @param identity
	 * @return
	 */
	private AccIdentityAccountDto createAccount(SysSystemDto system, IdmIdentityDto identity) {
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setUid(identity.getUsername());
		account.setAccountType(AccountType.PERSONAL);
		account = accountService.save(account);

		AccIdentityAccountDto accountIdentity = new AccIdentityAccountDto();
		accountIdentity.setIdentity(identity.getId());
		accountIdentity.setOwnership(true);
		accountIdentity.setAccount(account.getId());

		return identityAccoutnService.save(accountIdentity);
	}
	
	/**
	 * Clear all provisioning break and also clear global provisioning break configuration.
	 * Is necessary clear provisioning break for another test!
	 */
	private void clearProvisioningBreakConfiguration() {
		// remove specific configuration
		for (SysProvisioningBreakConfigDto config : provisioningBreakConfig.find(null)) {
			provisioningBreakConfig.delete(config);
		}
		//
		clearGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_DELETE_OPERATION);
		clearGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_UPDATE_OPERATION);
		clearGlobalConfiguration(ProvisioningBreakConfiguration.GLOBAL_BREAK_CREATE_OPERATION);
		
	}
	
	/**
	 * Method clear global provisioning break configuration for specific operation
	 * @param operation
	 */
	private void clearGlobalConfiguration(String operation) {
		String prefix = ProvisioningBreakConfiguration.GLOBAL_BREAK_PREFIX + operation;
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_DISABLE_LIMIT);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_DISABLED);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_IDENTITY_RECIPIENTS);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_PERIOD);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_ROLE_RECIPIENTS);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_TEMPLATE_DISABLE);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_TEMPLATE_WARNING);
		configurationService.deleteValue(prefix + ProvisioningBreakConfiguration.PROPERTY_WARNING_LIMIT);
	}
}