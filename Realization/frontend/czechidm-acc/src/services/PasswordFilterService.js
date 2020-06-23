import { Services, Domain } from 'czechidm-core';

/**
 * Password filter service
 *
 * @author Ondrej Kopr
 */
export default class PasswordFilterService extends Services.AbstractService {

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.code}`;
  }

  getApiPath() {
    return '/password-filters';
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
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('code');
  }
}
