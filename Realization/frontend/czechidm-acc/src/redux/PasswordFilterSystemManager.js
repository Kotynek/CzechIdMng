import { Managers } from 'czechidm-core';
import { PasswordFilterSystemService } from '../services';

const service = new PasswordFilterSystemService();

/**
 * Manager for password filter and connection to system
 *
 * @author Ondrej Kopr
 */
export default class PasswordFilterSystemManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'PasswordFilterSystem';
  }

  getCollectionType() {
    return 'passwordFilterSystems';
  }
}
