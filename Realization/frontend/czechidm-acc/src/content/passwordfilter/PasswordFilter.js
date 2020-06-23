import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced } from 'czechidm-core';
import { PasswordFilterManager } from '../../redux';

/**
 * Password filter detail with menu
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */

const manager = new PasswordFilterManager();

class PasswordFilter extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="filter"
          entity={ entity }
          showLoading={ !entity && showLoading }>
          { manager.getNiceLabel(entity) } <small> { this.i18n('acc:content.passwordFilter.edit.header') }</small>
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="password-filter" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

PasswordFilter.propTypes = {
};
PasswordFilter.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordFilter);
