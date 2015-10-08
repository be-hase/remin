var React = require('react');
var Router = require('react-router');
var classSet = React.addons.classSet;
var $ = require('jquery');
var _ = require('lodash');

var GroupActions = require('../actions/GroupActions');
var Utils = require('../utils/Utils');

var UserStore = require('../stores/UserStore');

var GroupSettingDelete = React.createClass({
    render: function() {
        var me = UserStore.getMe();
        var hasPermission = !AUTH_ENABLED || me.role === 'REMIN_ADMIN';

        if (!hasPermission) {
            return (
                <div />
            );
        }

        return (
            <div className="group-setting-delete-components">
                <div className="panel panel-default">
                    <div className="panel-heading clearfix">
                        Unregist group
                    </div>
                    <div className="panel-body">
                        <button className="btn btn-danger" data-toggle="modal" data-target=".unregist-group-modal">Unregist this group.</button>
                    </div>
                </div>
                <div className="modal unregist-group-modal" ref="unregist-group-modal">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                                <h4 className="modal-title">Unregist this group</h4>
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
            </div>
        );
    },
    handleClickDelete: function(event) {
        var _this = this;
        var $btn = $(event.currentTarget);

        Utils.loading(true);
        $btn.button('loading');
        GroupActions.deleteGroup(
            this.props.group.group_name,
            {
                success: function() {
                    if (_this.isMounted()) {
                        _this.hideModal();
                    }
                    Utils.showAlert({
                        message: 'Unregisted successfully',
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
    },
    hideModal: function() {
        $(React.findDOMNode(this.refs['unregist-group-modal'])).modal('hide');
    }
});

module.exports = GroupSettingDelete;
