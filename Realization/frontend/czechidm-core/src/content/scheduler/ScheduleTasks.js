import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import moment from 'moment';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import * as Domain from '../../domain';
//
import { SchedulerManager, DataManager, ConfigurationManager, SecurityManager, FormAttributeManager } from '../../redux';
import TriggerTypeEnum from '../../enums/TriggerTypeEnum';

const manager = new SchedulerManager();
const formAttributeManager = new FormAttributeManager();

/**
 * Unlimited description in task selectbox (used for select dependent task) with task parameters
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class SchedulerTaskOptionDecorator extends Basic.SelectBox.OptionDecorator {

  static _getIcon(supportedTasks, entity) {
    //
    let _task;
    if (supportedTasks && supportedTasks.has(entity.taskType)) {
      _task = supportedTasks.get(entity.taskType);
    }
    //
    let icon = null;
    if (_task && _task.formDefinition) {
      icon = formAttributeManager.getLocalization(_task.formDefinition, null, 'icon', null);
    }
    //
    if (!icon) {
      // default
      return 'component:scheduled-task';
    }
    return icon;
  }

  getEntityIcon(entity) {
    const { supportedTasks } = this.props;
    //
    return SchedulerTaskOptionDecorator._getIcon(supportedTasks, entity);
  }

  getDescriptionMaxLength() {
    return null;
  }

  renderDescription(entity) {
    const { supportedTasks } = this.props;
    //
    const filledParameters = _.keys(entity.parameters).filter(parameterName => {
      if (Utils.Ui.isEmpty(entity.parameters[parameterName])) {
        // not filled (false is needed to render)
        return false;
      }
      if (parameterName.lastIndexOf('core:', 0) === 0) { // core parameter
        // not filled (false is needed to render)
        return false;
      }
      //
      return true;
    });
    //
    return (
      <Basic.Div>
        { super.renderDescription(entity) }
        {
          filledParameters.length === 0
          ||
          <Basic.Div style={{ color: '#555', fontSize: '0.95em' }}>
            <strong>{ this.i18n('content.scheduler.schedule-tasks.action.task-edit.parameters') } :</strong>
            <Basic.Div style={{ fontStyle: 'italic' }}>
              <Advanced.LongRunningTaskProperties entity={ entity } supportedTasks={ supportedTasks } condensed/>
            </Basic.Div>
          </Basic.Div>
        }
      </Basic.Div>
    );
  }
}

/**
 * Task icon in select box value.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
class SchedulerTaskValueDecorator extends Basic.SelectBox.ValueDecorator {

  getEntityIcon(entity) {
    const { supportedTasks } = this.props;
    //
    return SchedulerTaskOptionDecorator._getIcon(supportedTasks, entity);
  }
}

/**
 * Scheduler administration
 *
 * @author Radek Tomiška
 */
class ScheduleTasks extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      triggerDetail: {
        show: false,
        entity: {}
      },
      triggerType: TriggerTypeEnum.findKeyBySymbol(TriggerTypeEnum.SIMPLE),
      taskType: null
    };
  }

  getContentKey() {
    return 'content.scheduler.schedule-tasks';
  }

  getNavigationKey() {
    return 'scheduler-schedule-tasks';
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return SchedulerManager.UI_KEY_TASKS;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(manager.fetchSupportedTasks());
    if (this.refs.text) {
      this.refs.text.focus();
    }
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

  /**
   * Open modal task detail
   */
  showDetail(entity) {
    const { supportedTasks } = this.props;
    //
    if (entity.parameters && entity.parameters.dryRun) {
      entity.dryRun = true;
    }
    //
    this.setState({
      detail: {
        show: true,
        entity
      },
      taskType: supportedTasks.has(entity.taskType) ? this._toOption(supportedTasks.get(entity.taskType)) : null
    }, () => {
      setTimeout(() => {
        this.refs.form.setData(entity);
        this.refs.taskType.focus();
      }, 10);
    });
  }

  /**
   * Close modal task detail
   */
  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      },
      taskType: null
    });
  }

  onChangeTaskType(taskType) {
    this.setState({
      taskType
    });
  }

  /**
   * Saves give entity
   */
  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    if (this.refs.formInstance) {
      if (!this.refs.formInstance.isValid()) {
        return;
      }
    }
    const formEntity = this.refs.form.getData();
    // transform properties
    if (this.refs.formInstance) {
      formEntity.parameters = this.refs.formInstance.getProperties();
    }
    //
    if (formEntity.dryRun) {
      formEntity.parameters.dryRun = true;
    }
    // remove trigger - they are not saved
    delete formEntity.triggers;
    //
    if (Utils.Entity.isNew(formEntity)) {
      this.context.store.dispatch(this.getManager().createEntity(formEntity, this.getUiKey(), this.afterSave.bind(this)));
    } else {
      this.context.store.dispatch(this.getManager().updateEntity(formEntity, this.getUiKey(), this.afterSave.bind(this)));
    }
  }

  /**
   * Callback after save
   */
  afterSave(entity, error) {
    if (error) {
      this.addError(error);
      this.refs.form.processEnded();
      return;
    }
    this.addMessage({ message: this.i18n('action.save.success', { record: this.getManager().getNiceLabel(entity) }) });
    this.refs.table.reload();
    this.closeDetail();
  }

  onRun(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-task-run'].show(
      this.i18n(`action.task-run.message`, { record: this.getManager().getNiceLabel(entity, true, this.props.supportedTasks) }),
      this.i18n(`action.task-run.header`)
    ).then(() => {
      this.context.store.dispatch(this.getManager().runTask(entity.id, () => {
        this.addMessage({
          message: this.i18n('action.task-run.success', {
            count: 1,
            record: this.getManager().getNiceLabel(entity, true, this.props.supportedTasks)
          })
        });
      }));
    }, () => {
      // Rejected
    });
  }

  onDryRun(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-task-dry-run'].show(
      this.i18n(`action.task-dry-run.message`, { record: this.getManager().getNiceLabel(entity, true, this.props.supportedTasks) }),
      this.i18n(`action.task-dry-run.header`)
    ).then(() => {
      this.context.store.dispatch(this.getManager().dryRunTask(entity.id, () => {
        this.addMessage({
          message: this.i18n('action.task-dry-run.success', {
            count: 1,
            record: this.getManager().getNiceLabel(entity, true, this.props.supportedTasks)
          })
        });
      }));
    }, () => {
      // Rejected
    });
  }

  onTriggerDelete(trigger) {
    const record = moment(trigger.nextFireTime).format(this.i18n('format.datetime'));
    // show confirm message for deleting entity or entities
    this.refs['confirm-delete'].show(
      this.i18n(`action.trigger-delete.message`, { record }),
      this.i18n(`action.trigger-delete.header`)
    ).then(() => {
      this.context.store.dispatch(manager.deleteTrigger(trigger, () => {
        this.addMessage({ message: this.i18n(`action.trigger-delete.success`) });
        this.refs.table.reload();
      }));
    }, () => {
      //
    });
  }

  saveTrigger(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.triggerForm.isFormValid()) {
      return;
    }
    const formEntity = this.refs.triggerForm.getData();
    //
    if (formEntity.type === 'REPEAT') {
      formEntity.type = 'CRON';
      formEntity.cron = this.refs.repeat.getCron();
      formEntity.executeDate = this.refs.repeat.getExecuteDate();
    }

    this.context.store.dispatch(this.getManager().createTrigger(formEntity, () => {
      this.addMessage({ message: this.i18n('action.trigger-create.success') });
      this.closeTriggerDetail();
      this.refs.table.reload();
    }));
  }

  onChangeTriggerType(triggerType) {
    // If the repeat task is chosen, trigger init of cron expression preview
    this.setState({
      triggerType: triggerType.value
    }, () => this.initCron());
  }

  initCron() {
    if (this.state.triggerType === 'REPEAT') {
      this.refs.repeat.generateCron();
    }
  }

  /**
   * Shows modal detail with given entity
   */
  showTriggerDetail(entity) {
    this.setState({
      triggerDetail: {
        show: true,
        entity
      }
    }, () => {
      setTimeout(() => {
        this.refs.triggerForm.setData(entity);
        this.refs.type.focus();
      }, 10);
    });
  }

  /**
   * Close modal detail
   */
  closeTriggerDetail() {
    this.setState({
      triggerDetail: {
        show: false,
        entity: {}
      }
    });
  }

  getLevel(data) {
    if (data.parameters && data.parameters.dryRun) {
      return 'info';
    }
    return 'success';
  }

  _toOption(task) {
    return {
      niceLabel:
        task.formDefinition
        ?
        formAttributeManager.getLocalization(task.formDefinition, null, 'label', Utils.Ui.getSimpleJavaType(task.taskType))
        :
        Utils.Ui.getSimpleJavaType(task.taskType),
      value: task.taskType,
      description:
        task.formDefinition
        ?
        formAttributeManager.getLocalization(task.formDefinition, null, 'help', task.description)
        :
        task.description,
      parameters: task.parameters,
      formDefinition: task.formDefinition,
      disabled: task.disabled,
      _icon:
        task.formDefinition
        ?
        formAttributeManager.getLocalization(task.formDefinition, null, 'icon', 'component:scheduled-task')
        :
        'component:scheduled-task'
    };
  }

  render() {
    const { supportedTasks, showLoading, showLoadingDetail, instanceId } = this.props;
    const { detail, triggerDetail, triggerType, taskType } = this.state;
    //
    const _supportedTasks = [];
    if (supportedTasks) {
      supportedTasks.forEach(task => {
        _supportedTasks.push(this._toOption(task));
      });
    }
    _supportedTasks.sort((one, two) => one.niceLabel > two.niceLabel);
    const entityParameterNames = [];
    if (detail.entity.parameters) {
      _.keys(detail.entity.parameters).forEach(parameterName => {
        if (parameterName.lastIndexOf('core:', 0) !== 0) {
          entityParameterNames.push(parameterName);
        }
      });
    }
    let formInstance = new Domain.FormInstance({});
    if (taskType && taskType.formDefinition && detail.entity) {
      formInstance = new Domain.FormInstance(taskType.formDefinition).setProperties(detail.entity.parameters);
    }
    const showProperties = formInstance && taskType && taskType.formDefinition && taskType.formDefinition.formAttributes.length > 0;
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Confirm ref="confirm-task-run" level="success"/>
        <Basic.Confirm ref="confirm-task-dry-run" level="info"/>

        <Advanced.Table
          ref="table"
          uiKey="schedule-task-table"
          manager={ manager }
          showRowSelection={ SecurityManager.hasAnyAuthority(['SCHEDULER_DELETE']) }
          showAuditLink={ false }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, _supportedTasks.length === 0 ? {} : { name: _supportedTasks[0], instanceId }) }
                rendered={ _supportedTasks.length > 0 && SecurityManager.hasAnyAuthority(['SCHEDULER_CREATE'])}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 8 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text.placeholder') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened
          _searchParameters={ this.getSearchParameters() }
          uuidEnd>
          <Advanced.Column
            className="detail-button"
            cell={
              ({ rowIndex, data }) => (
                <Advanced.DetailButton
                  title={ data[rowIndex].disabled ? this.i18n('label.disabled') : this.i18n('button.detail') }
                  onClick={ this.showDetail.bind(this, data[rowIndex]) }
                  disabled={ data[rowIndex].disabled }/>
              )
            }/>
          <Advanced.Column
            property="taskType"
            sort
            width={ 150 }
            cell={
              ({ rowIndex, data }) => (
                <Advanced.LongRunningTaskName entity={ data[rowIndex] } supportedTasks={ supportedTasks }/>
              )
            }/>
          <Basic.Column
            header={ this.i18n('action.task-edit.parameters') }
            width={ 300 }
            cell={
              ({ rowIndex, data }) => (
                <Advanced.LongRunningTaskProperties
                  key={ `lrt-eav-${ data[rowIndex].id }` }
                  entity={ data[rowIndex] }
                  supportedTasks={ supportedTasks }
                  condensed/>
              )
            }/>
          <Advanced.Column property="description" sort />
          <Advanced.Column property="instanceId" sort />
          <Basic.Column
            property="triggers"
            header={ this.i18n('entity.SchedulerTask.triggers') }
            width={ 200 }
            cell={
              ({ data, rowIndex, property}) => {
                const triggers = data[rowIndex][property];
                return (
                  <Basic.Div>
                    {
                      triggers.map(trigger => {
                        if (!trigger.initiatorTaskId
                            && (!trigger.nextFireTime || (trigger.fireTime && moment(trigger.fireTime).isBefore(moment())))) {
                          // only plan
                          return null;
                        }
                        return (
                          <Basic.Div>
                            {
                              trigger.initiatorTaskId
                              ?
                              <Advanced.SchedulerTaskInfo
                                entityIdentifier={ trigger.initiatorTaskId }
                                face="popover"/>
                              :
                              <Advanced.DateValue value={trigger.nextFireTime} title={trigger.cron ? `Cron: ${trigger.cron}` : null} showTime />
                            }
                            {' '}
                            <Basic.Button
                              level="link"
                              className="btn-xs"
                              onClick={ this.onTriggerDelete.bind(this, trigger) }
                              rendered={ SecurityManager.hasAnyAuthority(['SCHEDULER_DELETE']) }>
                              <Basic.Icon value="remove" color="red"/>
                            </Basic.Button>
                          </Basic.Div>
                        );
                      })
                    }
                    <Basic.Button
                      level="success"
                      className="btn-xs"
                      onClick={ this.showTriggerDetail.bind(this, { type: triggerType, taskId: data[rowIndex].id }) }
                      rendered={ !data[rowIndex].disabled && SecurityManager.hasAnyAuthority(['SCHEDULER_CREATE']) }>
                      <Basic.Icon value="plus"/>
                      {' '}
                      { this.i18n('button.add') }
                    </Basic.Button>
                  </Basic.Div>
                );
              }
            }/>
          <Basic.Column
            header={ this.i18n('label.action') }
            className="action"
            cell={
              ({ rowIndex, data }) => (
                <Basic.Div>
                  <Basic.Button
                    level="info"
                    onClick={ this.onDryRun.bind(this, data[rowIndex]) }
                    className="btn-xs"
                    title={ data[rowIndex].supportsDryRun ? this.i18n('button.dryRun') : this.i18n('error.SCHEDULER_DRY_RUN_NOT_SUPPORTED.title') }
                    titlePlacement="bottom"
                    style={{ marginLeft: 3 }}
                    rendered={ !data[rowIndex].disabled && SecurityManager.hasAnyAuthority(['SCHEDULER_EXECUTE']) }
                    disabled={ !data[rowIndex].supportsDryRun }
                    icon="play"/>
                  <Basic.Button
                    level="success"
                    onClick={ this.onRun.bind(this, data[rowIndex]) }
                    className="btn-xs"
                    title={ this.i18n('button.run') }
                    titlePlacement="bottom"
                    style={{ marginLeft: 3 }}
                    rendered={ !data[rowIndex].disabled && SecurityManager.hasAnyAuthority(['SCHEDULER_EXECUTE']) }
                    icon="play"/>
                </Basic.Div>
              )
            }/>
        </Advanced.Table>

        <Basic.Modal
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static"
          keyboard={ !showLoading }>

          <form onSubmit={ this.save.bind(this) }>
            <Basic.Modal.Header
              closeButton={ !showLoadingDetail }
              text={ !Utils.Entity.isNew(detail.entity) ? this.i18n('action.task-edit.header') : this.i18n('action.task-create.header')}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={ showLoadingDetail }
                readOnly={ !this.getManager().canSave(detail.entity) }>
                <Basic.EnumSelectBox
                  ref="taskType"
                  label={ this.i18n('entity.SchedulerTask.taskType') }
                  options={ _supportedTasks }
                  onChange={ this.onChangeTaskType.bind(this) }
                  required
                  searchable
                  readOnly={ !Utils.Entity.isNew(detail.entity) }
                  helpBlock={ taskType ? taskType.description : null }
                  clearable={ false }/>
                <Basic.TextArea
                  ref="description"
                  placeholder={ taskType ? taskType.description : null }
                  label={ this.i18n('entity.SchedulerTask.description') }
                  max={255}/>
                <Basic.TextField
                  ref="instanceId"
                  label={ this.i18n('entity.SchedulerTask.instanceId.label') }
                  helpBlock={ this.i18n('entity.SchedulerTask.instanceId.help') }
                  required/>
                <Basic.Div style={ showProperties ? {} : { display: 'none' }}>
                  <Basic.ContentHeader text={ this.i18n('action.task-edit.parameters') }/>
                  <Advanced.EavForm
                    ref="formInstance"
                    formInstance={ formInstance }
                    useDefaultValue={ Utils.Entity.isNew(detail.entity) }/>
                </Basic.Div>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ showLoadingDetail }>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoadingDetail }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ this.getManager().canSave(detail.entity) }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>

        <Basic.Modal
          show={triggerDetail.show}
          onHide={this.closeTriggerDetail.bind(this)}
          backdrop="static"
          keyboard={!showLoading}>

          <form onSubmit={ this.saveTrigger.bind(this) }>
            <Basic.Modal.Header closeButton={ !showLoading } text={ this.i18n('action.trigger-create.header') }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm ref="triggerForm" showLoading={ showLoading }>
                <Basic.EnumSelectBox
                  ref="type"
                  enum={ TriggerTypeEnum }
                  label={ this.i18n('entity.SchedulerTask.trigger._type.label') }
                  required
                  onChange={ this.onChangeTriggerType.bind(this) }
                  clearable={ false }/>
                <Basic.DateTimePicker
                  ref="fireTime"
                  label={ this.i18n('entity.SchedulerTask.trigger.fireTime') }
                  hidden={ triggerType !== 'SIMPLE' }
                  required={ triggerType === 'SIMPLE' }/>
                <Advanced.CronGenerator
                  ref="repeat"
                  hidden={ triggerType !== 'REPEAT' }
                  required={ triggerType === 'REPEAT' }/>
                <Basic.TextField
                  ref="cron"
                  label={ this.i18n('entity.SchedulerTask.trigger.cron.label') }
                  helpBlock={
                    <span>
                      { this.i18n('entity.SchedulerTask.trigger.cron.help') }
                      {' '}
                      <Basic.Link
                        href={ this.i18n('entity.SchedulerTask.trigger.cron.link.href') }
                        text={ this.i18n('entity.SchedulerTask.trigger.cron.link.text') }/>
                    </span>
                  }
                  hidden={ triggerType !== 'CRON' }
                  required={ triggerType === 'CRON' }/>
                <Basic.DateTimePicker
                  ref="executeDate"
                  label={ this.i18n('entity.SchedulerTask.trigger.executeDate.label') }
                  helpBlock={ this.i18n('entity.SchedulerTask.trigger.executeDate.help') }
                  hidden={ triggerType !== 'CRON' }/>
                <Basic.SelectBox
                  ref="initiatorTaskId"
                  manager={ manager }
                  niceLabel={ (item) => `${ manager.getNiceLabel(item, false)} (${ item.id.substr(item.id.length - 7, item.id.length) })`}
                  label={ this.i18n('entity.SchedulerTask.trigger.dependent.initiatorTaskId.label') }
                  helpBlock={ this.i18n('entity.SchedulerTask.trigger.dependent.initiatorTaskId.help') }
                  hidden={ triggerType !== 'DEPENDENT' }
                  required={ triggerType === 'DEPENDENT' }
                  optionComponent={ connect(() => { return { supportedTasks }; })(SchedulerTaskOptionDecorator) }
                  valueComponent={ connect(() => { return { supportedTasks }; })(SchedulerTaskValueDecorator) }
                  clearable={ false }
                  searchInFields={ ['text', 'description'] }/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeTriggerDetail.bind(this) }
                showLoading={ showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

ScheduleTasks.propTypes = {
  instanceId: PropTypes.string,
  userContext: PropTypes.object,
  supportedTasks: PropTypes.object,
  showLoading: PropTypes.bool,
  showLoadingDetail: PropTypes.bool
};
ScheduleTasks.defaultProps = {
  instanceId: null,
  userContext: null,
  supportedTasks: null,
  showLoading: true,
  showLoadingDetail: false
};

function select(state) {
  return {
    instanceId: ConfigurationManager.getPublicValue(state, 'idm.pub.app.instanceId'),
    userContext: state.security.userContext,
    supportedTasks: DataManager.getData(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS),
    showLoading: Utils.Ui.isShowLoading(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS),
    showLoadingDetail: Utils.Ui.isShowLoading(state, SchedulerManager.UI_KEY_TASKS),
    _searchParameters: Utils.Ui.getSearchParameters(state, 'schedule-task-table')
  };
}

export default connect(select)(ScheduleTasks);
