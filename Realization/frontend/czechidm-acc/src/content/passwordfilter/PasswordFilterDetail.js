import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Utils, Advanced, Enums, Managers } from 'czechidm-core';
import { PasswordFilterManager } from '../../redux';

/**
 * Password filter definition detail
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
export default class PasswordFilterDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      _showLoading: true
    };
    this.manager = new PasswordFilterManager();
    this.scriptManager = new Managers.ScriptManager();
  }

  getContentKey() {
    return 'acc:content.passwordFilter';
  }

  componentDidMount() {
    const { entity } = this.props;
    this._selectEntity(entity);
  }

  _selectEntity(entity) {
    this.setState({
      _showLoading: false
    }, () => {
      this.refs.form.setData(entity);
      this.refs.code.focus();
    });
  }

  save(afterAction, event) {
    const { manager } = this.props;
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    const entity = this.refs.form.getData();
    this.refs.form.processStarted();

    if (Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(manager.createEntity(entity, null, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
        this.context.history.replace(`${this.addRequestPrefix('password-filter', this.props.match.params)}/${createdEntity.id}/detail`);
      }));
    } else {
      this.context.store.dispatch(manager.updateEntity(entity, null, (patchedEntity, error) => {
        this._afterSave(patchedEntity, error);
      }));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    //
    this.refs.form.processEnded();
    this.addMessage({ message: this.i18n('save.success', { code: entity.code }) });
  }

  render() {
    const { entity,
      showLoading,
      permissions,
      manager } = this.props;

    const { _showLoading } = this.state;

    if (!entity) {
      return null;
    }

    return (
      <Basic.Div>
        <Helmet title={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic') } />
        <form onSubmit={ this.save.bind(this, 'CONTINUE') }>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader
              text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic') }/>
            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading || showLoading }
                readOnly={ !manager.canSave(entity, permissions) }>


                <Basic.TextField
                  ref="code"
                  label={ this.i18n('acc:entity.PasswordFilter.code') }
                  max={ 255 }
                  required/>

                <Basic.TextField
                  ref="timeout"
                  label={ this.i18n('acc:entity.PasswordFilter.timeout.label') }
                  helpBlock={ this.i18n('acc:entity.PasswordFilter.timeout.helpBlock') }
                  type="number"
                  required/>

                <Basic.Checkbox
                  ref="changeInIdm"
                  label={ this.i18n('acc:entity.PasswordFilter.changeInIdm.label') }
                  helpBlock={ this.i18n('acc:entity.PasswordFilter.changeInIdm.helpBlock') }/>
                <Basic.TextArea
                  ref="description"
                  label={ this.i18n('acc:entity.PasswordFilter.description') }
                  max={2000}/>
                <Basic.Checkbox
                  ref="disabled"
                  label={ this.i18n('acc:entity.PasswordFilter.disabled.label') }
                  helpBlock={ this.i18n('acc:entity.PasswordFilter.disabled.helpBlock') }/>
                <Advanced.ScriptArea
                  ref="transformationScript"
                  scriptCategory={ [Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.SYSTEM),
                    Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.SYSTEM)] }
                  headerText={ this.i18n('acc:entity.PasswordFilter.transformationScriptSelectBox.label') }
                  helpBlock={ this.i18n('acc:entity.PasswordFilter.transformationScript.helpBlock') }
                  label={ this.i18n('acc:entity.PasswordFilter.transformationScript.label') }
                  scriptManager={ this.scriptManager }/>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button
                type="button"
                level="link"
                onClick={this.context.history.goBack}
                showLoading={ _showLoading} >
                { this.i18n('button.back') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoadingIcon
                onClick={ this.save.bind(this, 'CONTINUE') }
                showLoadingText={ this.i18n('button.saving') }
                rendered={ manager.canSave(entity, permissions) }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </Basic.Div>
    );
  }
}

PasswordFilterDetail.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool,
  permissions: PropTypes.arrayOf(PropTypes.string)
};
PasswordFilterDetail.defaultProps = {
  permissions: null
};
