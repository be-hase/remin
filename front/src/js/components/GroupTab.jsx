var React = require('react');
var Router = require('react-router');
var classSet = React.addons.classSet;

var GroupStore = require('../stores/GroupStore');

var GroupTab = React.createClass({
    mixins: [Router.State],
    render: function() {
        var _this = this;
        var paths = this.getPathname().split('/');
        var tab = paths[3] || 'monitoring';
        var groupName = this.props.group.group_name;
        var list = [];

        _.each(['Monitoring', 'Info', 'Setting'], function(val){
            var lowerVal = val.toLowerCase();
            var liClass = classSet({
                'active': lowerVal === tab
            });
            var href = '#/group/' + groupName;
            if (lowerVal !== 'monitoring') {
                href += '/' + lowerVal;
            }

            list.push(
                <li key={val} className={liClass}><a href={href}>{val}</a></li>
            );
        });
        return (
            <div className="group-tab-components">
                <h2><span className="glyphicon glyphicon-menu-right"></span> {groupName}</h2>
                <ul className="nav nav-tabs">
                    {list}
                </ul>
            </div>
        );
    }
});

module.exports = GroupTab;
