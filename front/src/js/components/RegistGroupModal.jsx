var React = require('react/addons');
var ValidationMixin = require('react-validation-mixin');
var Joi = require('joi');
var $ = require('jquery');

var GroupActions = require('../actions/GroupActions');
var Utils = require('../utils/Utils');

var ValidationRender = require('../mixins/ValidationRender');

var RegisterGroupModal = React.createClass({
    mixins: [ValidationMixin, ValidationRender, React.addons.LinkedStateMixin],
    validatorTypes: {
        groupName: Joi.string().max(50).regex(/^[a-zA-Z0-9_-]+$/).trim().required().label('Group name'),
        hostAndPorts: Joi.string().trim().required().label('Host and ports'),
        password: Joi.string().allow('').trim().label('Password')
    },
    getInitialState: function() {
        return {
            groupName: '',
            hostAndPorts: '',
            password: ''
        };
    },
    render: function() {
        return (
            <div className="modal regist-group-modal-components" ref="modal">
                <div className="modal-dialog">
                    <div className="modal-content">
                        <div className="modal-header">
                            <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                            <h4 className="modal-title">Regist group</h4>
                        </div>
                        <div className="modal-body">
                            <div className={this.getClasses('groupName')}>
                                <label>Group name (alphabet, numeric, -, _)</label>
                                <input type='text' valueLink={this.linkState('groupName')} onBlur={this.handleValidation('groupName')} className='form-control' placeholder='Group name' />
                                {this.getValidationMessages('groupName').map(this.renderHelpText)}
                            </div>
                            <div className={this.getClasses('hostAndPorts')}>
                                <label>Host and ports(host:port). Comma reparated. And allow using range-format by '-'.</label>
                                <input type='text' valueLink={this.linkState('hostAndPorts')} onBlur={this.handleValidation('hostAndPorts')} className='form-control' placeholder='127.0.0.0:1000,127.0.0.0:2000-2004' />
                                {this.getValidationMessages('hostAndPorts').map(this.renderHelpText)}
                            </div>
                            <div className={this.getClasses('password')}>
                                <label>Password</label>
                                <input type='password' valueLink={this.linkState('password')} onBlur={this.handleValidation('password')} className='form-control' placeholder='Password' />
                                {this.getValidationMessages('password').map(this.renderHelpText)}
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-default" data-dismiss="modal">Close</button>
                            <button className="btn btn-primary" onClick={this.handleSubmit}>OK</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    },
    handleSubmit: function(event) {
        var _this = this;
        var $btn = $(event.currentTarget);

        event.preventDefault();
        var onValidate = function(error, validationErrors) {
            if (error) {
                return;
            }

            Utils.loading(true);
            $btn.button('loading');
            GroupActions.setGroup(
                {
                    groupName: _.trim(_this.state.groupName),
                    hostAndPorts: _.trim(_this.state.hostAndPorts),
                    password: _.trim(_this.state.password)
                },
                {
                    success: function() {
                        if (_this.isMounted()) {
                            _this.setState(_this.getInitialState());
                            _this.closeModal();
                        }
                        Utils.showAlert({
                            message: 'Registed successfully',
                            level: 'success'
                        });
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
    closeModal: function() {
        $(React.findDOMNode(this.refs.modal)).modal('hide');
    }
});

module.exports = RegisterGroupModal;
