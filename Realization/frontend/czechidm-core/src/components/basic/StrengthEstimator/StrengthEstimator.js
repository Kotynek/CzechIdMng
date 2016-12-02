import React, { PropTypes } from 'react';
import zxcvbn from 'zxcvbn';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import Tooltip from '../Tooltip/Tooltip';
import Icon from '../Icon/Icon';

const INFO_WEAK = 'weak';
const INFO_OKAY = 'okay';
const INFO_STRONG = 'strong';
const INFO_GREAT = 'great';

/**
 * Component for strength estimator, with string validation from zxcvbn
 */
class StrengthEstimator extends AbstractFormComponent {

  constructor(props) {
    super(props);
    const { initialStrength } = this.props;
    this.state = {
      strength: initialStrength,
      strengthStyle: 'danger',
      info: 'weak'
    };
  }

  componentWillReceiveProps(nextProps) {
    const { value } = nextProps;
    this._getStrength(value);
  }

  _getStrength(value) {
    if (value !== null) {
      const { max } = this.props;
      const response = zxcvbn(value);
      let strengthStyle = 'danger';
      if (response.score > 3) {
        strengthStyle = 'success';
      } else if (response.score > 1) {
        strengthStyle = 'warning';
      }
      const strength = response.score;
      let width;
      if (strength !== 0) {
        width = (100 / (max / strength));
      } else {
        width = 0;
      }

      let info;
      let background;
      if (width >= 75) {
        info = INFO_GREAT;
        background = 'green';
      } else if (width >= 50) {
        info = INFO_STRONG;
        background = 'yellow';
      } else if (width >= 25) {
        info = INFO_OKAY;
        background = 'orange';
      } else {
        info = INFO_WEAK;
        background = 'red';
      }

      this.setState({
        strength,
        strengthStyle,
        width,
        info, background
      });
    }
  }

  render() {
    const {
      spanClassName, isTooltip,
      triggerForTooltip, isIcon,
      placementForTooltip, opacity,
      icon, tooltip } = this.props;

    const { background, info, width } = this.state;

    return (
      <Tooltip
        trigger={triggerForTooltip}
        ref="popover"
        placement={placementForTooltip}
        value={this.i18n(tooltip)}
        rendered={isTooltip} >
        <span className={spanClassName} style={{ opacity }}>
        <Icon icon={icon} showLoading={false} rendered={isIcon} />
            <span className="strength-estimator" style={{ width: width + '%', background }}>
            </span>
          {this.i18n('content.password.strength.' + info)}
        </span>
      </Tooltip>
    );
  }
}

StrengthEstimator.propTypes = {
  spanClassName: PropTypes.string,
  triggerForTooltip: PropTypes.array,
  placementForTooltip: PropTypes.string,
  tooltip: PropTypes.string,
  max: PropTypes.number.isRequired,
  icon: PropTypes.string,
  isIcon: PropTypes.bool,
  isTooltip: PropTypes.bool,
  initialStrength: PropTypes.number,
  opacity: PropTypes.number,
  value: PropTypes.string
};

StrengthEstimator.defaultProps = {
  spanClassName: 'col-sm-offset-3 col-sm-8',
  triggerForTooltip: ['hover'],
  tooltip: 'content.password.change.passwordChangeTooltip',
  placementForTooltip: 'right',
  initialStrength: 0,
  isIcon: true,
  isTooltip: true,
  opacity: 0.54,
  icon: 'lock',
  value: null
};


export default StrengthEstimator;
