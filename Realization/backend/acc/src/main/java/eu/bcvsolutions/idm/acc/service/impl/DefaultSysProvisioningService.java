package eu.bcvsolutions.idm.acc.service.impl;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmProvisioningService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfConnectorObject;
import eu.bcvsolutions.idm.icf.api.IcfObjectClass;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.impl.IcfAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfConnectorObjectImpl;
import eu.bcvsolutions.idm.icf.impl.IcfObjectClassImpl;
import eu.bcvsolutions.idm.icf.impl.IcfPasswordAttributeImpl;
import eu.bcvsolutions.idm.icf.impl.IcfUidAttributeImpl;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorFacade;
import eu.bcvsolutions.idm.security.domain.GuardedString;

/**
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysProvisioningService implements IdmProvisioningService, SysProvisioningService {

	private static final String PASSWORD_IDM_PROPERTY_NAME = "password";
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningService.class);
	private SysSystemEntityHandlingService entityHandlingService;
	private SysSchemaAttributeHandlingService attributeHandlingService;
	private IcfConnectorFacade connectorFacade;
	private SysSystemService systemService;
	private AccIdentityAccountService identityAccoutnService;

	@Autowired
	public DefaultSysProvisioningService(SysSystemEntityHandlingService entityHandlingService,
			SysSchemaAttributeHandlingService attributeHandlingService, IcfConnectorFacade connectorFacade,
			SysSystemService systemService, AccIdentityAccountService identityAccoutnService) {
		super();
		this.entityHandlingService = entityHandlingService;
		this.attributeHandlingService = attributeHandlingService;
		this.connectorFacade = connectorFacade;
		this.systemService = systemService;
		this.identityAccoutnService = identityAccoutnService;
	}

	@Override
	public void doIdentityProvisioning(IdmIdentity identity) {
		Assert.notNull(identity);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);
		Page<AccIdentityAccount> identityAccounts = identityAccoutnService.find(filter, null);
		List<AccIdentityAccount> idenittyAccoutnList = identityAccounts.getContent();
		if (idenittyAccoutnList == null) {
			return;
		}
		idenittyAccoutnList.stream().filter(identityAccount -> {
			return identityAccount.isOwnership();
		}).forEach((identityAccount) -> {
			doOperation(identityAccount.getAccount().getUid(), identity, AccountOperationType.UPDATE,
					SystemOperationType.PROVISIONING, SystemEntityType.IDENTITY,
					identityAccount.getAccount().getSystem());
		});
	}

	@Override
	public void deleteAccount(AccAccount account) {
		Assert.notNull(account);
		doOperation(account.getUid(), null, AccountOperationType.DELETE, SystemOperationType.PROVISIONING,
				SystemEntityType.IDENTITY, account.getSystem());
	}

	/**
	 * Do provisioning/synchronisation/reconciliation for given identity account
	 * 
	 * @param identityAccount
	 * @param operation
	 */
	public void doAccountOperation(AccIdentityAccount identityAccount, SystemOperationType operation) {
		Assert.notNull(operation);
		Assert.notNull(identityAccount);

		doOperation(identityAccount.getAccount().getUid(), identityAccount.getIdentity(), AccountOperationType.UPDATE,
				operation, SystemEntityType.IDENTITY, identityAccount.getAccount().getSystem());
	}
	
	
	@Override
	public void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange){
		Assert.notNull(identity);
		Assert.notNull(passwordChange);
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentity(identity);
		Page<AccIdentityAccount> identityAccounts = identityAccoutnService.find(filter, null);
		List<AccIdentityAccount> idenittyAccoutnList = identityAccounts.getContent();
		if (idenittyAccoutnList == null) {
			return;
		}
		GuardedString guardedPassword = new GuardedString(passwordChange.getNewPassword());

		// TODO: ? add into IdentityAccountFilter: accountId IN (..., ...);
		idenittyAccoutnList.stream().filter(identityAccount -> {
			return passwordChange.getResources().contains(identityAccount.getId().toString());
		}).forEach(identityAccount -> {
			doProvisioningForAttribute(identityAccount.getAccount().getUid(), PASSWORD_IDM_PROPERTY_NAME, guardedPassword,
					identityAccount.getAccount().getSystem(), AccountOperationType.UPDATE, SystemEntityType.IDENTITY);
		});
	}
	
	@Override
	public void doProvisioningForAttribute(String uid, String idmPropertyName, Object value, SysSystem system, 
			AccountOperationType operationType, SystemEntityType entityType){
		
		Assert.notNull(uid);
		Assert.notNull(system);
		Assert.notNull(entityType);

		List<SysSchemaAttributeHandling> attributes = findAttributesHandling(SystemOperationType.PROVISIONING, entityType, system);
		if (attributes == null || attributes.isEmpty()) {
			return;
		}

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		IcfUidAttribute uidAttribute = new IcfUidAttributeImpl(null, uid, null);

		Optional<SysSchemaAttributeHandling> attriubuteHandlingOptional = attributes.stream().filter((attribute) -> {return idmPropertyName.equals(attribute.getIdmPropertyName());}).findFirst();
		if(!attriubuteHandlingOptional.isPresent()){
			throw new ResultCodeException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("property", idmPropertyName, "uid", uid));
		}
		SysSchemaAttributeHandling attributeHandling = attriubuteHandlingOptional.get();
		if(!attributeHandling.getSchemaAttribute().isUpdateable()){
			throw new ResultCodeException(AccResultCode.PROVISIONING_SCHEMA_ATTRIBUTE_IS_NOT_UPDATEABLE,
					ImmutableMap.of("property", idmPropertyName, "uid", uid));
		}
		
		String objectClassName = attributeHandling.getSchemaAttribute().getObjectClass().getObjectClassName();
		IcfAttribute icfAttributeForCreate = createIcfAttribute(attributeHandling, value);
		IcfObjectClass icfObjectClass = new IcfObjectClassImpl(objectClassName);
		// Call icf modul for update single attribute
		connectorFacade.updateObject(connectorKey, connectorConfig,icfObjectClass , uidAttribute, ImmutableList.of(icfAttributeForCreate));
		
	}

	/**
	 * Do provisioning/synchronisation/reconciliation on given system for given
	 * entity
	 * 
	 * @param uid
	 * @param entity
	 * @param provisioningType
	 * @param entityType
	 * @param system
	 */
	public void doOperation(String uid, AbstractEntity entity, AccountOperationType operationType,
			SystemOperationType provisioningType, SystemEntityType entityType, SysSystem system) {
		Assert.notNull(uid);
		Assert.notNull(provisioningType);
		Assert.notNull(system);
		Assert.notNull(entityType);

		List<SysSchemaAttributeHandling> attributes = findAttributesHandling(provisioningType, entityType, system);
		if (attributes == null || attributes.isEmpty()) {
			return;
		}

		// Find connector identification persisted in system
		IcfConnectorKey connectorKey = system.getConnectorKey();
		if (connectorKey == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_KEY_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}

		// Find connector configuration persisted in system
		IcfConnectorConfiguration connectorConfig = systemService.getConnectorConfiguration(system);
		if (connectorConfig == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND,
					ImmutableMap.of("system", system.getName()));
		}
		IcfUidAttribute uidAttribute = new IcfUidAttributeImpl(null, uid, null);

		Map<String, IcfConnectorObject> objectByClassMap = new HashMap<>();

		for (SysSchemaAttributeHandling ah : attributes) {
			String objectClassName = ah.getSchemaAttribute().getObjectClass().getObjectClassName();
			if (!objectByClassMap.containsKey(objectClassName)) {
				IcfObjectClass icfObjectClass = new IcfObjectClassImpl(objectClassName);
				IcfConnectorObject connectorObject = connectorFacade.readObject(connectorKey, connectorConfig, icfObjectClass, uidAttribute);
				objectByClassMap.put(objectClassName, connectorObject);
			}
		}

		if (SystemOperationType.PROVISIONING == provisioningType) {
			// Provisioning
			doProvisioning(uid, entity, operationType, attributes, connectorKey, connectorConfig, objectByClassMap);

		} else {
			// TODO Synchronisation or reconciliation
		}

	}

	/**
	 * Find list of {@link SysSchemaAttributeHandling} by provisioning type and entity type on given system
	 * @param provisioningType
	 * @param entityType
	 * @param system
	 * @return
	 */
	private List<SysSchemaAttributeHandling> findAttributesHandling(SystemOperationType provisioningType,
			SystemEntityType entityType, SysSystem system) {
		List<SysSystemEntityHandling> entityHandlingList = entityHandlingService.findBySystem(system, provisioningType,
				entityType);
		if (entityHandlingList == null || entityHandlingList.size() != 1) {
			return null;
		}

		SysSystemEntityHandling entityHandling = entityHandlingList.get(0);
		List<SysSchemaAttributeHandling> attributes = attributeHandlingService.findByEntityHandling(entityHandling);
		return attributes;
	}

	/**
	 * Do provisioning for given entity
	 * 
	 * @param uid
	 * @param entity
	 * @param attributes
	 * @param connectorKey
	 * @param connectorConfig
	 * @param uidAttribute
	 * @param objectByClassMap
	 */
	private void doProvisioning(String uid, AbstractEntity entity, AccountOperationType operationType,
			List<SysSchemaAttributeHandling> attributes, IcfConnectorKey connectorKey,
			IcfConnectorConfiguration connectorConfig, Map<String, IcfConnectorObject> objectByClassMap) {

		Map<String, IcfConnectorObject> objectByClassMapForUpdate = new HashMap<>();
		Map<String, IcfConnectorObject> objectByClassMapForCreate = new HashMap<>();
		Map<String, IcfConnectorObject> objectByClassMapForDelete = new HashMap<>();

		IcfUidAttribute uidAttribute = new IcfUidAttributeImpl(null, uid, null);

		// One IDM account can be mapped to more then one connector object (on more connector class).
		// We have to iterate via all mapped attribute and do operation for all object class

		for (SysSchemaAttributeHandling attributeHandling : attributes) {
			SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
			String objectClassName = schemaAttribute.getObjectClass().getObjectClassName();
			IcfConnectorObject connectorObject = objectByClassMap.get(objectClassName);
			if (AccountOperationType.UPDATE == operationType && connectorObject == null) {
				/**
				 * Create new connector object for this object class
				 */
				IcfConnectorObject connectorObjectForCreate = initConnectorObject(objectByClassMapForCreate, objectClassName);
				createAttribute(uid, entity, connectorObjectForCreate, attributeHandling,
						schemaAttribute, objectClassName);

			} else if (AccountOperationType.UPDATE == operationType && connectorObject != null) {
				/**
				 * Update connector object
				 */
				if (schemaAttribute.isUpdateable()) {
					if (!schemaAttribute.isReturnedByDefault()) {
						// TODO update for attributes not returned by default (for example __PASSWORD__)
					} else {
						// Update attribute on resource by given handling attribute and mapped value in entity
						updateAttribute(uid, entity, objectByClassMapForUpdate,
								attributeHandling, schemaAttribute, objectClassName, connectorObject);
					}
				}
			} else if (AccountOperationType.DELETE == operationType && connectorObject != null) {
				/**
				 * Delete connector object for this object class
				 */
				if (connectorObject != null && !objectByClassMapForDelete.containsKey(objectClassName)) {
					objectByClassMapForDelete.put(objectClassName, connectorObject);
				}
			}
		}

		// call create on ICF module
		objectByClassMapForCreate.forEach((objectClassName, connectorObject) -> {
			log.debug("Provisioning - create object with uid " + uid + " and connector object "
					+ connectorObject.getObjectClass().getType());
			connectorFacade.createObject(connectorKey, connectorConfig,
			connectorObject.getObjectClass(), connectorObject.getAttributes());
		});

		// call update on ICF module
		objectByClassMapForUpdate.forEach((objectClassName, connectorObject) -> {
			log.debug("Provisioning - update object with uid " + uid + " and connector object "
					+ connectorObject.getObjectClass().getType());
			connectorFacade.updateObject(connectorKey, connectorConfig,
			connectorObject.getObjectClass(), uidAttribute, connectorObject.getAttributes());
		});
		// call delete on ICF module
		objectByClassMapForDelete.forEach((objectClassName, connectorObject) -> {
			log.debug("Provisioning - delete object with uid " + uid + " and connector object "
					+ connectorObject.getObjectClass().getType());
			connectorFacade.deleteObject(connectorKey, connectorConfig,
					connectorObject.getObjectClass(), uidAttribute);
		});

	}

	/**
	 * Return connector object from map by object class. If object for object
	 * class is missing, then will be create.
	 * 
	 * @param objectByClassMap
	 * @param objectClassName
	 * @return
	 */
	private IcfConnectorObject initConnectorObject(Map<String, IcfConnectorObject> objectByClassMap,
			String objectClassName) {
		IcfConnectorObject connectorObject = null;

		if (objectByClassMap != null) {
			connectorObject = objectByClassMap.get(objectClassName);
		}
		if (connectorObject == null) {
			IcfObjectClass ioc = new IcfObjectClassImpl(objectClassName);
			connectorObject = new IcfConnectorObjectImpl(ioc, null);
			if (objectByClassMap != null) {
				objectByClassMap.put(objectClassName, connectorObject);
			}
		}
		return connectorObject;
	}

	/**
	 * Create ICF attribute by schema attribute.
	 * ICF attribute will be set with value obtained form given entity.
	 * This value will be transformed to system value first.
	 * @param uid
	 * @param entity
	 * @param connectorObjectForCreate
	 * @param attributeHandling
	 * @param schemaAttribute
	 * @param objectClassName
	 */
	private void createAttribute(String uid, AbstractEntity entity,
			IcfConnectorObject connectorObjectForCreate, SysSchemaAttributeHandling attributeHandling,
			SysSchemaAttribute schemaAttribute, String objectClassName) {
		if (schemaAttribute.isCreateable()) {
			try {
				Object idmValue = null;
				if (attributeHandling.isUid()) {
					// When is attribute marked as UID, then we use as
					// value input uid
					idmValue = uid;
				} else {
					// Find value from entity by property name
					idmValue = getEntityValue(entity, attributeHandling.getIdmPropertyName());

				}
				IcfAttribute icfAttributeForCreate = createIcfAttribute(attributeHandling, idmValue);
				connectorObjectForCreate.getAttributes().add(icfAttributeForCreate);

			} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid, "property", attributeHandling.getIdmPropertyName()), e);
			}
		}
	}

	/**
	 * Update attribute on resource by given handling attribute and mapped value
	 * in entity
	 * 
	 * @param uid
	 * @param entity
	 * @param objectByClassMapForUpdate
	 * @param attributeHandling
	 * @param schemaAttribute
	 * @param objectClassName
	 * @param connectorObject
	 */
	private void updateAttribute(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForUpdate, SysSchemaAttributeHandling attributeHandling,
			SysSchemaAttribute schemaAttribute, String objectClassName, IcfConnectorObject connectorObject) {
		List<IcfAttribute> icfAttributes = connectorObject.getAttributes();

		Optional<IcfAttribute> icfAttributeOptional = icfAttributes.stream().filter(icfa -> {
			return schemaAttribute.getName().equals(icfa.getName());
		}).findFirst();
		IcfAttribute icfAttribute = null;
		if (icfAttributeOptional.isPresent()) {
			icfAttribute = icfAttributeOptional.get();
		}
		if (schemaAttribute.isMultivalued()) {
			// TODO multi value
		} else {
			// Single value
			updateAttributeSingleValue(uid, entity, objectByClassMapForUpdate, attributeHandling, objectClassName,
					icfAttribute);
		}
	}

	/**
	 * Check difference of attribute value on resource and in entity for given
	 * attribute. When is value changed, then add update of this attribute to
	 * map
	 * 
	 * @param uid
	 * @param entity
	 * @param objectByClassMapForUpdate
	 * @param attributeHandling
	 * @param objectClassName
	 * @param icfAttribute
	 */
	private void updateAttributeSingleValue(String uid, AbstractEntity entity,
			Map<String, IcfConnectorObject> objectByClassMapForUpdate, SysSchemaAttributeHandling attributeHandling,
			String objectClassName, IcfAttribute icfAttribute) {

		Object icfValue = icfAttribute != null ? icfAttribute.getValue() : null;
		Object icfValueTransformed = attributeHandlingService.transformValueFromSystem(icfValue, attributeHandling);

		try {
			Object idmValue = null;
			if (attributeHandling.isUid()) {
				// When is attribute marked as UID, then we use as value input
				// uid
				idmValue = uid;
			} else {
				// Find value from entity by property name
				idmValue = getEntityValue(entity, attributeHandling.getIdmPropertyName());
			}

			if (!Objects.equals(idmValue, icfValueTransformed)) {
				// values is not equals
				IcfAttribute icfAttributeForUpdate = createIcfAttribute(attributeHandling, idmValue);
				IcfConnectorObject connectorObjectForUpdate = initConnectorObject(objectByClassMapForUpdate, objectClassName);
				connectorObjectForUpdate.getAttributes().add(icfAttributeForUpdate);
			}

		} catch (IntrospectionException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_IDM_FIELD_NOT_FOUND,
					ImmutableMap.of("uid", uid, "property", attributeHandling.getIdmPropertyName()), e);
		}
	}

	/**
	 * Create instance of ICF attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param attributeHandling
	 * @param icfAttribute
	 * @param idmValue
	 * @return
	 */
	private IcfAttribute createIcfAttribute(SysSchemaAttributeHandling attributeHandling, Object idmValue) {
		Object idmValueTransformed = attributeHandlingService.transformValueToSystem(idmValue, attributeHandling);
		SysSchemaAttribute schemaAttribute = attributeHandling.getSchemaAttribute();
		// Check type of value
		try {
			Class<?> classType = Class.forName(schemaAttribute.getClassType());
			if(idmValue != null && !(classType.isAssignableFrom(idmValue.getClass()))){
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
						ImmutableMap.of("attribute", attributeHandling.getIdmPropertyName(), "schemaAttributeType",
								schemaAttribute.getClassType(), "valueType", idmValue.getClass().getName()));
			}
		} catch (ClassNotFoundException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_TYPE_NOT_FOUND,
					ImmutableMap.of("attribute", attributeHandling.getIdmPropertyName(), "schemaAttributeType", schemaAttribute.getClassType()), e);
		}
		
		IcfAttribute icfAttributeForUpdate  = null;
		if(IcfConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(schemaAttribute.getName())){
			// Attribute is password type
			icfAttributeForUpdate = new IcfPasswordAttributeImpl((GuardedString) idmValue);
			
		} else {
			icfAttributeForUpdate = new IcfAttributeImpl(schemaAttribute.getName(),
					idmValueTransformed);
		}
		return icfAttributeForUpdate;
	}

	/**
	 * Return object from entity for given property name
	 * 
	 * @param entity
	 * @param propertyName
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private Object getEntityValue(AbstractEntity entity, String propertyName)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Optional<PropertyDescriptor> propertyDescriptionOptional = Arrays
				.asList(Introspector.getBeanInfo(entity.getClass(), AbstractEntity.class).getPropertyDescriptors())
				.stream().filter(propertyDescriptor -> {
					return propertyName.equals(propertyDescriptor.getName());
				}).findFirst();
		if (!propertyDescriptionOptional.isPresent()) {
			throw new IllegalAccessException("Field " + propertyName + " not found!");
		}
		PropertyDescriptor propertyDescriptor = propertyDescriptionOptional.get();

		return propertyDescriptor.getReadMethod().invoke(entity);
	}

}
