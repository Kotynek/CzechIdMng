import React from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import PasswordFilterDetail from './PasswordFilterDetail';
import { PasswordFilterManager } from '../../redux';

const manager = new PasswordFilterManager();
/**
 * Passsword filter content
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
class PasswordFilterContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};

    const { entityId } = this.props.match.params;

    if (this._isNew()) {
      // For newly created entities setup default timeout
      this.context.store.dispatch(manager.receiveEntity(entityId, { timeout: 216000 }));
    } else {
      this.context.store.dispatch(manager.fetchEntity(entityId, null, (entity, error) => {
        this.handleError(error);
      }));
    }
  }

  getContentKey() {
    return 'acc:content.passwordFilter';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('password-filter-detail', this.props.match.params);
  }

  componentDidMount() {
    super.componentDidMount();
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading, permissions } = this.props;
    return (
      <Basic.Row>
        <div className={this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12'}>
          {
            !entity
            ||
            <PasswordFilterDetail
              entity={entity}
              showLoading={showLoading}
              manager={ manager }
              permissions={ permissions }
              match={ this.props.match }/>
          }
        </div>
      </Basic.Row>
    );
  }
}

PasswordFilterContent.propTypes = {
};

PasswordFilterContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    permissions: manager.getPermissions(state, null, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordFilterContent);
