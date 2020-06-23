module.exports = {
  id: 'acc',
  npmName: 'czechidm-acc',
  backendId: 'acc',
  disableable: true,
  name: 'Account managment',
  description: 'Module for account managment',
  // 'mainStyleFile': 'src/css/main.less',
  mainRouteFile: 'routes.js',
  mainComponentDescriptorFile: 'component-descriptor.js',
  mainLocalePath: 'src/locales/',
  navigation: {
    items: [
      {
        id: 'sys-systems',
        type: 'DYNAMIC',
        labelKey: 'acc:content.systems.title',
        titleKey: 'acc:content.systems.title',
        order: 1100,
        priority: 0,
        path: '/systems',
        icon: 'component:systems',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ],
        items: [
          {
            id: 'sys-systems-main-menu',
            type: 'MAIN-MENU',
            labelKey: 'acc:content.systems.title',
            titleKey: 'acc:content.systems.title',
            order: 10,
            priority: 0,
            path: '/systems',
            icon: 'component:systems',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ],
            items: [
              {
                id: 'system-detail',
                type: 'TAB',
                labelKey: 'acc:content.system.detail.basic',
                order: 1,
                path: '/system/:entityId/detail',
                icon: 'fa:newspaper-o',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-connector',
                type: 'TAB',
                labelKey: 'acc:content.system.connector.title',
                order: 2,
                path: '/system/:entityId/connector',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-provisioning-break-config',
                type: 'TAB',
                icon: 'fa:stop-circle-o',
                labelKey: 'acc:content.provisioningBreakConfig.title',
                order: 10,
                path: '/system/:entityId/break-configs',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-accounts',
                type: 'TAB',
                labelKey: 'acc:content.system.accounts.title',
                order: 20,
                path: '/system/:entityId/accounts',
                icon: 'fa:external-link',
                access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
              },
              {
                id: 'system-entities',
                type: 'TAB',
                labelKey: 'acc:content.system.entities.title',
                order: 30,
                path: '/system/:entityId/entities',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'schema-object-classes',
                type: 'TAB',
                icon: 'fa:object-group',
                labelKey: 'acc:content.system.schemaObjectClasses.title',
                order: 40,
                path: '/system/:entityId/object-classes',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-mappings',
                type: 'TAB',
                icon: 'list-alt',
                labelKey: 'acc:content.system.mappings.title',
                order: 50,
                path: '/system/:entityId/mappings',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-roles',
                type: 'TAB',
                icon: 'component:roles',
                labelKey: 'acc:content.systemRoles.title',
                titleKey: 'acc:content.systemRoles.header',
                order: 55,
                path: '/system/:entityId/roles',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-synchronization-configs',
                type: 'TAB',
                icon: 'component:synchronizations',
                labelKey: 'acc:content.system.systemSynchronizationConfigs.title',
                order: 60,
                path: '/system/:entityId/synchronization-configs',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-provisioning-operations',
                type: 'TAB',
                labelKey: 'acc:content.provisioningOperations.label',
                titleKey: 'acc:content.provisioningOperations.title',
                order: 70,
                path: '/system/:entityId/provisioning',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
              }
            ]
          },
          {
            id: 'password-filter',
            labelKey: 'acc:content.passwordFilter.title',
            order: 20,
            priority: 0,
            path: '/password-filter',
            icon: 'filter',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ],
            items: [
              {
                id: 'password-filter-detail',
                type: 'TAB',
                labelKey: 'acc:content.passwordFilter.basic',
                order: 10,
                path: '/password-filter/:entityId/detail',
                icon: 'fa:newspaper-o',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
              },
              {
                id: 'password-filter-system',
                type: 'TAB',
                labelKey: 'acc:content.passwordFilterSystem.detail',
                order: 20,
                path: '/password-filter/:entityId/systems',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
              }
            ]
          }
        ]
      },
      {
        id: 'identity-accounts',
        parentId: 'identity-profile',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 100,
        priority: 0,
        path: '/identity/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYACCOUNT_READ'] } ]
      },
      {
        id: 'identity-provisioning-operations',
        parentId: 'identity-profile',
        type: 'TAB',
        labelKey: 'acc:content.provisioningOperations.label',
        titleKey: 'acc:content.provisioningOperations.title',
        order: 110,
        path: '/identity/:entityId/provisioning',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
      },
      {
        id: 'identity-contract-accounts',
        parentId: 'profile-contracts',
        type: 'TAB',
        labelKey: 'acc:content.contract.accounts.title',
        order: 100,
        priority: 0,
        path: '/identity/:identityId/identity-contract/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTACCOUNT_READ'] } ]
      },
      {
        id: 'contract-slice-accounts',
        parentId: 'identity-contract-slices',
        type: 'TAB',
        labelKey: 'acc:content.contract-slice.accounts.title',
        order: 100,
        priority: 0,
        path: '/identity/:identityId/contract-slice/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICEACCOUNT_READ'] } ]
      },
      // Provisioning for contract is not implemented yet
      // {
      //   'id': 'identity-contract-provisioning-operations',
      //   'parentId': 'profile-contracts',
      //   'type': 'TAB',
      //   'labelKey': 'acc:content.provisioningOperations.label',
      //   'titleKey': 'acc:content.provisioningOperations.title',
      //   'order': 110,
      //   'path': '/identity/:identityId/identity-contract/:entityId/provisioning',
      //   'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_ADMIN'] } ]
      // },
      {
        id: 'password-policies-systems',
        parentId: 'password-policies',
        type: 'TAB',
        labelKey: 'acc:content.passwordPolicy.system.tab',
        order: 100,
        priority: 0,
        path: '/password-policies/:entityId/systems',
        icon: '',
        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['PASSWORDPOLICY_READ', 'SYSTEM_READ'] } ]
      },
      {
        id: 'role-systems',
        type: 'TAB',
        parentId: 'roles',
        labelKey: 'acc:content.role.systems.title',
        titleKey: 'acc:content.role.systems.title',
        order: 500,
        path: '/role/:entityId/systems',
        icon: 'link',
        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
      },
      {
        id: 'request-role-systems',
        type: 'TAB',
        parentId: 'request-roles',
        labelKey: 'acc:content.role.systems.title',
        titleKey: 'acc:content.role.systems.title',
        order: 500,
        path: 'requests/:requestId/role/:entityId/systems',
        icon: 'link',
        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
      },
      {
        id: 'provisioning-operations',
        parentId: 'audit',
        labelKey: 'acc:content.provisioningOperations.label',
        titleKey: 'acc:content.provisioningOperations.title',
        order: 200,
        path: '/provisioning',
        access: [
          {
            type: 'HAS_ANY_AUTHORITY',
            authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ']
          }
        ]
      },
      {
        id: 'role-accounts',
        parentId: 'roles',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 500,
        priority: 0,
        path: '/role/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEACCOUNT_READ'] } ]
      },
      {
        id: 'tree-accounts',
        parentId: 'tree-nodes',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 500,
        priority: 0,
        path: '/tree/nodes/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREEACCOUNT_READ'] } ]
      },
      {
        id: 'role-catalogue-accounts',
        parentId: 'role-catalogues',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 500,
        priority: 0,
        path: '/role-catalogue/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUEACCOUNT_READ'] } ]
      }
    ]
  }
};
