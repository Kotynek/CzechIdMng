module.exports = {
  module: 'acc',
  childRoutes: [
    {
      path: 'systems',
      component: require('./src/content/system/Systems'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
    },
    {
      path: 'system/:entityId/',
      component: require('./src/content/system/System'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/system/SystemContent'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'entities',
          component: require('./src/content/system/SystemEntities'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs',
          component: require('./src/content/system/SystemSynchronizationConfigs'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'accounts',
          component: require('./src/content/system/SystemAccounts'),
          access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
        },
        {
          path: 'connector',
          component: require('./src/content/system/SystemConnector'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes',
          component: require('./src/content/system/SchemaObjectClasses'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes/:objectClassId/detail',
          component: require('./src/content/system/SchemaObjectClassDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes/:objectClassId/new',
          component: require('./src/content/system/SchemaObjectClassDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'schema-attributes/:attributeId/detail',
          component: require('./src/content/system/SchemaAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'schema-attributes/:attributeId/new',
          component: require('./src/content/system/SchemaAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'mappings',
          component: require('./src/content/system/SystemMappings'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'mappings/:mappingId/detail',
          component: require('./src/content/system/SystemMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'mappings/:mappingId/new',
          component: require('./src/content/system/SystemMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'attribute-mappings/:attributeId/detail',
          component: require('./src/content/system/SystemAttributeMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'attribute-mappings/:attributeId/new',
          component: require('./src/content/system/SystemAttributeMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'synchronization-configs/:configId/detail',
          component: require('./src/content/system/SystemSynchronizationConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs/:configId/new',
          component: require('./src/content/system/SystemSynchronizationConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'synchronization-logs/:logId/detail',
          component: require('./src/content/system/SystemSynchronizationLogDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-action-logs/:logActionId/detail',
          component: require('./src/content/system/SystemSyncActionLogDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-item-logs/:logItemId/detail',
          component: require('./src/content/system/SystemSyncItemLogDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'provisioning',
          component: require('./src/content/system/SystemProvisioningOperations'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
        },
        {
          path: 'break-configs',
          component: require('./src/content/system/SystemProvisioningBreakConfigs'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'break-configs/:configId/new',
          component: require('./src/content/system/SystemProvisioningBreakConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_CREATE'] } ]
        },
        {
          path: 'break-configs/:configId/detail',
          component: require('./src/content/system/SystemProvisioningBreakConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'roles',
          component: require('./src/content/system/SystemRoles'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/new',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/detail',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/attributes/:attributeId/detail',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/attributes/:attributeId/new',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        }
      ]
    },
    {
      path: 'system/:entityId/new',
      component: require('./src/content/system/SystemContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_CREATE'] } ]
    },
    {
      path: 'identity/:entityId/',
      component: require('czechidm-core/src/content/identity/Identity'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/identity/IdentityAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYACCOUNT_READ'] } ]
        },
        {
          path: 'provisioning',
          component: require('./src/content/identity/IdentityProvisioningOperations'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
        }
      ]
    },
    {
      path: '/identity/:identityId/identity-contract/:entityId',
      component: require('czechidm-core/src/content/identity/contract/IdentityContract'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/contract/ContractAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTACCOUNT_READ'] } ]
        },
        {
          path: 'provisioning',
          component: require('./src/content/contract/ContractProvisioningOperations'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
        }
      ]
    },
    {
      path: '/identity/:identityId/contract-slice/:entityId',
      component: require('czechidm-core/src/content/identity/contractSlice/ContractSlice'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/contractSlice/ContractSliceAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICEACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: 'password-policies/',
      component: require('czechidm-core/src/content/passwordpolicy/PasswordPolicyRoutes'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDPOLICY_READ'] } ],
      childRoutes: [
        {
          path: ':entityId/systems',
          component: require('./src/content/passwordpolicy/PasswordPolicySystems'),
          access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['PASSWORDPOLICY_READ', 'SYSTEM_READ'] } ]
        }
      ]
    },
    {
      path: 'role/:entityId/',
      component: require('czechidm-core/src/content/role/Role'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ],
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/role/RoleAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEACCOUNT_READ'] } ]
        },
        {
          path: 'systems',
          component: require('./src/content/role/RoleSystems'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/new',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/detail',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/attributes/:attributeId/detail',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/attributes/:attributeId/new',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        }
      ]
    },
    {
      path: 'requests/:requestId',
      component: require('czechidm-core/src/content/request/RequestInfo'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ],
      childRoutes: [
        {
          path: 'role/:entityId',
          component: require('czechidm-core/src/content/role/Role'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ],
          childRoutes: [
            {
              path: 'accounts',
              component: require('./src/content/role/RoleAccounts'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEACCOUNT_READ'] } ]
            },
            {
              path: 'systems',
              component: require('./src/content/role/RoleSystems'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/new',
              component: require('./src/content/role/RoleSystemDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/detail',
              component: require('./src/content/role/RoleSystemDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/attributes/:attributeId/detail',
              component: require('./src/content/role/RoleSystemAttributeDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/attributes/:attributeId/new',
              component: require('./src/content/role/RoleSystemAttributeDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
            }
          ]
        }
      ]
    },
    {
      path: 'tree',
      childRoutes: [
        {
          path: 'nodes/:entityId',
          component: require('czechidm-core/src/content/tree/node/Node'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREENODE_READ'] } ],
          childRoutes: [
            {
              path: 'accounts',
              component: require('./src/content/tree/TreeAccounts'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREEACCOUNT_READ'] } ]
            }
          ]
        }
      ]
    },
    {
      path: 'role-catalogue/:entityId',
      component: require('czechidm-core/src/content/rolecatalogue/RoleCatalogue'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUE_READ'] } ],
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/rolecatalogue/RoleCatalogueAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUEACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: 'provisioning',
      component: require('./src/content/provisioning/AuditProvisioningOperations'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ],
    },
    {
      path: 'password-filter/:entityId/new',
      component: require('./src/content/passwordfilter/PasswordFilterContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_CREATE'] } ],
    },
    {
      path: 'password-filter',
      component: require('./src/content/passwordfilter/PasswordFilters'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
    },
    {
      path: 'password-filter/:entityId/',
      component: require('./src/content/passwordfilter/PasswordFilter'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/passwordfilter/PasswordFilterContent'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
        },
        {
          path: 'systems',
          component: require('./src/content/passwordfilter/PasswordFilterSystems'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
        }
      ]
    }
  ]
};
