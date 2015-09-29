var React = require('react');
var Router = require('react-router');

var GroupStore = require('../stores/GroupStore');
var UserStore = require('../stores/UserStore');
var GroupActions = require('../actions/GroupActions');

var HeaderSidebar = React.createClass({
    mixins: [Router.State],
    getInitialState: function() {
        return {
            groups: GroupStore.getGroups()
        };
    },
    componentDidMount: function() {
        GroupStore.addChangeListener(this.onChangeHandle);
    },
    componentWillUnmount: function() {
        GroupStore.removeChangeListener(this.onChangeHandle);
    },
    render: function() {
        var groupNames = [];
        var currentGroupName = this.getParams().groupName;
        var me = UserStore.getMe();

        _.each(this.getGroupLinks(), function(val){
            var href = "#group/" + val.groupName;
            var liClassName = '';

            if (val.groupName === currentGroupName) {
                liClassName = 'selected';
            }

            groupNames.push(
                <li key={val.groupName} className={liClassName}><a href={href}><span className="group-name">{val.groupName}</span></a></li>
            );
        });
        if (groupNames.length === 0) {
            groupNames.push(
                <li key="no-group"><span>Group is not registered.</span></li>
            );
        }

        var registAndCreateGroupMenuView;
        if (!AUTH_ENABLED || me.role === 'REMIN_ADMIN') {
            registAndCreateGroupMenuView = [
                (<li><a href="#" data-toggle="modal" data-target=".regist-group-modal-components">Regist group</a></li>),
            ];
        }

        var loginDropDownView;
        if (AUTH_ENABLED) {
            if (USER.login) {
                loginDropDownView = (
                    <li className="dropdown user-dropdown">
                        <a href="#" className="dropdown-toggle" data-toggle="dropdown"><i className="fa fa-user"></i> {USER.username}<b className="caret"></b></a>
                        <ul className="dropdown-menu">
                            <li><a href="#/change-profile"><i className="fa fa-gear"></i> Change profile</a></li>
                            <li className="divider"></li>
                            <li><a href="/logout"><i className="fa fa-power-off"></i> Logout</a></li>
                        </ul>
                    </li>
                );
            } else {
                loginDropDownView = (
                    <li><a href="/login">Login</a></li>
                );
            }
        }

        return (
            <nav className="navbar navbar-inverse navbar-fixed-top header-sidebar-components" role="navigation">
                <div className="navbar-header">
                    <button type="button" className="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                        <span className="icon-bar"></span>
                    </button>
                    <a className="navbar-brand" href="#">Remin</a>
                </div>
                <div className="collapse navbar-collapse navbar-ex1-collapse">
                    <ul className="nav navbar-nav side-nav">
                        <li><span><i className="glyphicon glyphicon-list"></i> <strong> Group list</strong></span></li>
                        {groupNames}
                    </ul>
                    <ul className="nav navbar-nav navbar-right dis-iframe">
                        {registAndCreateGroupMenuView}
                        <li><a href="#/users">Users</a></li>
                        {loginDropDownView}
                    </ul>
                </div>
            </nav>
        );
    },
    onChangeHandle: function() {
        this.setState({groups: GroupStore.getGroups()});
    },
    getGroupLinks: function() {
        var groups = this.state.groups;
        var groupLinks = [];

        _.each(groups, function(val){
            groupLinks.push({
                groupName: val.group_name
            });
        });

        return groupLinks;
    }
});

module.exports = HeaderSidebar;
