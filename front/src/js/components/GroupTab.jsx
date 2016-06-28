var React = require('react');
var Router = require('react-router');
var classSet = React.addons.classSet;

var GroupStore = require('../stores/GroupStore');

var tabs = [
    {
        name: 'Monitoring',
        link: 'monitoring'
    },
    {
        name: 'Slow log',
        link: 'slowlog'
    },
    {
        name: 'Info',
        link: 'info'
    },
    {
        name: 'Setting',
        link: 'setting'
    }
];

var GroupTab = React.createClass({
    mixins: [Router.State],
    render: function() {
        var paths = this.getPathname().split('/');
        var tab = paths[3] || 'monitoring';
        var groupName = this.props.group.group_name;
        var list = [];

        _.each(tabs, function(val){
            var liClass = classSet({
                'active': val.link === tab
            });
            var href = '#/group/' + groupName;
            if (val.link !== 'monitoring') {
                href += '/' + val.link;
            }
            if (val.link === 'slowlog') {
                href += '/1';
            }

            list.push(
                <li key={val.link} className={liClass}><a href={href}>{val.name}</a></li>
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
