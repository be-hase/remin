var React = require('react');
var Router = require('react-router');
var classSet = React.addons.classSet;
var $ = require('jquery');
var _ = require('lodash');
var ValidationMixin = require('react-validation-mixin');
var Joi = require('joi');

var ValidationRender = require('../mixins/ValidationRender');
var GroupActions = require('../actions/GroupActions');
var UserStore = require('../stores/UserStore');

var GroupInfo = React.createClass({
    mixins: [ValidationMixin, ValidationRender, React.addons.LinkedStateMixin],
    validatorTypes: {
        hostAndPorts:  Joi.string().trim().required().label('Host and ports'),
        password: Joi.string().allow('').trim().label('Password')
    },
    getInitialState: function() {
        return {
            hostAndPorts: '',
            password: ''
        };
    },
    componentDidMount: function() {
        GroupActions.getGroup(this.props.group.group_name);
    },
    render: function() {
        var _this = this;
        var me = UserStore.getMe();
        var hasPermission = !AUTH_ENABLED || me.role === 'REMIN_ADMIN';

        var addNodeBtn = (<div />);
        var addNodeModal = (<div />);
        if (hasPermission) {
            addNodeBtn = (
                <button className="btn btn-default btn-xs" data-toggle="modal" data-target=".add-node-modal">Add node</button>
            );
            addNodeModal = (
                <div className="modal add-node-modal" ref="add-node-modal">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <h4 className="modal-title">Add node</h4>
                            </div>
                            <div className="modal-body">
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
        }

        var deleteNodeModal = (<div />);
        if (hasPermission) {
            deleteNodeModal = (
                <div className="modal delete-node-modal" ref="delete-node-modal">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <h4 className="modal-title">Delete node</h4>
                            </div>
                            <div className="modal-body">
                                Are you sure ?
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-default" data-dismiss="modal">Close</button>
                                <button className="btn btn-danger" onClick={this.handleClickDelete}>Delete !!</button>
                            </div>
                        </div>
                    </div>
                </div>
            );
        }

        // node item
        var nodeItemList = _.map(this.props.group.nodes, function(node) {
            var statusClassName = 'status label label-';
            if (node.connected) {
                statusClassName += 'success';
            } else {
                statusClassName += 'danger';
            }

            var connectedText = node.connected ? 'connected' : 'disconnected';

            var deleteNodeBtn;
            if (hasPermission && _this.props.group.nodes.length > 1) {
                deleteNodeBtn = (
                    <button className="btn btn-default btn-xs" onClick={function(event) { _this.handleClickShowDeleteModal(event, node.host_and_port); } }><span>&times;</span></button>
                );
            } else {
                deleteNodeBtn = (<div />);
            }

            return (
                <tr key={node.host_and_port}>
                    <td><span className={statusClassName}> </span><span className="status-text">{connectedText}</span></td>
                    <td>{node.host_and_port}</td>
                    <td>{node.password}</td>
                    <td>
                        {deleteNodeBtn}
                    </td>
                </tr>
            );
        });

        return (
            <div className="group-info-components">
                <div className="panel panel-default">
                    <div className="panel-heading clearfix">
                        Nodes
                        <div className="pull-right">
                            {addNodeBtn}
                        </div>
                    </div>

                    <div className="panel-body">
                        <div className="table-responsive">
                            <table className="table table-striped">
                                <thead>
                                    <tr>
                                        <th style={{width: '200px'}}>status</th>
                                        <th style={{width: '300px'}}>host:port</th>
                                        <th>password</th>
                                        <th style={{width: '50px'}}></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {nodeItemList}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {addNodeModal}
                {deleteNodeModal}
            </div>
        );
    },
    handleClickShowDeleteModal: function(event, hostAndPort) {
        event.preventDefault();

        var $modal = $(React.findDOMNode(this.refs['delete-node-modal']));
        $modal.attr('data-host-and-port', hostAndPort);
        $modal.modal('show');
    },
    handleClickDelete: function(event) {
        event.preventDefault();

        var _this = this;
        var $modal = $(React.findDOMNode(this.refs['delete-node-modal']));
        var hostAndPort = $modal.attr('data-host-and-port');
        var $btn = $(event.currentTarget);

        Utils.loading(true);
        $btn.button('loading');
        GroupActions.deleteGroupNode(
            _.trim(this.props.group.group_name),
            _.trim(hostAndPort),
            {
                success: function(data) {
                    Utils.showAlert({level: 'success', message: "Deleted node successfully."});
                    if (_this.isMounted()) {
                        $modal.modal('hide');
                    }
                },
                complete: function() {
                    Utils.loading(false);
                    if (_this.isMounted()) {
                        $btn.button('reset');
                    }
                }
            }
        );
    },
    handleSubmit: function(event) {
        event.preventDefault();

        var _this = this;
        var $btn = $(event.currentTarget);

        var onValidate = function(error, validationErrors) {
            if (error) {
                return;
            }

            Utils.loading(true);
            $btn.button('loading');
            GroupActions.addGroupNodes(
                _.trim(_this.props.group.group_name),
                {
                    hostAndPorts: _.trim(_this.state.hostAndPorts),
                    password: _.trim(_this.state.password)
                },
                {
                    success: function(data) {
                        Utils.showAlert({level: 'success', message: "Added nodes successfully."});
                        if (_this.isMounted()) {
                            _this.setState({
                                hostAndPorts: ''
                            });
                            $(React.findDOMNode(_this.refs['add-node-modal'])).modal('hide');
                        }
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
    }
});

module.exports = GroupInfo;
