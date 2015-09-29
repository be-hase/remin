var React = require('react');
var Router = require('react-router');
var classSet = React.addons.classSet;
var $ = require('jquery');
var _ = require('lodash');
var Joi = require('joi');
var Select = require('react-select');
var numeral = require('numeral');

var ValidationRender = require('../mixins/ValidationRender');
var GroupActions = require('../actions/GroupActions');
var Utils = require('../utils/Utils');
var RedisConstants = require('../constants/RedisConstants');

var UserStore = require('../stores/UserStore');

var OPERATORS = [
    {value: 'eq', label: ' == '},
    {value: 'ne', label: ' != '},
    {value: 'gt', label: ' > '},
    {value: 'ge', label: ' >= '},
    {value: 'lt', label: ' < '},
    {value: 'le', label: ' <= '}
];

function getOperatorByValue(value) {
    return _.find(OPERATORS, function(val) {
        return val.value === value;
    });
}

function getNotificationCondition(noticeItem, pretty) {
    var name = noticeItem.metrics_name;
    var operator = getOperatorByValue(noticeItem.operator);
    var value;
    if (pretty && noticeItem.value_type === 'number') {
        value = numeral(noticeItem.value).format('0,0.[00000000]');
    } else {
        value = '"' + noticeItem.value + '"';
    }
    return name + operator.label + value;
}

var GroupSettingThreshold = React.createClass({
    mixins: [React.addons.LinkedStateMixin],
    validatorTypes: {
        valueNumber: Joi.number().required().label('Value'),
        valueString: Joi.string().max(255).trim().required().label('Value')
    },
    getInitialState: function () {
        return {
            metricsName: false,
            operator: false,
            valueType: false,
            value: '',
            notifyWhenDisconnected: this.props.notice.notify_when_disconnected,
            items: this.props.notice.items,
            newItems: []
        };
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({
            notifyWhenDisconnected: nextProps.notice.notify_when_disconnected,
            items: nextProps.notice.items
        });
    },
    componentDidMount: function() {
        $(React.findDOMNode(this)).find('[data-toggle="tooltip"]').tooltip();
    },
    componentDidUpdate: function() {
        $(React.findDOMNode(this)).find('[data-toggle="tooltip"]').tooltip();
    },
    render: function() {
        var _this = this;
        var me = UserStore.getMe();
        var hasPermission = !AUTH_ENABLED || me.role === 'REMIN_ADMIN';

        var addThresholdBtnView;
        var saveBtnView;
        var addThresholdModalView;
        if (hasPermission) {
            addThresholdBtnView = (
                <button className="btn btn-default btn-xs" onClick={this.handleClickOpenAddThresholdModal}>
                    Add threshold
                </button>
            );

            saveBtnView = (
                <div className="modal-footer">
                    <button className="btn btn-primary" onClick={this.handleClickSaveBtn}>Save</button>
                </div>
            );

            var metricsList = RedisConstants.metrics;

            var metricsNameOptions = _.map(metricsList, function(val) {
                return  {value: val.name, label: val.name};
            });

            var metricsDesc = '';
            if (this.state.metricsName) {
                metricsDesc = Utils.getRedisMetricsByName(this.state.metricsName).desc;
            }

            var opetatorOptions;
            if (this.state.metricsName) {
                if (this.state.valueType === 'string') {
                    opetatorOptions = [
                        {value: 'eq', label: ' == '},
                        {value: 'ne', label: ' != '}
                    ];
                } else {
                    opetatorOptions = [
                        {value: 'eq', label: ' == '},
                        {value: 'ne', label: ' != '},
                        {value: 'gt', label: ' > '},
                        {value: 'ge', label: ' >= '},
                        {value: 'lt', label: ' < '},
                        {value: 'le', label: ' <= '}
                    ];
                }
            } else {
                opetatorOptions = [];
            }

            var valueLabel = this.state.valueType === 'string' ? 'Value (string, case-insentive)' : 'Value (number)';

            var expression = <div></div>;
            if (this.state.metricsName && this.state.operator && this.state.value) {
                var conditionPretty = getNotificationCondition({
                    metrics_name: this.state.metricsName,
                    operator: this.state.operator,
                    value: this.state.value,
                    value_type: this.state.valueType
                }, true);
                expression = (
                    <div className="well well-sm result-notification-condition">
                        <p><u><strong>Result : notification condition</strong></u></p>
                        <pre>{conditionPretty}</pre>
                    </div>
                );
            }

            addThresholdModalView = (
                <div className="modal add-threshold-modal" ref="add-threshold-modal">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <h4 className="modal-title">Add threshold</h4>
                            </div>
                            <div className="modal-body">
                                <div className="form-group">
                                    <label>Metcis name</label>
                                    <Select name="metrics-name" value={this.state.metricsName} options={metricsNameOptions} clearable={false} onChange={this.handleChangeMetricsName}/>
                                    <p className="help-block"><small>{metricsDesc}</small></p>
                                </div>
                                <div style={{display: this.state.metricsName ? 'block' : 'none'}}>
                                    <div className="form-group">
                                        <label>Operator</label>
                                        <Select name="operator" value={this.state.operator} options={opetatorOptions} clearable={false} onChange={this.handleChangeOperator}/>
                                    </div>
                                    <div className="form-group">
                                        <label>{valueLabel}</label>
                                        <input type='text' valueLink={this.linkState('value')} className='form-control' placeholder='Enter value' />
                                    </div>
                                    {expression}
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-default" data-dismiss="modal">Close</button>
                                <button className="btn btn-primary" onClick={this.handleClickAddThreshold}>OK</button>
                            </div>
                        </div>
                    </div>
                </div>
            );
        }

        var items = this.state.items.concat(this.state.newItems);
        var itemViews;
        if (items.length === 0) {
            itemViews = (
                <div>
                    Not exists.
                </div>
            );
        } else {
            itemViews = this.renderTable();
        }

        var notifyWhenDisconnectedCheckbox;
        if (hasPermission) {
            notifyWhenDisconnectedCheckbox = (
                <label>
                    <input type="checkbox" checkedLink={this.linkState('notifyWhenDisconnected')} /> Notify when node is disconnected.
                </label>
            );
        } else {
            notifyWhenDisconnectedCheckbox = (
                <label>
                    <input type="checkbox" checkedLink={this.linkState('notifyWhenDisconnected')} disabled /> Notify when node is disconnected.
                </label>
            );
        }

        return (
            <div className="group-setting-threshold-components">
                <div className="panel panel-default">
                    <div className="panel-heading">
                        Setting of notification condition
                    </div>
                    <div className="panel-body">
                        <div className="node-status-wrap">
                            <h4>Node status</h4>
                            <div className="checkbox">
                                {notifyWhenDisconnectedCheckbox}
                            </div>
                        </div>
                        <div>
                            <h4 className="clearfix">
                                Metrics threshold
                                <div className="pull-right">
                                    {addThresholdBtnView}
                                </div>
                            </h4>
                            {itemViews}
                        </div>
                        {saveBtnView}
                    </div>
                </div>
                {addThresholdModalView}
            </div>
        );
    },
    renderTable: function() {
        var _this = this;
        var me = UserStore.getMe();
        var hasPermission = !AUTH_ENABLED || me.role === 'REMIN_ADMIN';

        var items = this.state.items.concat(this.state.newItems);
        var list = _.map(items, function(val) {
            var operator = getOperatorByValue(val.operator);
            var condition = getNotificationCondition(val);
            var conditionPretty = getNotificationCondition(val, true);

            var tooltipTitle = Utils.getRedisMetricsByName(val.metrics_name).desc;

            var deleteBtnView;
            if (hasPermission) {
                deleteBtnView = (
                    <button className="btn btn-default btn-xs" onClick={function(event) { _this.handleClickItemDelete(event, val); }}><span>&times;</span></button>
                );
            }

            return (
                <tr key={condition} className="">
                    <td><span title={tooltipTitle} data-toggle="tooltip" data-placement="top">{val.metrics_name}</span></td>
                    <td>{operator.label}</td>
                    <td>{val.value}</td>
                    <td><pre>{conditionPretty}</pre></td>
                    <td className="text-right delete-threshold">
                        {deleteBtnView}
                    </td>
                </tr>
            );
        });

        return (
            <div className="table-responsive">
                <table className="table table-striped">
                    <thead>
                        <tr>
                            <th>Metrics name</th>
                            <th>Operator</th>
                            <th>Value</th>
                            <th>Notification condition</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {list}
                    </tbody>
                </table>
            </div>
        );
    },
    handleChangeMetricsName: function(val) {
        var preInfo = Utils.getRedisMetricsByName(this.state.metricsName) || {};
        var afterInfo = Utils.getRedisMetricsByName(val) || {};

        if (preInfo.valueType === afterInfo.valueType) {
            this.setState({
                metricsName: val || false,
                valueType: afterInfo.valueType
            });
        } else {
            this.setState({
                metricsName: val || false,
                operator: false,
                valueType: afterInfo.valueType,
                value: ''
            });
        }
    },
    handleChangeOperator: function(val) {
        this.setState({
            operator: val || false
        });
    },
    handleClickSaveBtn: function(event) {
        var _this = this;
        var $btn = $(event.currentTarget);

        var data = _.assign({}, _this.props.notice, {
            notify_when_disconnected: _this.state.notifyWhenDisconnected,
            items: _this.state.items.concat(_this.state.newItems)
        });



        $btn.button('loading');
        GroupActions.setGroupNotice(
            _this.props.group.group_name,
            {
                notice: JSON.stringify(data)
            },
            {
                success: function() {
                    Utils.showAlert({
                        message: 'Saved successfully',
                        level: 'success'
                    });
                    if (_this.isMounted()) {
                        _this.setState({
                            newItems: []
                        });
                    }
                },
                complete: function() {
                    if (_this.isMounted()) {
                        $btn.button('reset');
                    }
                }
            }
        );
    },
    handleClickOpenAddThresholdModal: function(event) {
        event.preventDefault();
        this.showAddThresholdModal();
    },
    handleClickAddThreshold: function(event) {
        event.preventDefault();

        if (!this.state.metricsName) {
            Utils.showAlert({message: 'Select metrics name.', level: 'error'});
            return;
        }
        if (!this.state.operator) {
            Utils.showAlert({message: 'Select operator.', level: 'error'});
            return;
        }

        var validateResult;
        if (this.state.valueType === 'number') {
            validateResult = Joi.validate({valueNumber: this.state.value}, {valueNumber: this.validatorTypes.valueNumber});
            if (validateResult.error) {
                Utils.showAlert({message: validateResult.error.details[0].message, level: 'error'});
                return;
            }
        } else {
            validateResult = Joi.validate({valueString: this.state.value}, {valueString: this.validatorTypes.valueString});
            if (validateResult.error) {
                Utils.showAlert({message: validateResult.error.details[0].message, level: 'error'});
                return;
            }
        }

        var newItem = {
            metrics_name: this.state.metricsName,
            operator: this.state.operator,
            value_type: this.state.valueType,
            value: _.trim(this.state.value)
        };

        var existsItems = this.state.items.concat(this.state.newItems);
        if (_.findIndex(existsItems, _.matches(newItem)) >= 0) {
            Utils.showAlert({message: 'Already exists same condition.', level: 'error'});
            return;
        }

        this.setState({
            metricsName: false,
            operator: false,
            valueType: false,
            value: '',
            newItems: [].concat(this.state.newItems).concat(newItem)
        });
        this.hideAddThresholdModal();
    },
    showAddThresholdModal: function() {
        $(React.findDOMNode(this.refs['add-threshold-modal'])).modal('show');
    },
    hideAddThresholdModal: function() {
        $(React.findDOMNode(this.refs['add-threshold-modal'])).modal('hide');
    },
    handleClickItemDelete: function(event, item) {
        event.preventDefault();
        var matches = _.matches(item);
        var items = _.reject(this.state.items, matches);
        var newItems = _.reject(this.state.newItems, matches);
        this.setState({
            items: items,
            newItems: newItems
        });
    }
});

module.exports = GroupSettingThreshold;
