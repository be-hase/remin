var React = require('react/addons');
var ValidationMixin = require('react-validation-mixin');
var Joi = require('joi');
var Router = require('react-router');
var classSet = React.addons.classSet;
var $ = require('jquery');
var _ = require('lodash');

var GroupActions = require('../actions/GroupActions');
var Utils = require('../utils/Utils');
var ValidationRender = require('../mixins/ValidationRender');
var UserStore = require('../stores/UserStore');

var GroupSettingChangeName = React.createClass({
    mixins: [ValidationMixin, ValidationRender, React.addons.LinkedStateMixin, Router.Navigation],
    validatorTypes: {
        newGroupName: Joi.string().max(50).regex(/^[a-zA-Z0-9_-]+$/).trim().required().label('Group name')
    },
    getInitialState: function() {
        return {
            newGroupName: ''
        };
    },
    render: function() {
        var me = UserStore.getMe();
        var hasPermission = !AUTH_ENABLED || me.role === 'RELUMIN_ADMIN';

        if (!hasPermission) {
            return (
                <div />
            );
        }

        return (
            <div className="group-setting-change-name-components">
                <div className="panel panel-default">
                    <div className="panel-heading clearfix">
                        Change group name
                    </div>
                    <div className="panel-body">
                        <button className="btn btn-default" data-toggle="modal" data-target=".change-group-name-modal">Change group name</button>
                    </div>
                </div>
                <div className="modal change-group-name-modal" ref="change-group-name-modal">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <h4 className="modal-title">Change group name</h4>
                            </div>
                            <div className="modal-body">
                                <div className={this.getClasses('newGroupName')}>
                                    <label>New group name (alphabet, numeric, -, _)</label>
                                    <input type='text' valueLink={this.linkState('newGroupName')} onBlur={this.handleValidation('newGroupName')} className='form-control' placeholder='New group name' />
                                    {this.getValidationMessages('newGroupName').map(this.renderHelpText)}
                                </div>
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-default" data-dismiss="modal">Close</button>
                                <button className="btn btn-primary" onClick={this.handleClickOk}>OK</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    },
    handleClickOk: function(event) {
        var _this = this;
        var $btn = $(event.currentTarget);

        event.preventDefault();
        var onValidate = function(error, validationErrors) {
            if (error) {
                return;
            }

            var newGroupName = _.trim(_this.state.newGroupName);
            Utils.loading(true);
            $btn.button('loading');
            GroupActions.changeGroupName(
                _this.props.group.group_name,
                {
                    newGroupName: newGroupName,
                },
                {
                    success: function() {
                        if (_this.isMounted()) {
                            _this.hideModal();
                        }
                        Utils.showAlert({
                            message: 'Change group name successfully',
                            level: 'success'
                        });
                        //_this.transitionTo('/group/' + newGroupName);
                    },
                    complete: function() {
                        Utils.loading(false);
                        if (_this.isMounted()) {
                            $btn.button('reset');
                        }
                    }
                }
            );
        };
        this.validate(onValidate);
    },
    hideModal: function() {
        $(React.findDOMNode(this.refs['change-group-name-modal'])).modal('hide');
    }
});

module.exports = GroupSettingChangeName;
