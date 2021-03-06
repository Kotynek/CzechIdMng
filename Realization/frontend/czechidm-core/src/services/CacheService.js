import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Cache operations
 *
 * @author Peter Štrunc
 */
export default class CacheService extends AbstractService {

  getApiPath() {
    return '/caches';
  }

  getNiceLabel(cache) {
    if (!cache) {
      return '';
    }
    return cache.name;
  }

  /**
   * Returns all available caches
   *
   * @return Promise
   */
  getAvailableCaches() {
    return RestApiService
      .get(this.getApiPath())
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Evict cache with given name
   *
   * @param {string} cacheName
   */
  evictCache(cacheId) {
    return RestApiService
      .patch(`${this.getApiPath() }/${cacheId}/evict`)
      .then(response => {
        if (response.status === 204) {
          return {
            id: cacheId
          };
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Evict all caches
   *
   */
  evictAllCaches() {
    return RestApiService
      .patch(`${this.getApiPath() }/evict`)
      .then(response => {
        if (response.status === 204) {
          return {
          };
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}
