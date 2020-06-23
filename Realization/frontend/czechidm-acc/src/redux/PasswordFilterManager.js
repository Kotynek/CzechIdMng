import { Managers } from 'czechidm-core';
import { PasswordFilterService } from '../services';

const service = new PasswordFilterService();

/**
 * Password filter definition manager for frontend.
 *
 * @author Ondrej Kopr
 */
export default class PasswordFilterManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'PasswordFilter';
  }

  getCollectionType() {
    return 'passwordFilters';
  }
}
