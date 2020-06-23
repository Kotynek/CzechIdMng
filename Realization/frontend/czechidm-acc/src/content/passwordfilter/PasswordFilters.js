import React from 'react';
//
import { Basic } from 'czechidm-core';
import { PasswordFilterManager } from '../../redux';
import PasswordFilterTable from './PasswordFilterTable';

/**
 * List of password filters definition
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
export default class PasswordFilters extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new PasswordFilterManager();
  }

  getContentKey() {
    return 'acc:content.passwordFilter';
  }

  getNavigationKey() {
    return 'password-filter';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <PasswordFilterTable uiKey="password-filter-table" manager={this.manager}/>
        </Basic.Panel>

      </div>
    );
  }
}

PasswordFilters.propTypes = {
};
PasswordFilters.defaultProps = {
};
