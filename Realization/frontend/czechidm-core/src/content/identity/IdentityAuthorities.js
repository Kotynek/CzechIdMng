import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { HelpContent } from '../../domain';
import { IdentityManager, DataManager } from '../../redux';
import AuthoritiesPanel from '../role/AuthoritiesPanel';

const uiKeyAuthorities = 'identity-authorities';
const identityManager = new IdentityManager();

/**
 * Identity's authorities.
 *
 * TODO: orchestrate with some menu / detail. Available just on url /identity/username/authorities
 *
 * @author Radek Tomiška
 */
class IdentityAuthorities extends Basic.AbstractContent {

  getContentKey() {
    return 'content.identity.authorities';
  }

  getNavigationKey() {
    return 'profile-authorities';
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.match.params;
    this.context.store.dispatch(identityManager.fetchAuthorities(entityId, `${uiKeyAuthorities}-${entityId}`));
  }

  getHelp() {
    let helpContent = new HelpContent();
    helpContent = helpContent.setHeader(this.i18n('help.header'));
    helpContent = helpContent.setBody(
      <div>
        <div>
          { this.i18n('help.body.title', { escape: false }) }
        </div>
        <div style={{ marginTop: 15 }}>
          { this.i18n('help.body.checkbox.title', { escape: false }) }
        </div>
        <ul>
          <li>
            <Basic.Icon value="fa:square-o" style={{ marginRight: 5 }}/>
            { this.i18n('help.body.checkbox.none', { escape: false }) }
          </li>
          <li>
            <Basic.Icon value="fa:minus-square-o" style={{ marginRight: 5 }}/>
            { this.i18n('help.body.checkbox.some', { escape: false }) }
          </li>
          <li>
            <Basic.Icon value="fa:check-square-o" style={{ marginRight: 5 }}/>
            { this.i18n('help.body.checkbox.all', { escape: false }) }
          </li>
        </ul>
      </div>
    );
    //
    return helpContent;
  }

  render() {
    const { authorities } = this.props;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Helmet title={this.i18n('title')} />

        <Basic.Panel className="no-border">
          <Basic.PanelHeader help={ this.getHelp() } style={{ marginBottom: 15 }}>
            <h3>{ this.i18n('header', { escape: false }) }</h3>
          </Basic.PanelHeader>

          <AuthoritiesPanel authorities={authorities} />

        </Basic.Panel>
      </div>
    );
  }
}

IdentityAuthorities.propTypes = {
  _showLoading: PropTypes.bool,
  authorities: PropTypes.arrayOf(PropTypes.object)
};
IdentityAuthorities.defaultProps = {
  _showLoading: true,
  authorities: []
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKeyAuthorities}-${component.match.params.entityId}`),
    authorities: DataManager.getData(state, `${uiKeyAuthorities}-${component.match.params.entityId}`)
  };
}

export default connect(select)(IdentityAuthorities);
