import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { RoleManager } from '../../redux';

let manager = null;

/**
 * Extended role attributes
 *
 * @author Radek Tomiška
 */
class RoleEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(this.props.match.params, new RoleManager());
  }

  getContentKey() {
    return 'content.role.eav';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-eav', this.props.match.params);
  }

  render() {
    const { entityId } = this.props.match.params;
    const { _entity, _permissions } = this.props;
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <Advanced.EavContent
          formableManager={ manager }
          entityId={ entityId }
          contentKey={ this.getContentKey() }
          showSaveButton={ manager.canSave(_entity, _permissions) }/>
      </Basic.Div>
    );
  }
}

RoleEav.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
RoleEav.defaultProps = {
  _entity: null,
  _permissions: null
};

function select(state, component) {
  if (!manager) {
    return {};
  }
  return {
    _entity: manager.getEntity(state, component.match.params.entityId),
    _permissions: manager.getPermissions(state, null, component.match.params.entityId)
  };
}

export default connect(select)(RoleEav);
