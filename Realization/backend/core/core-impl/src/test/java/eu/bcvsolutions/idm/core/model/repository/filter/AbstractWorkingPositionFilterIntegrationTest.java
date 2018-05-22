package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Prepare data for all tests with subordinates / managers.
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractWorkingPositionFilterIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private FormService formService;
	//
	protected IdmIdentityDto managerOne;
	protected IdmIdentityDto managerTwo;
	protected IdmIdentityDto invalidManager;
	protected IdmIdentityDto guaranteeThree;
	protected IdmIdentityDto guaranteeFour;
	protected IdmIdentityDto subordinateOne;
	protected IdmIdentityDto subordinateTwo;
	protected IdmIdentityDto subordinateThree;
	protected IdmTreeTypeDto structureOne;
	protected IdmTreeTypeDto structureTwo;
	protected IdmIdentityContractDto contractOne;
	protected IdmIdentityContractDto contractTwo;
	
	@Before
	public void init() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	/**
	 * prepare identities, tree structures and contracts
	 * suborinateOne has managerOne by tree structure - structureOne
	 * suborinateOne has managerTwo by tree structure - structureTwo
	 * suborinateOne has guaranteeThree by contractOne
	 * suborinateOne has guaranteeFour by contractTwo
	 * suborinateOne is manager for suborinateTwo
	 */
	protected void prepareData() {
		managerOne = getHelper().createIdentity((GuardedString) null);
		managerTwo = getHelper().createIdentity((GuardedString) null);
		invalidManager = getHelper().createIdentity((GuardedString) null);
		guaranteeThree = getHelper().createIdentity((GuardedString) null);
		guaranteeFour = getHelper().createIdentity((GuardedString) null);
		subordinateOne = getHelper().createIdentity((GuardedString) null);
		subordinateTwo = getHelper().createIdentity((GuardedString) null);
		subordinateThree = getHelper().createIdentity((GuardedString) null);
		//
		structureOne = getHelper().createTreeType();
		IdmTreeNodeDto managerOnePosition = getHelper().createTreeNode(structureOne, null); 
		getHelper().createIdentityContact(managerOne, managerOnePosition);
		getHelper().createIdentityContact(invalidManager, managerOnePosition, new LocalDate().plusDays(1), null);
		//
		structureTwo = getHelper().createTreeType();
		IdmTreeNodeDto managerTwoPosition = getHelper().createTreeNode(structureTwo, null); 
		getHelper().createIdentityContact(managerTwo, managerTwoPosition);
		// subordinate one
		IdmTreeNodeDto subordinateOnePositionOne = createPosition(structureOne, managerOnePosition);
		contractOne = getHelper().createIdentityContact(subordinateOne, subordinateOnePositionOne);
		getHelper().createContractGuarantee(contractOne.getId(), guaranteeThree.getId());
		IdmTreeNodeDto subordinateOnePositionTwo = createPosition(structureTwo, managerTwoPosition); 
		contractTwo = getHelper().createIdentityContact(subordinateOne, subordinateOnePositionTwo);
		getHelper().createContractGuarantee(contractTwo.getId(), guaranteeFour.getId());
		// subordinate two
		IdmTreeNodeDto subordinateTwoPosition = createPosition(structureOne, subordinateOnePositionOne);
		IdmIdentityContractDto contractSubordinateTwo = getHelper().createIdentityContact(subordinateTwo, subordinateTwoPosition);
		getHelper().createContractGuarantee(contractSubordinateTwo.getId(), guaranteeFour.getId());
		// subordinate three
		getHelper().createContractGuarantee(getHelper().createIdentityContact(subordinateThree).getId(), managerOne.getId());
	}
	
	protected void testManagersBuilder(FilterBuilder<IdmIdentity, IdmIdentityFilter> builder) {
		//
		// tests
		// all managers - for subordinate one
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setIncludeGuarantees(true);
		List<IdmIdentity> managers = builder.find(filter, null).getContent();
		assertTrue(contains(managers, managerOne));
		assertTrue(contains(managers, managerTwo));
		assertTrue(contains(managers, guaranteeThree));
		assertTrue(contains(managers, guaranteeFour));
		assertEquals(4, managers.size());
		//
		// find contract managers
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setManagersByContract(contractOne.getId());
		managers = builder.find(filter, null).getContent();
		assertEquals(2, managers.size());
		assertTrue(contains(managers, managerOne));
		assertTrue(contains(managers, guaranteeThree));
		//
		// find contract managers by without guarantees
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setManagersByContract(contractOne.getId());
		filter.setIncludeGuarantees(false);
		managers = builder.find(filter, null).getContent();
		assertEquals(1, managers.size());
		assertTrue(contains(managers, managerOne));
		//
		// manager by tree structures
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateOne.getId());
		filter.setManagersByTreeType(structureOne.getId());
		managers = builder.find(filter, null).getContent();
		assertEquals(1, managers.size());
		assertTrue(contains(managers, managerOne));
		filter.setManagersByTreeType(structureTwo.getId());
		managers = builder.find(filter, null).getContent();
		assertEquals(1, managers.size());
		assertTrue(contains(managers, managerTwo));
		//
		// all manager - for subordinate one
		filter = new IdmIdentityFilter();
		filter.setManagersFor(subordinateTwo.getId());
		managers = builder.find(filter, null).getContent();
		assertEquals(2, managers.size());
		assertTrue(contains(managers, subordinateOne));
		assertTrue(contains(managers, guaranteeFour));
	}
	
	protected void testSubordinatesBuilder(FilterBuilder<IdmIdentity, IdmIdentityFilter> builder) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(managerOne.getId());
		List<IdmIdentity> subordinates = builder.find(filter, null).getContent();
		assertEquals(2, subordinates.size());
		assertTrue(contains(subordinates, subordinateOne));
		assertTrue(contains(subordinates, subordinateThree));
		//
		filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(managerOne.getId());
		filter.setSubordinatesByTreeType(structureOne.getId());
		subordinates = builder.find(filter, null).getContent();
		assertEquals(1, subordinates.size());
		assertTrue(contains(subordinates, subordinateOne));
		//
		filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(guaranteeFour.getId());
		subordinates = builder.find(filter, null).getContent();
		assertEquals(2, subordinates.size());
		assertTrue(contains(subordinates, subordinateOne));
		assertTrue(contains(subordinates, subordinateTwo));
		//
		filter = new IdmIdentityFilter();
		filter.setSubordinatesFor(invalidManager.getId());
		subordinates = builder.find(filter, null).getContent();
		assertTrue(subordinates.isEmpty());
	}
	
	private IdmTreeNodeDto createPosition(IdmTreeTypeDto type, IdmTreeNodeDto parent) {
		IdmTreeNodeDto node = getHelper().createTreeNode(type, parent);
		
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmTreeNode.class, FormService.DEFAULT_DEFINITION_CODE);
		IdmFormAttributeDto attr = formDefinition.getMappedAttributeByCode(EavCodeSubordinatesFilter.DEFAULT_FORM_ATTRIBUTE_CODE);
		if (attr == null) {
			attr = new IdmFormAttributeDto();
			attr.setName("Parent code");
			attr.setCode(EavCodeSubordinatesFilter.DEFAULT_FORM_ATTRIBUTE_CODE);
			attr.setFormDefinition(formDefinition.getId());
			attr.setPersistentType(PersistentType.TEXT);
			attr.setUnmodifiable(true);
			attr = formService.saveAttribute(attr);
		}
		//
		formService.saveValues(node, attr, Lists.newArrayList(parent.getCode()));
		//
		return node;
	}
	
	private boolean contains(List<IdmIdentity> managers, IdmIdentityDto manager) {
		return managers
				.stream()
				.filter(m -> { 
					return m.getId().equals(manager.getId()); 
					})
				.count() == 1;
	}

}
