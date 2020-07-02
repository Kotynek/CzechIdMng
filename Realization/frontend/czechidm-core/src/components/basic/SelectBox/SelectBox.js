import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Select from 'react-select';
import Joi from 'joi';
import _ from 'lodash';
import Waypoint from 'react-waypoint';
//
import * as Utils from '../../../utils';
import Icon from '../Icon/Icon';
import Tooltip from '../Tooltip/Tooltip';
import OptionDecorator from './OptionDecorator';
import ValueDecorator from './ValueDecorator';
import FlashMessage from '../FlashMessages/FlashMessage';
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import EntityManager from '../../../redux/data/EntityManager';
import SearchParameters from '../../../domain/SearchParameters';

const NICE_LABEL = 'niceLabel';
const ITEM_FULL_KEY = 'itemFullKey';
const ITEM_VALUE = 'value';

/**
 * A Select control - async loads option from BE.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class SelectBox extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.getOptions = this.getOptions.bind(this);
    this.state = {
      ...this.state,
      options: [],
      error: null,
      actualPage: 0
    };
    // Timer identifier
    this.typingTimeoutId = null;
  }

  getComponentKey() {
    return 'component.basic.SelectBox';
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    super.UNSAFE_componentWillReceiveProps(nextProps);
    const { forceSearchParameters} = nextProps;
    if (!SearchParameters.is(forceSearchParameters, this.props.forceSearchParameters)) {
      this._initComponent(nextProps);
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
    this._initComponent(this.props);
  }

  /**
  * Defines if component works with complex value.
  * That is using for correct set input value in form component.
  * Complex value could be exist in _embedded map and we need to now if
  * should be used value from field (UUID) or _embedded (entity).
  *
  */
  isValueComplex() {
    return true;
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    // initialize value
    // We have to propagate actual forceSearchParameters (maybe from this.props, maybe from nextProps)
    const { useFirst, forceSearchParameters } = props;
    if (useFirst) {
      this.getOptions('', forceSearchParameters, true);
    }
  }

  getRequiredValidationSchema() {
    if (this.props.multiSelect === true) {
      return Joi.array().min(1).required();
    }
    return Joi.object().required();
  }

  /**
   * Merge hard and user deffined search parameters
   */
  _createSearchParameters(inputText, forceSearchParameters) {
    const { manager, pageSize, loadMoreContent } = this.props;
    // user input
    let searchParameters = manager.getDefaultSearchParameters().setFilter('text', inputText).setSize(pageSize || SearchParameters.getDefaultSize());
    if (manager.supportsAuthorization()) {
      searchParameters = searchParameters.setName(SearchParameters.NAME_AUTOCOMPLETE);
    }
    // hard filters
    let _forceSearchParameters = null;
    if (forceSearchParameters) {
      // we dont want override setted pagination
      _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null);
    }
    // if set loadMoreContent it is neccessary override page
    if (loadMoreContent && forceSearchParameters && forceSearchParameters.getPage()) {
      _forceSearchParameters = _forceSearchParameters.setPage(forceSearchParameters.getPage());
    }
    return manager.mergeSearchParameters(searchParameters, _forceSearchParameters);
  }

  getOptions(input, forceSearchParameters, useFirst = false, addToEnd = false) {
    const { manager, clearable, multiSelect, emptyOptionLabel, additionalOptions } = this.props;
    const { options } = this.state;
    const searchParameters = this._createSearchParameters(input, forceSearchParameters);
    const timeInMs = Date.now();
    // We create unique key for this call and save it to component state
    const uiKey = `${manager.getEntityType()}_${(timeInMs)}`;
    this.setState({
      isLoading: true,
      uiKeyCurrent: uiKey
    }, () => {
      this.context.store.dispatch(manager.fetchEntities(searchParameters, uiKey, (json, error, resultUiKey) => {
        if (!error) {
          // We confirm if is uiKey in result same as uiKey saved in component state
          // If is key different, then is result older and we don`t want him.
          if (resultUiKey !== this.state.uiKeyCurrent) {
            this.getLogger().debug(`[SelectBox]: Recieved data is too old, we will throw out data! [${resultUiKey}|${this.state.uiKeyCurrent}]`);
            this.setState({
              isLoading: false
            });
            return;
          }

          const result = json;
          let data = null;
          const results = result._embedded[manager.getCollectionType()];
          if (results) {
            for (const item in results) {
              if (!results.hasOwnProperty(item)) {
                continue;
              }
              this.itemRenderer(results[item], input);
              // use the first value
              if (this.state.value === null && useFirst) {
                this.onChange(results[item]);
              }
            }
            let finalOptions = result._embedded[manager.getCollectionType()];
            // if we want add options to end, create sum of options
            if (addToEnd) {
              // remove last options (info about size)
              const optionsWithoutInfoItem = _.dropRight(options);
              // final options is sum before options and current options
              finalOptions = _.concat(optionsWithoutInfoItem, result._embedded[manager.getCollectionType()]);
            }
            data = {
              options: finalOptions,
              complete: finalOptions.length >= result.page.totalElements,
            };
            //
            let addedCustomOptions = 0;
            if (additionalOptions && additionalOptions.length > 0) {
              addedCustomOptions += additionalOptions.length;
              // additional selecbox options
              if (result.page.number === 0) { // only once
                additionalOptions.forEach(additionalOption => {
                  data.options.unshift({
                    ...additionalOption,
                    _moreOption: true, // prevent to show icon
                    additionalOption: true
                  });
                });
              }
            }
            // add empty option at start
            if (clearable && !multiSelect && data.options.length > 0) {
              const emptyOption = this.getEmptyOption(emptyOptionLabel);
              if (emptyOption) {
                if (result.page.number === 0) { // only once
                  data.options.unshift(emptyOption);
                }
                addedCustomOptions += 1;
              }
            }
            if (!data.complete) {
              data.options.push({
                [NICE_LABEL]: (
                  <Waypoint
                    onEnter={ this._loadMoreContent.bind(this, input) }>
                    {
                      this.i18n('results', {
                        escape: false,
                        count: data.options.length - addedCustomOptions,
                        total: result.page.totalElements
                      })
                    }
                  </Waypoint>
                ),
                [ITEM_FULL_KEY]: input,
                _moreOption: true,
                _disabled: true, // prevent to enable this option by not disableable select box
                disabled: true // info only
              });
            } else {
              data.options.push({
                [NICE_LABEL]: this.i18n('results', {
                  escape: false,
                  count: data.options.length - addedCustomOptions,
                  total: result.page.totalElements
                }),
                [ITEM_FULL_KEY]: input,
                _moreOption: true,
                _disabled: true, // prevent to enable this option by not disableable select box
                disabled: true // info only
              });
            }
          }
          this.setState({
            options: data.options,
            isLoading: false
          });
        } else {
          this.setState({
            isLoading: false,
            error
          });
        }
      }));
    });
  }

  /**
   * Empty option for clearable selects.
   *
   * @param  {string} custom label text
   * @return {option}
   * @since 10.1.0
   */
  getEmptyOption(emptyOptionLabel) {
    if (emptyOptionLabel === false) {
      // option will not be shown
      return null;
    }
    //
    const label = emptyOptionLabel || this.i18n('emptyOption.label', { defaultValue: '-- not selected --' });
    //
    return {
      [NICE_LABEL]: label,
      [ITEM_FULL_KEY]: label,
      [ITEM_VALUE]: null,
      _clearable: true,
      _moreOption: true // prevent to show icon
    };
  }

  /**
   * Method load more content to options state and increment actualPage
   */
  _loadMoreContent(input = '') {
    const { actualPage } = this.state;
    const { pageSize, manager, useFirst, loadMoreContent, forceSearchParameters } = this.props;
    if (!loadMoreContent) {
      return;
    }
    const finalPageSize = pageSize || SearchParameters.getDefaultSize();
    // increment actualPage
    const newActualPage = actualPage + 1;
    // if exists force search parameters merge it
    let finalSearchParameters = manager.getDefaultSearchParameters().setSize(finalPageSize).setPage(newActualPage);
    if (forceSearchParameters) {
      finalSearchParameters = manager.mergeSearchParameters(forceSearchParameters, finalSearchParameters);
    }
    //
    this.getOptions(input, finalSearchParameters, useFirst, true);
    this.setState({
      actualPage: newActualPage
    });
  }

  getValue() {
    const { returnProperty, multiSelect } = this.props;
    const { value, error } = this.state;
    //
    if (error && error.statusCode !== 404) {
      // select box is not rendered
      // FIXME: show text field with filled value instead? The same as in code lists?
      return undefined;
    }
    //
    if (!value) {
      if (multiSelect === true) {
        return [];
      }
      return null;
    }
    // value is array ... multiselect
    if (value instanceof Array && multiSelect === true) {
      const copyValues = [];
      for (const item of value) {
        const copyValue = this._deletePrivateField(_.merge({}, item));
        if (returnProperty) {
          copyValues.push(copyValue[returnProperty]);
        } else {
          copyValues.push(copyValue);
        }
      }
      return copyValues;
    }
    // value is not array
    const copyValue = _.merge({}, value);
    this._deletePrivateField(copyValue);
    //
    if (value && value.additionalOption) {
      return copyValue;
    }
    if (returnProperty) { // result property value - if value is false, then whole object is returned
      return copyValue[returnProperty];
    }
    return copyValue;
  }

  _deletePrivateField(item) {
    if (item.hasOwnProperty(NICE_LABEL)) {
      delete item[NICE_LABEL];
    }
    if (item.hasOwnProperty(ITEM_FULL_KEY)) {
      delete item[ITEM_FULL_KEY];
    }
    return item;
  }

  isValid() {
    const { isLoading } = this.state;
    if (isLoading === true) {
      // value still loading ... component is invalid
      return false;
    }
    return super.isValid();
  }

  normalizeValue(value) {
    const { manager, label } = this.props;
    //
    if (value) {
      // value is array ... multiselect
      if (value instanceof Array && this.props.multiSelect === true) {
        if (_.isEmpty(value)) {
          // nothing
        } else {
          let isError = false;
          const renderedValues = [];
          //
          for (const item of _.clone(value)) {
            if (item instanceof Object && !item.itemFullKey) {
              // value is object but doesn't have itemFullKey attribute
              this.itemRenderer(item, '');
            } else if (manager && ((typeof item === 'string') || (typeof item === 'number'))) {
              // value is string, we try load entity by id
              // TODO: wait for another loading get the entity
              // if (!manager.isShowLoading(this.context.store.getState(), null, item)) {
              this.setState({
                isLoading: true
                /* eslint-disable no-loop-func */
              }, () => {
                this.context.store.dispatch(manager.autocompleteEntityIfNeeded(item, null, (json, error) => {
                  if (!error) {
                    this.itemRenderer(json, '');
                    // add item to array
                    renderedValues.push(json);
                    if (renderedValues.length === value.length) { // last callback
                      if (!isError) {
                        this.setState({
                          value: renderedValues,
                          isLoading: false
                        }, this.validate);
                      } else {
                        this.setState({
                          value: null,
                          isLoading: false
                        });
                      }
                    }
                  } else {
                    isError = true;
                    renderedValues.push(item);
                    this.setState({
                      isLoading: false,
                      error,
                      showValidationError: true,
                      validationResult: {
                        status: 'error',
                        class: 'has-error has-feedback',
                        isValid: false,
                        message: this.i18n('validationError.invalid.base')
                      }
                    });
                  }
                }));
              });
              // }
            }
          }
        }
      } else if (value instanceof Object && !value.itemFullKey) {
        // value is object but doesn't have itemFullKey attribute
        this.itemRenderer(value, '');
        this.setState({isLoading: false});
      } else if (typeof value === 'string' || typeof value === 'number') {
        // value is string, we try load entity by id
        // TODO: wait for another loading get the entity
        // if (manager && !manager.isShowLoading(this.context.store.getState(), null, value)) {
        this.setState({
          isLoading: false
        }, () => {
          this.context.store.dispatch(manager.autocompleteEntityIfNeeded(value, null, (json, error) => {
            if (!error) {
              if (!json) {
                this.setState({
                  value: null,
                  isLoading: false,
                  error: {
                    title: label,
                    level: 'warning',
                    message: this.i18n('security.record.notFound')
                  }
                });
              } else {
                this.itemRenderer(json, '');
                this.setState({ value: json, isLoading: false }, this.validate);
              }
            } else {
              this.setState({
                value: null,
                isLoading: false,
                error
              });
            }
          }));
        });
        // }
      }
    }
    return value;
  }

  getFiltersDefiniton(input) {
    const filters = [];
    for (const field of this.props.searchInFields) {
      filters.push({
        field,
        value: input
      });
    }
    return filters;
  }

  onChange(value) {
    // handle empty option before custom handler is called
    if (value && value._clearable) {
      // null option
      value = null;
    }
    //
    let result = true;
    if (this.props.onChange) {
      result = this.props.onChange(value);
    }
    // if onChange listener returns false, then we can end
    if (result === false) {
      return;
    }
    //
    this.setState({
      value,
      error: null // clear previous error
    }, () => {
      this.validate();
    });
  }

  focus() {
    if (this.refs.selectComponent) {
      this.refs.selectComponent.focus();
    }
  }

  itemRenderer(item, input) {
    const { niceLabel } = this.props;

    let _niceLabel;
    if (niceLabel) {
      _niceLabel = niceLabel(item);
    } else {
      _niceLabel = this.props.manager.getNiceLabel(item);
    }

    const inputLower = input.toLowerCase();

    let itemFullKey = _niceLabel;
    if (inputLower) {
      if (_niceLabel !== null && !_niceLabel.toLowerCase().indexOf(inputLower) >= 0) {
        for (const field of this.props.searchInFields) {
          if (!Utils.Ui.isEmpty(item[field]) && item[field].toLowerCase().indexOf(inputLower) >= 0) {
            itemFullKey = `${ itemFullKey } (${ item[field] })`;
            continue;
          }
        }
      }
    }

    _.merge(item, {[NICE_LABEL]: _niceLabel, [ITEM_FULL_KEY]: itemFullKey});
  }

  onInputChange(value) {
    // Clears the previously set timer.
    clearTimeout(this.typingTimeoutId);

    // after change input set actualPage to zero, we want start from begin
    this.setState({
      actualPage: 0
    });

    // Reset the timer, to make the http call of the search after 200ms
    // We need this for IE11 and for prevent redundant call of search
    this.typingTimeoutId = setTimeout(() => {
      this.getOptions(value, this.props.forceSearchParameters);
      this.typingTimeoutId = null;
    }, 200);

    return value;
  }

  getBody(feedback) {
    const { labelSpan, label, componentSpan, required } = this.props;
    const { value, disabled, isLoading, error } = this.state;
    //
    const labelClassName = classNames(labelSpan, 'control-label');
    //
    let showAsterix = false;
    if (required && !value) {
      showAsterix = true;
    }
    //
    return (
      <div className={ showAsterix ? 'has-feedback' : '' }>
        {
          !label
          ||
          <label
            className={ labelClassName }>
            { label }
            { this.renderHelpIcon() }
          </label>
        }
        <div className={ componentSpan }>
          {
            (disabled === true && this.props.multiSelect !== true)
            ?
            <div ref="selectComponent" className="form-text">
              { value ? value[this.props.fieldLabel] : null }
              <Icon type="fa" icon="refresh" className="icon-loading" rendered={ isLoading === true } showLoading/>
            </div>
            :
            <Tooltip ref="popover" placement={ this.getTitlePlacement() } value={ this.getTitle() }>
              <span>
                {
                  !error
                  ||
                  <FlashMessage message={ this.flashMessagesManager.convertFromError(error) } className="no-margin" />
                }
                { this.getSelectComponent() }
                {
                  feedback != null
                  ?
                  feedback
                  :
                  <span>
                    {
                      showAsterix
                      ?
                      <span className="form-control-feedback" style={{ color: 'red', zIndex: 0 }}>*</span>
                      :
                      ''
                    }
                  </span>
                }
              </span>
            </Tooltip>
          }
          { this.props.children }
          { !label ? this.renderHelpIcon() : null }
          { this.renderHelpBlock() }
        </div>
      </div>
    );
  }

  getPlaceholder(placeholder) {
    if (placeholder !== null && placeholder !== undefined) {
      return placeholder;
    }
    // default placeholder
    return this.i18n('label.searchSelect');
  }

  /**
   * Load first page on input is opened
   */
  onOpen() {
    // OnOpen function is called for first change in the input.
    // This call invokes redundant search. We skip that call (if some typing timeout exists)
    if (this.typingTimeoutId) {
      return;
    }
    // it is neccessary set actualPage to zero and remove all options, after open
    this.setState({
      actualPage: 0,
      options: []
    });
    // loads default first page.
    this.getOptions('', this.props.forceSearchParameters);
  }

  getSelectComponent() {
    const {
      placeholder,
      fieldLabel,
      multiSelect,
      clearable,
      showLoading,
      optionComponent,
      valueComponent,
      disableable
    } = this.props;
    const {
      isLoading,
      options,
      readOnly,
      disabled,
      value
    } = this.state;
    //
    // from new version react-select is necessary turn off onBlurResetsInput and closeOnSelect
    // onBlurResetsInput made problems with submit form and focus
    return (
      <Select
        ref="selectComponent"
        isLoading={ isLoading || showLoading}
        value={value}
        onChange={ this.onChange }
        disabled={readOnly || disabled}
        ignoreCase
        ignoreAccents={false}
        multi={ multiSelect }
        valueKey={ ITEM_FULL_KEY }
        labelKey={ fieldLabel }
        onBlurResetsInput={ false }
        closeOnSelect={ !multiSelect }
        onSelectResetsInput={ false }
        noResultsText={this.i18n('component.basic.SelectBox.noResultsText')}
        placeholder={this.getPlaceholder(placeholder)}
        searchingText={this.i18n('component.basic.SelectBox.searchingText')}
        searchPromptText={this.i18n('component.basic.SelectBox.searchPromptText')}
        clearable={clearable}
        onInputChange={this.onInputChange.bind(this)}
        options={ options.map(option => {
          const _option = _.clone(option);
          // turn off disabled feature => @todo: react-select 2.x has this feature outofbox
          if (!disableable && !_option._disabled) {
            _option._disabled = _option.disabled; // original ino private prop
            _option.disabled = false;
          }
          //
          return _option;
        })}
        onOpen={ this.onOpen.bind(this) }
        optionComponent={ optionComponent }
        valueComponent={ valueComponent }/>
    );
  }
}

SelectBox.propTypes = {
  ...AbstractFormComponent.propTypes,
  placeholder: PropTypes.string,
  manager: PropTypes.instanceOf(EntityManager).isRequired,
  searchInFields: PropTypes.arrayOf(PropTypes.string).isRequired,
  fieldLabel: PropTypes.string,
  value: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.object),
    PropTypes.object,
    PropTypes.arrayOf(PropTypes.string),
    PropTypes.string
  ]),
  /**
   * The component is in multi select mode
   */
  multiSelect: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
  * If object is selected, then this property value will be returned. If value is false, then whole object is returned.
  */
  returnProperty: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool
  ]),
  /**
   * Selected options can be cleared
   */
  clearable: PropTypes.bool,
  /**
   * Function with transform label in select box
   */
  niceLabel: PropTypes.func,
  /**
   * Use the first searched value, if value is empty
   */
  useFirst: PropTypes.bool,
  /**
   * Search results page size
   * @see SearchParameters.getDefaultSize()
   * @see SearchParameters.MAX_SIZE
   */
  pageSize: PropTypes.number,
  /**
   * Boolean flag that call load automaticaly more content after reach last item in options
   */
  loadMoreContent: PropTypes.bool,
  /**
   * Option decorator - generalize OptionDecorator
   */
  optionComponent: PropTypes.func,
  /**
   * Value decorator - generalize ValueDecorator
   */
  valueComponent: PropTypes.func,
  /**
   * If disabled option can be selected.
   */
  disableable: PropTypes.bool,
  /**
   * Empty option label (text). Default emptyOption.label localization.
   *
   * false = empty option will not be shown
   */
  emptyOptionLabel: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool
  ]),
  /**
   * Additional select box options - extend options wit custom behavior.
   */
  additionalOptions: PropTypes.arrayOf(PropTypes.object)
};

SelectBox.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  fieldLabel: NICE_LABEL,
  multiSelect: false,
  returnProperty: 'id',
  searchInFields: [],
  clearable: true,
  useFirst: false,
  pageSize: SearchParameters.getDefaultSize(),
  loadMoreContent: true,
  optionComponent: OptionDecorator,
  valueComponent: ValueDecorator,
  disableable: true,
  additionalOptions: []
};

SelectBox.NICE_LABEL = NICE_LABEL;
SelectBox.ITEM_FULL_KEY = ITEM_FULL_KEY;
SelectBox.ITEM_VALUE = ITEM_VALUE;
//
SelectBox.OptionDecorator = OptionDecorator;
SelectBox.ValueDecorator = ValueDecorator;


export default SelectBox;
