import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { RoleSystemManager, SystemManager } from '../../redux';

const uiKey = 'role-systems-table';
const manager = new RoleSystemManager();
const systemManager = new SystemManager();
const roleManager = new Managers.RoleManager();

class RoleSystems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getManager() {
    return manager;
  }

  getContentKey() {
    return 'acc:content.role.systems';
  }

  componentDidMount() {
    this.selectNavigationItems(['roles', 'role-systems']);
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      role: entity.id && entity._embedded.role ? entity._embedded.role.id : this.props.params.entityId,
      system: entity.id && entity._embedded.system ? entity._embedded.system.id : null
    });

    this.setState({
      detail: {
        show: true,
        entity: entityFormData
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.system.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    entity.role = roleManager.getSelfLink(entity.role);
    entity.system = systemManager.getSelfLink(entity.system);
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.getManager().createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
        if (!error) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(this.getManager().patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { system: entity._embedded.system.name, role: entity._embedded.role.name }) });
    this.closeDetail();
  }

  onDelete(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: this.getManager().getNiceLabel(selectedEntities[0]), records: this.getManager().getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: this.getManager().getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntities(selectedEntities, uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  render() {
    const { entityId } = this.props.params;
    const { _showLoading, role } = this.props;
    const { detail } = this.state;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('roleId', entityId);

    const _role = role || {};

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])
              ?
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
              :
              null
            }
            buttons={
              [
                <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }>
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.Column property="_embedded.system.name" header={this.i18n('acc:entity.RoleSystem.system')} sort face="text" />
            <Advanced.Column property="type" header={this.i18n('acc:entity.RoleSystem.type')} sort face="text" />
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="default"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header', { role: _role.name })} rendered={detail.entity.id === undefined}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name, role: _role.name })} rendered={detail.entity.id !== undefined}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="form" showLoading={_showLoading} className="form-horizontal">
                <Basic.SelectBox
                  ref="role"
                  manager={roleManager}
                  label={this.i18n('acc:entity.RoleSystem.role')}
                  readOnly
                  required/>
                <Basic.SelectBox
                  ref="system"
                  manager={systemManager}
                  label={this.i18n('acc:entity.RoleSystem.system')}
                  required/>
                <Basic.TextField
                  ref="type"
                  label={this.i18n('acc:entity.RoleSystem.type')}
                  required/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

RoleSystems.propTypes = {
  role: PropTypes.object,
  _showLoading: PropTypes.bool,
};
RoleSystems.defaultProps = {
  role: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    role: Utils.Entity.getEntity(state, roleManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(RoleSystems);
