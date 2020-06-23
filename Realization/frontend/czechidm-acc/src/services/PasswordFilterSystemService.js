import { Services, Domain } from 'czechidm-core';

/**
 * Service for password filter and connection to system
 *
 * @author Ondrej Kopr
 */
export default class PasswordFilterSystemService extends Services.AbstractService {

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (entity._embedded && entity._embedded.system) {
      return `${ entity._embedded.system.name }`;
    }
    return '';
  }

  getApiPath() {
    return '/password-filter-systems';
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'PASSWORDFILTER';
  }

  // dto
  supportsPatch() {
    return true;
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('system.name');
  }
}
