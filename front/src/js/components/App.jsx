var React = require('react');
var Router = require('react-router');
var RouteHandler = Router.RouteHandler;
var NotificationSystem = require('react-notification-system');
var Spinner = require('react-spinkit');
var $ = require('jquery');

var HeaderSidebar = require('./HeaderSidebar');
var RegistGroupModal = require('./RegistGroupModal');

var AppStore = require('../stores/AppStore');
var UserStore = require('../stores/UserStore');

var App = React.createClass({
    componentDidMount: function() {
        AppStore.addAlertListener(this.onAlertHandle);
    },
    componentWillUnmount: function() {
        AppStore.removeAlertListener(this.onAlertHandle);
    },
    render: function() {
        var registGroupModalView;
        var me = UserStore.getMe();

        if (!AUTH_ENABLED || me.role === 'REMIN_ADMIN') {
            registGroupModalView = (<RegistGroupModal />);
        }
        return (
            <div>
                <div id="wrapper">
                    <HeaderSidebar />
                    <div id="page-wrapper">
                        <RouteHandler/>
                    </div>
                </div>
                {registGroupModalView}
                <NotificationSystem ref="notificationSystem" />
                <div className="global-loading">
                    <Spinner spinnerName='three-bounce' ref="spinner" />
                </div>
            </div>
        );
    },
    onAlertHandle: function() {
        this.refs.notificationSystem.addNotification(AppStore.getAlert());
    }
});

module.exports = App;
