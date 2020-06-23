import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Domain } from 'czechidm-core';
import PasswordFilterSystemTable from './PasswordFilterSystemTable';
import { PasswordFilterSystemManager } from '../../redux';

/**
 * Password filter - table with connected system
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
export default class PasswordFilterSystems extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.manager = new PasswordFilterSystemManager();
  }

  getContentKey() {
    return 'acc:content.passwordFilterSystem';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('password-filter-system', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new Domain.SearchParameters().setFilter('passwordFilterId', this.props.match.params.entityId);
    return (
      <div>
        <Helmet title={this.i18n('detail')} />

        <Basic.ContentHeader text={ this.i18n('detail') } style={{ marginBottom: 0 }}/>
        <PasswordFilterSystemTable
          uiKey="password-filter-system-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"
          manager={ this.manager }
          match={ this.props.match }/>
      </div>
    );
  }
}
