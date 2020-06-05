package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AddNewAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Integration tests with identity contracts:
 * - work with assigned roles, when contract is changed (disable, etc.)
 * - automatic role is defined, changed
 * - expiration
 * - evaluate state
 * - filter - TODO: move to rest test
 * 
 * TODO: @Transactional - automatic roles are recount after original transaction ends (LRT)
 * 
 * @author Radek Tomiška
 * @author Marek Klement
 *
 */
public class DefaultIdmIdentityContractServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private LongRunningTaskManager taskManager;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmTreeNodeService treeNodeService;
	@Autowired private LookupService lookupService;
	//
	private DefaultIdmIdentityContractService service;
	private IdmTreeTypeDto treeType = null;
	private IdmTreeNodeDto nodeA = null;
	private IdmTreeNodeDto nodeB = null;
	private IdmTreeNodeDto nodeC = null;
	private IdmTreeNodeDto nodeD = null;
	private IdmTreeNodeDto nodeE = null;
	private IdmTreeNodeDto nodeF = null;
	private IdmRoleDto roleA = null;
	private IdmRoleDto roleB = null;
	private IdmRoleDto roleC = null;
	private IdmRoleDto roleD = null;
	private IdmRoleTreeNodeDto automaticRoleA = null;
	private IdmRoleTreeNodeDto automaticRoleD = null;
	private IdmRoleTreeNodeDto automaticRoleE = null;
	private IdmRoleTreeNodeDto automaticRoleF = null;
	
	@Before
	public void init() {
		service = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityContractService.class);
		prepareTreeStructureAndRoles();
	}
	
	@After 
	public void after() {
		// delete this test automatic roles only	
		if(automaticRoleA != null) try { deleteAutomaticRole(automaticRoleA); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleD != null) try { deleteAutomaticRole(automaticRoleD); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleE != null) try { deleteAutomaticRole(automaticRoleE); } catch (EmptyResultDataAccessException ex) {} ;
		if(automaticRoleF != null) try { deleteAutomaticRole(automaticRoleF); } catch (EmptyResultDataAccessException ex) {} ;
	}	
	
	private void prepareTreeStructureAndRoles() {
		// tree type
		treeType = getHelper().createTreeType();
		// four levels
		// https://proj.bcvsolutions.eu/ngidm/doku.php?id=roztridit:standardni_procesy#role_pridelovane_na_zaklade_zarazeni_v_organizacni_strukture
		nodeA = getHelper().createTreeNode(treeType, null);
		nodeB = getHelper().createTreeNode(treeType, nodeA);
		nodeC = getHelper().createTreeNode(treeType, nodeB);
		nodeD = getHelper().createTreeNode(treeType, nodeB);
		nodeE = getHelper().createTreeNode(treeType, nodeD);
		nodeF = getHelper().createTreeNode(treeType, nodeD);
		// create roles
		roleA = getHelper().createRole();
		roleB = getHelper().createRole();
		roleC = getHelper().createRole();
		roleD = getHelper().createRole();
	}
	
	/**
	 * Save automatic role with repository and manual create and wait for task
	 * @param automaticRole
	 * @return
	 */
	private IdmRoleTreeNodeDto saveAutomaticRole(IdmRoleTreeNodeDto automaticRole, boolean withLongRunningTask) {
		automaticRole.setName("default"); // default name
		IdmRoleTreeNodeDto roleTreeNode = roleTreeNodeService.saveInternal(automaticRole);
		//
		if (withLongRunningTask) {
			AddNewAutomaticRoleTaskExecutor task = new AddNewAutomaticRoleTaskExecutor();
			task.setAutomaticRoleId(roleTreeNode.getId());
			taskManager.executeSync(task);
		}
		//
		return roleTreeNodeService.get(roleTreeNode.getId());
	}
	
	private void deleteAutomaticRole(IdmRoleTreeNodeDto automaticRole) {
		RemoveAutomaticRoleTaskExecutor task = new RemoveAutomaticRoleTaskExecutor();
		task.setAutomaticRoleId(automaticRole.getId());
		task.setContinueOnException(true);
		task.setRequireNewTransaction(true);
		taskManager.executeSync(task);
	}
	
	/**
	 * 
	 */
	private void prepareAutomaticRoles() {
		// prepare automatic roles
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.DOWN);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeA.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);	
		
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.DOWN);
		automaticRoleD.setRole(roleB.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, false);
		
		automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleC.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = saveAutomaticRole(automaticRoleF, false);
		
		automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleD.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = saveAutomaticRole(automaticRoleE, false);
	}
	
	@Test
	public void testFindAutomaticRoleWithoutRecursion() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.NO);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);
		//
		// test
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeD.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeB.getId()).isEmpty());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeF.getId()).isEmpty());
	}
	
	@Test
	public void testFindAutomaticRoleWithRecursionDown() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.DOWN);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);
		//
		// test
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeD.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeB.getId()).isEmpty());
		automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeF.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
	}
	
	@Test
	public void testFindAutomaticRoleWithRecursionUp() {
		// prepare
		automaticRoleA = new IdmRoleTreeNodeDto();
		automaticRoleA.setRecursionType(RecursionType.UP);
		automaticRoleA.setRole(roleA.getId());
		automaticRoleA.setTreeNode(nodeD.getId());
		automaticRoleA = saveAutomaticRole(automaticRoleA, false);
		//
		// test
		Set<IdmRoleTreeNodeDto> automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeD.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
		assertTrue(roleTreeNodeService.getAutomaticRolesByTreeNode(nodeF.getId()).isEmpty());
		automaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(nodeB.getId());
		assertEquals(1, automaticRoles.size());
		assertEquals(roleA.getId(), automaticRoles.iterator().next().getRole());
	}	
	
	@Test
	public void testCRUDContractWithAutomaticRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contractToCreate = service.getPrimeContract(identity.getId());
		contractToCreate.setIdentity(identity.getId());
		contractToCreate.setValidFrom(new LocalDate().minusDays(1));
		contractToCreate.setValidTill(new LocalDate().plusMonths(1));
		contractToCreate.setWorkPosition(nodeD.getId());
		contractToCreate.setMain(true);
		contractToCreate.setDescription("test-node-d");
		service.save(contractToCreate);
		//
		IdmIdentityContractDto contract = service.getPrimeContract(identity.getId());
		//
		// test after create
		Assert.assertEquals(nodeD.getId(), contract.getWorkPosition());
		Assert.assertEquals("test-node-d", contract.getDescription());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(3, identityRoles.size());
		Assert.assertTrue(identityRoles.stream().allMatch(ir -> contract.getValidFrom().equals(ir.getValidFrom())));
		Assert.assertTrue(identityRoles.stream().allMatch(ir -> contract.getValidTill().equals(ir.getValidTill())));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleA.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleB.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleC.getId().equals(ir.getRole());
		}));
		//
		identityRoles = identityRoleService.findAllByIdentity(identity.getId());
		assertEquals(3, identityRoles.size());
		//
		// test after delete
		service.delete(contract);
		assertTrue(identityRoleService.findAllByIdentity(identity.getId()).isEmpty());
	}
	
	@Test
	public void testCRUDContractWithAutomaticRolesAsync() {
		prepareAutomaticRoles();
		//
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			TransactionContextHolder.setContext(TransactionContextHolder.createEmptyContext());
			UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
			//
			// prepare identity and contract
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			IdmIdentityContractDto contractToCreate = service.getPrimeContract(identity.getId());
			contractToCreate.setIdentity(identity.getId());
			contractToCreate.setValidFrom(new LocalDate().minusDays(1));
			contractToCreate.setValidTill(new LocalDate().plusMonths(1));
			contractToCreate.setWorkPosition(nodeD.getId());
			contractToCreate.setMain(true);
			contractToCreate.setDescription("test-node-d");
			service.save(contractToCreate);
			//
			IdmIdentityContractDto contract = service.getPrimeContract(identity.getId());
			//
			// test after create
			Assert.assertEquals(nodeD.getId(), contract.getWorkPosition());
			Assert.assertEquals("test-node-d", contract.getDescription());
			Assert.assertEquals(transactionId, contract.getTransactionId());
			//
			getHelper().waitForResult(res -> {
				return identityRoleService.findAllByContract(contract.getId()).isEmpty();
			}, 500, Integer.MAX_VALUE);
			//
			List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
			Assert.assertEquals(3, identityRoles.size());
			Assert.assertTrue(identityRoles.stream().allMatch(ir -> contract.getValidFrom().equals(ir.getValidFrom())));
			Assert.assertTrue(identityRoles.stream().allMatch(ir -> contract.getValidTill().equals(ir.getValidTill())));
			Assert.assertTrue(identityRoles.stream().allMatch(ir -> ir.getTransactionId().equals(transactionId)));
			Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
				return roleA.getId().equals(ir.getRole());
			}));
			Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
				return roleB.getId().equals(ir.getRole());
			}));
			Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
				return roleC.getId().equals(ir.getRole());
			}));
			//
			// find by transactionId
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setTransactionId(transactionId);
			identityRoles = identityRoleService.find(filter, null).getContent();
			Assert.assertEquals(3, identityRoles.size());
			Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
				return roleA.getId().equals(ir.getRole());
			}));
			Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
				return roleB.getId().equals(ir.getRole());
			}));
			Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
				return roleC.getId().equals(ir.getRole());
			}));
			//
			service.delete(contract);
		} finally { 
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}
	
	@Test
	public void testChangeContractValidityWithAssignedRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract = service.save(contract);
		//
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		};
		// test after change
		contract.setValidFrom(new LocalDate().minusDays(2));
		contract.setValidTill(new LocalDate().plusMonths(4));
		contract = service.save(contract);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		}
	}
	
	@Test
	public void testChangeContractPositionWithAutomaticRolesAssigned() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeD.getId());
		contract = service.save(contract);
		// 
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleA.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleB.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleC.getId().equals(ir.getRole());
		}));
		//
		contract.setWorkPosition(nodeE.getId());
		contract = service.save(contract);
		//
		// test after change
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleA.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleB.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleD.getId().equals(ir.getRole());
		}));
	}
	
	@Test
	public void testDeleteAutomaticRoleWithContractAlreadyExists() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeC.getId());
		contract = service.save(contract);
		//
		assertEquals(1, identityRoleService.findAllByContract(contract.getId()).size());
		//
		deleteAutomaticRole(automaticRoleD);
		assertEquals(1, identityRoleService.findAllByContract(contract.getId()).size());
		//
		deleteAutomaticRole(automaticRoleA);
		assertTrue(identityRoleService.findAllByContract(contract.getId()).isEmpty());
	}
	
	@Test
	public void testDontRemoveSameRole() {
		automaticRoleF = new IdmRoleTreeNodeDto();
		automaticRoleF.setRecursionType(RecursionType.UP);
		automaticRoleF.setRole(roleA.getId());
		automaticRoleF.setTreeNode(nodeF.getId());
		automaticRoleF = saveAutomaticRole(automaticRoleF, false);
		//
		automaticRoleE = new IdmRoleTreeNodeDto();
		automaticRoleE.setRecursionType(RecursionType.NO);
		automaticRoleE.setRole(roleA.getId());
		automaticRoleE.setTreeNode(nodeE.getId());
		automaticRoleE = saveAutomaticRole(automaticRoleE, false);
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(nodeF.getId());
		contract = service.save(contract);
		//
		// check assigned role after creation 
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(roleA.getId(), identityRoles.get(0).getRole());
		assertEquals(automaticRoleF.getId(), identityRoles.get(0).getAutomaticRole());
		//
		UUID id = identityRoles.get(0).getId();
		//
		// change
		contract.setWorkPosition(nodeE.getId());
		contract = service.save(contract);
		//
		// check assigned role after creation 
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(roleA.getId(), identityRoles.get(0).getRole());
		assertEquals(automaticRoleE.getId(), identityRoles.get(0).getAutomaticRole());
		assertEquals(id, identityRoles.get(0).getId());
	}
	
	@Test
	public void testAssingRoleByNewAutomaticRoleForExistingContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition("new position");
		contract = service.save(contract);
		//
		IdmIdentityContractDto contractF = new IdmIdentityContractDto();
		contractF.setIdentity(identity.getId());
		contractF.setWorkPosition(nodeF.getId());
		contractF = service.save(contractF);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD = service.save(contractD);
		//
		IdmIdentityContractDto contractB = new IdmIdentityContractDto();
		contractB.setIdentity(identity.getId());
		contractB.setWorkPosition(nodeB.getId());
		contractB = service.save(contractB);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.DOWN);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractB.getId());
		assertTrue(identityRoles.isEmpty());
		//
		identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getAutomaticRole());
		//
		identityRoles = identityRoleService.findAllByContract(contractF.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getAutomaticRole());
	}
	
	@Test
	public void testAssingRoleForContractValidInTheFuture() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD.setValidFrom(new LocalDate().plusDays(1));
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(1, identityRoles.size());
		assertEquals(automaticRoleD.getId(), identityRoles.get(0).getAutomaticRole());
	}
	
	@Test
	public void testDontAssingRoleForContractValidInThePast() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD.setValidTill(new LocalDate().minusDays(1));
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDontAssingRoleForDisabledContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setWorkPosition(nodeD.getId());
		contractD.setState(ContractState.DISABLED);
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDisableContractWithAssignedRoles() {
		prepareAutomaticRoles();
		//
		// prepare identity and contract
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract = service.save(contract);
		//
		// test after create
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
		for(IdmIdentityRoleDto identityRole : identityRoles) {
			assertEquals(contract.getValidFrom(), identityRole.getValidFrom());
			assertEquals(contract.getValidTill(), identityRole.getValidTill());
		};
		// test after change
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertTrue(identityRoles.isEmpty());
		// enable again
		contract.setState(null);
		contract = service.save(contract);
		identityRoles = identityRoleService.findAllByContract(contract.getId());
		assertEquals(3, identityRoles.size());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityOnRole() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = getHelper().createRoleTreeNode(role, treeNode, false);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		getHelper().deleteRole(role.getId());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityOnTreeNode() {
		// prepare data
		IdmRoleDto role = getHelper().createRole();
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		// automatic role
		IdmRoleTreeNodeDto roleTreeNode = getHelper().createRoleTreeNode(role, treeNode, false);
		//
		assertNotNull(roleTreeNode.getId());
		assertEquals(roleTreeNode.getId(), roleTreeNodeService.get(roleTreeNode.getId()).getId());
		//
		getHelper().deleteTreeNode(treeNode.getId());
	}
	
	@Test
	public void testReferentialIntegrityOnIdentityDelete() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityWithContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identityWithContract);
		getHelper().createContractGuarantee(contract.getId(), identity.getId());
		//
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contract.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		getHelper().deleteIdentity(identity.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(0, guarantees.size());
	}
	
	@Test
	public void testReferentialIntegrityOnContractDelete() {
		// prepare data
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityWithContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identityWithContract);
		getHelper().createContractGuarantee(contract.getId(), identity.getId());
		getHelper().createContractPosition(contract);
		//
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setGuaranteeId(identity.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		//
		IdmContractPositionFilter positionFilter = new IdmContractPositionFilter();
		positionFilter.setIdentityContractId(contract.getId());
		List<IdmContractPositionDto> positions = contractPositionService.find(positionFilter, null).getContent();
		assertEquals(1, positions.size());
		//
		getHelper().deleteIdentityContact(contract.getId());
		//
		guarantees = contractGuaranteeService.find(filter, null).getContent();
		Assert.assertTrue(guarantees.isEmpty());
		positions = contractPositionService.find(positionFilter, null).getContent();
		Assert.assertTrue(positions.isEmpty());
	}
	
	@Test
	public void testDontCreateContractByConfiguration() {
		configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, false);
		//
		try {
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			assertNull(service.getPrimeContract(identity.getId()));
		} finally {
			configurationService.setBooleanValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT, true);
		}		
	}
	
	@Test
	public void testSetStateByDateValidInFuture() {
		IdmIdentityContractDto contract = getHelper().createIdentityContact(getHelper().createIdentity((GuardedString) null), null, new LocalDate().plusDays(1), null);
		//
		Assert.assertNull(contract.getState());
		Assert.assertFalse(((IdmIdentityContract) lookupService.lookupEntity(IdmIdentityContractDto.class, contract.getId())).isDisabled());
	}
	
	@Test
	public void testSetStateByDateValidInPast() {
		IdmIdentityContractDto contract = getHelper().createIdentityContact(getHelper().createIdentity((GuardedString) null), null, new LocalDate().plusDays(1), new LocalDate().minusDays(1));
		//
		Assert.assertNull(contract.getState());
		Assert.assertFalse(((IdmIdentityContract) lookupService.lookupEntity(IdmIdentityContractDto.class, contract.getId())).isDisabled());
	}
	
	@Test
	public void textFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		node.setName("Position105");
		treeNodeService.save(node);

		IdmTreeNodeDto node2 = getHelper().createTreeNode();
		node2.setName("Position006");
		treeNodeService.save(node2);

		IdmTreeNodeDto node3 = getHelper().createTreeNode();
		node3.setCode("Position007");
		treeNodeService.save(node3);

		IdmTreeNodeDto node4 = getHelper().createTreeNode();
		node4.setCode("Position108");
		treeNodeService.save(node4);

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2);
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity3,node3);
		IdmIdentityContractDto contract4 = getHelper().createIdentityContact(identity4,node4);

		contract.setPosition("Position001");
		contract = service.save(contract);

		contract2.setPosition("Position102");
		service.save(contract2);

		contract3.setPosition("Position103");
		service.save(contract3);

		contract4.setPosition("Position104");
		service.save(contract4);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setText("Position00");
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertEquals("Wrong Text",3,result.getTotalElements());
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
		assertTrue(result.getContent().contains(contract3));
	}

	@Test
	public void identityFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity,node2);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity.getId());
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertEquals("Wrong Identity",3,result.getTotalElements());
		assertTrue(result.getContent().contains(service.getPrimeContract(identity.getId())));
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
	}

	@Test
	public void datesValidFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();
		IdmTreeNodeDto node3 = getHelper().createTreeNode();
		IdmTreeNodeDto node4 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node, LocalDate.now(), LocalDate.now().plusDays(2));
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2, LocalDate.now(), LocalDate.now().plusDays(2));
		IdmIdentityContractDto contract3 = getHelper().createIdentityContact(identity3,node3, LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
		IdmIdentityContractDto contract4 = getHelper().createIdentityContact(identity4,node4, LocalDate.now().minusDays(2), LocalDate.now().plusDays(2));

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setValidFrom(contract.getValidFrom());
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));

		filter.setValidFrom(null);
		filter.setValidTill(contract2.getValidTill());
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));

		filter.setValidTill(null);
		filter.setValid(true);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract3));

		filter.setValid(null);
		filter.setValidNowOrInFuture(true);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract4));

		filter.setValidNowOrInFuture(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract3));
	}

	@Test
	public void externeFilterTest(){
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2);

		contract.setExterne(true);
		service.save(contract);

		contract2.setExterne(false);
		service.save(contract2);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setExterne(true);
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertFalse(result.getContent().contains(contract2));

		filter.setExterne(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract));
	}

	@Test
	public void mainFilterTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = getHelper().createIdentity((GuardedString) null);

		IdmTreeNodeDto node = getHelper().createTreeNode();
		IdmTreeNodeDto node2 = getHelper().createTreeNode();

		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity,node);
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identity2,node2);

		contract.setMain(true);
		service.save(contract);

		contract2.setMain(false);
		service.save(contract2);

		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setMain(true);
		Page<IdmIdentityContractDto> result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract));
		assertFalse(result.getContent().contains(contract2));

		filter.setMain(false);
		result = service.find(filter,null);
		assertTrue(result.getContent().contains(contract2));
		assertFalse(result.getContent().contains(contract));
	}
	
	@Test
	public void positionFilterTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setPosition(getHelper().createName());
		contract = getHelper().getService(IdmIdentityContractService.class).save(contract);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setPosition(contract.getPosition());
		Page<IdmIdentityContractDto> results = service.find(filter, null);
		Assert.assertTrue(results.getContent().contains(contract));
		Assert.assertEquals(1, results.getTotalElements());
	}
	
	@Test
	public void workPositionFilterTest() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmTreeNodeDto treeNode = getHelper().createTreeNode();
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identity.getId());
		contract.setWorkPosition(treeNode.getId());
		contract = getHelper().getService(IdmIdentityContractService.class).save(contract);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setWorkPosition(treeNode.getId());
		Page<IdmIdentityContractDto> results = service.find(filter, null);
		Assert.assertTrue(results.getContent().contains(contract));
		Assert.assertEquals(1, results.getTotalElements());
	}
	
	@Test
	public void testFindAllValidContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		List<IdmIdentityContractDto> contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), false);
		Assert.assertEquals(1, contracts.size());
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		//
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), false);
		Assert.assertEquals(0, contracts.size());
		//
		// invalid
		getHelper().createIdentityContact(identity, null, LocalDate.now().plusDays(1), null);
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), false);
		Assert.assertEquals(0, contracts.size());
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now().plusDays(1), false);
		Assert.assertEquals(1, contracts.size());
		//
		// externe
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), true);
		Assert.assertEquals(0, contracts.size());
		contract.setExterne(true);
		contract.setState(ContractState.EXCLUDED);
		contract = service.save(contract);
		contracts = service.findAllValidForDate(identity.getId(), LocalDate.now(), true);
		Assert.assertEquals(1, contracts.size());
	}
	
	@Test
	public void testFindExcludedContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		contract = service.save(contract);
		IdmIdentityContractDto disabled = getHelper().createIdentityContact(identity);
		disabled.setState(ContractState.DISABLED);
		contract = service.save(contract);
		IdmIdentityContractDto valid = getHelper().createIdentityContact(identity);
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setIdentity(identity.getId());
		filter.setExcluded(Boolean.TRUE);
		List<IdmIdentityContractDto> contracts = service.find(filter, null).getContent();
		//
		Assert.assertEquals(1, contracts.size());
		Assert.assertEquals(contract.getId(), contracts.get(0).getId());
		//
		filter.setExcluded(Boolean.FALSE);
		contracts = service.find(filter, null).getContent();
		Assert.assertEquals(2, contracts.size());
		Assert.assertTrue(contracts.stream().anyMatch(c -> c.getId().equals(disabled.getId())));
		Assert.assertTrue(contracts.stream().anyMatch(c -> c.getId().equals(valid.getId())));
	}
	
	@Test
	public void testDisableIdentityAfterExcludeContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		Assert.assertFalse(identity.isDisabled());
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityMoreContracts() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		getHelper().createIdentityContact(identity);
		Assert.assertFalse(identity.isDisabled());
		//
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityAfterIncludeContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.EXCLUDED);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		contract.setState(null);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityAfterEnableContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		contract.setState(null);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterDisableContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setState(ContractState.DISABLED);
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterInvalidateContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setValidFrom(LocalDate.now().plusDays(1));
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testEnableIdentityAfterValidateContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setValidFrom(LocalDate.now().plusDays(1));
		contract = service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		contract.setValidFrom(LocalDate.now());
		service.save(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterDeleteContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		service.delete(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());	
	}
	
	@Test
	public void testEnableIdentityAfterCreateValidContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		service.delete(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		getHelper().createIdentityContact(identity);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertFalse(identity.isDisabled());
	}
	
	@Test
	public void testDisableIdentityAfterCreateInvalidContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		service.delete(contract);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
		//
		getHelper().createIdentityContact(identity, null, LocalDate.now().plusDays(1), null);
		//
		identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, identity.getId());
		Assert.assertTrue(identity.isDisabled());
	}
	
	@Test
	public void testDontAssingRoleForDisabledContractWhenPositionIsChanged() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setState(ContractState.DISABLED);
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		contractD.setWorkPosition(nodeD.getId());
		contractD = service.save(contractD);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testDontAssingRoleForInvalidContractWhenPositionIsChanged() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		IdmIdentityContractDto contractD = new IdmIdentityContractDto();
		contractD.setIdentity(identity.getId());
		contractD.setValidTill(LocalDate.now().minusDays(1));
		contractD = service.save(contractD);
		//
		// create new automatic role
		automaticRoleD = new IdmRoleTreeNodeDto();
		automaticRoleD.setRecursionType(RecursionType.NO);
		automaticRoleD.setRole(roleA.getId());
		automaticRoleD.setTreeNode(nodeD.getId());
		automaticRoleD = saveAutomaticRole(automaticRoleD, true);
		//
		contractD.setWorkPosition(nodeD.getId());
		contractD = service.save(contractD);
		//
		// check
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contractD.getId());
		assertEquals(0, identityRoles.size());
	}
	
	@Test
	public void testFindLastExpiredContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto last = service.findLastExpiredContract(identity.getId(), null);
		Assert.assertNull(last);
		//
		IdmIdentityContractDto contractOne = new IdmIdentityContractDto();
		contractOne.setIdentity(identity.getId());
		contractOne.setValidTill(LocalDate.now().minusDays(1));
		contractOne = service.save(contractOne);
		//
		IdmIdentityContractDto contractTwo = new IdmIdentityContractDto();
		contractTwo.setIdentity(identity.getId());
		contractTwo.setValidTill(LocalDate.now().minusDays(2));
		contractTwo = service.save(contractTwo);
		//
		last = service.findLastExpiredContract(identity.getId(), contractTwo.getValidTill());
		Assert.assertNull(last);
		//
		last = service.findLastExpiredContract(identity.getId(), null);
		Assert.assertEquals(contractOne, last);
		//
		last = service.findLastExpiredContract(identity.getId(), contractOne.getValidTill());
		Assert.assertEquals(contractTwo, last);
	}

	@Test
	public void testCheckExpiredContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);

		List<IdmIdentityContractDto> contracts = service.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());

		IdmIdentityContractDto last = service.findLastExpiredContract(identity.getId(), null);
		Assert.assertNull(last);

		IdmIdentityContractDto identityContractTwo = getHelper().createIdentityContact(identity);

		last = service.findLastExpiredContract(identity.getId(), null);
		Assert.assertNull(last);

		contracts = service.findAllByIdentity(identity.getId());
		assertEquals(2, contracts.size());

		identityContractTwo.setValidTill(LocalDate.now().minusDays(5));
		identityContractTwo = service.save(identityContractTwo);

		contracts = service.findAllByIdentity(identity.getId());
		assertEquals(2, contracts.size());

		last = service.findLastExpiredContract(identity.getId(), null);
		Assert.assertNotNull(last);
		assertEquals(identityContractTwo.getId(), last.getId());

		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		contracts = service.findAllByIdentity(identityTwo.getId());
		assertEquals(1, contracts.size());
		last = service.findLastExpiredContract(identityTwo.getId(), null);
		Assert.assertNull(last);
	}
	
	@Test
	@Transactional
	public void testOtherMainContractByValidFrom() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contractOne = getHelper().getPrimeContract(identity);
		contractOne.setWorkPosition(null);
		contractOne.setMain(false);
		contractOne.setValidFrom(new LocalDate().minusDays(2));
		service.save(contractOne);
		IdmIdentityContractDto contractTwo = getHelper().createIdentityContact(identity, null, new LocalDate().minusDays(1), null);
		//
		Assert.assertEquals(contractOne.getId(), getHelper().getPrimeContract(identity).getId());
		//
		contractTwo.setValidFrom(new LocalDate().minusDays(3));
		service.save(contractTwo);
		//
		Assert.assertEquals(contractTwo.getId(), getHelper().getPrimeContract(identity).getId());
	}
	
	@Test
	public void testAutomaticRolesRemovalAfterContractEnds() {
		// automatic roles by tree structure
		prepareAutomaticRoles();
		// automatic role by attribute on contract
		String autoPosition = getHelper().createName();
		IdmRoleDto autoAttributeRole = getHelper().createRole();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(autoAttributeRole.getId());
		getHelper().createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.CONTRACT, IdmIdentityContract_.position.getName(), null, autoPosition);
		//
		// prepare identity, contract, direct roles and automatic roles
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = service.getPrimeContract(identity.getId());
		contract.setIdentity(identity.getId());
		contract.setValidFrom(new LocalDate().minusDays(1));
		contract.setValidTill(new LocalDate().plusMonths(1));
		contract.setWorkPosition(nodeD.getId());
		contract.setMain(true);
		contract.setDescription("test-node-d");
		contract.setPosition(autoPosition);
		contract = service.save(contract);
		UUID contractId = contract.getId();
		IdmRoleDto directRole = getHelper().createRole();
		getHelper().createIdentityRole(contract, directRole);
		//
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByContract(contract.getId());
		Assert.assertEquals(5, identityRoles.size());
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleA.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleB.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return roleC.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return directRole.getId().equals(ir.getRole());
		}));
		Assert.assertTrue(identityRoles.stream().anyMatch(ir -> {
			return autoAttributeRole.getId().equals(ir.getRole());
		}));
		//
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			//
			// end contract - all roles should be removed, after asynchronous role request ends
			contract.setValidTill(LocalDate.now().minusDays(1));
			contract = service.save(contract);
			//
			Assert.assertFalse(contract.isValidNowOrInFuture());
			//
			getHelper().waitForResult(res -> {
				return !identityRoleService.findAllByContract(contractId).isEmpty();
			}, 300, Integer.MAX_VALUE);
			//
			identityRoles = identityRoleService.findAllByContract(contract.getId());
			Assert.assertTrue(identityRoles.isEmpty());
			//
			service.delete(contract);
		} finally { 
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}
}
