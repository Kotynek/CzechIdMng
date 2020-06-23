import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Managers, Utils } from 'czechidm-core';
import { SystemManager, PasswordFilterSystemManager, PasswordFilterManager } from '../../redux';

const manager = new PasswordFilterSystemManager();
const passwordFilterManager = new PasswordFilterManager();
const systemManager = new SystemManager();

/**
* Table of password filter definitions
*
* @author Ondrej Kopr
* @since 10.5.0
*/
class PasswordFilterSystemTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false
      }
    };
  }

  getContentKey() {
    return 'acc:content.passwordFilterSystem';
  }

  getManager() {
    return manager;
  }

  getDefaultSearchParameters() {
    return this.getManager().getDefaultSearchParameters();
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    const { entityId } = this.props.match.params;

    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    } else {
      entity.passwordFilter = entityId;
    }
    //
    super.showDetail(entity, () => {
      this.refs.system.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const {
      uiKey,
      columns,
      forceSearchParameters,
      className,
      _permissions
    } = this.props;

    const {
      showLoading,
      detail,
      _showLoading
    } = this.state;

    return (
      <Basic.Div>
        <Basic.Row>
          <Basic.Col lg={ 12 } >
            <Basic.Confirm ref="confirm-delete" level="danger"/>

            <Advanced.Table
              ref="table"
              uiKey={ uiKey }
              manager={ this.getManager() }
              rowClass={ ({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }
              filterOpened
              forceSearchParameters={ forceSearchParameters }
              showRowSelection
              showLoading={ showLoading }
              buttons={[
                <span>
                  <Basic.Button
                    level="success"
                    key="add_button"
                    className="btn-xs"
                    onClick={ this.showDetail.bind(this, { }) }
                    rendered={ Managers.SecurityManager.hasAuthority('PASSWORD_FILTER_CREATE') }
                    icon="fa:plus">
                    { this.i18n('button.add') }
                  </Basic.Button>
                </span>
              ]}
              actions={
                [
                  { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
                ]
              }
              _searchParameters={ this.getSearchParameters() }
              className={ className }>

              <Advanced.Column
                header=""
                className="detail-button"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Advanced.DetailButton
                        title={ this.i18n('button.detail') }
                        onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                    );
                  }
                }
                sort={false}/>

              <Advanced.Column
                property="system"
                sortProperty="system.name"
                face="text"
                header={ this.i18n('acc:entity.PasswordFilterSystem.system') }
                sort
                cell={
                  ({ rowIndex, data }) => {
                    const entity = data[rowIndex];
                    return (
                      <Advanced.EntityInfo
                        entityType="system"
                        entityIdentifier={ entity.system }
                        entity={ entity._embedded.system }
                        face="popover"
                        showIcon/>
                    );
                  }
                }
                rendered={_.includes(columns, 'system')}/>
            </Advanced.Table>
          </Basic.Col>
        </Basic.Row>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header
              icon="fa:list-alt"
              closeButton={ !_showLoading }
              text={ this.i18n('create.header')}
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              icon="fa:list-alt"
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header', { name: this.getManager().getNiceLabel(detail.entity) }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                data={ detail.entity }
                showLoading={ _showLoading }
                readOnly={ !this.getManager().canSave(detail.entity, _permissions) }>

                <Basic.SelectBox
                  ref="passwordFilter"
                  manager={ passwordFilterManager }
                  clearable={ false }
                  label={ this.i18n('acc:entity.PasswordFilterSystem.passwordFilter') }
                  required
                  readOnly/>

                <Basic.SelectBox
                  ref="system"
                  manager={ systemManager }
                  label={ this.i18n('acc:entity.PasswordFilterSystem.system') }
                  required/>

              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ manager.canSave(detail.entity, _permissions) }
                showLoading={ _showLoading}
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>

      </Basic.Div>
    );
  }
}

PasswordFilterSystemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Css
   */
  className: PropTypes.string
};

PasswordFilterSystemTable.defaultProps = {
  columns: ['system'],
  forceSearchParameters: null
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { forwardRef: true })(PasswordFilterSystemTable);
