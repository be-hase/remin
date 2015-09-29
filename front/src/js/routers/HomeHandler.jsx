var React = require('react');
var Router = require('react-router');

var ApiUtils = require('../utils/ApiUtils');
var Utils = require('../utils/Utils');

var GroupStore = require('../stores/GroupStore');
var GroupActions = require('../actions/GroupActions');

var HomeHandler = React.createClass({
    mixins: [Router.State, Router.Navigation],
    componentDidMount: function() {
        Utils.pageChangeInit();
    },
    componentWillMount: function() {
        var groups = GroupStore.getGroups();

        if (groups && !_.isEmpty(groups)) {
            this.transitionTo('/group/' + groups[0].group_name);
            return;
        }
    },
    render: function() {
        return (
            <div>Group is not registered.</div>
        );
    }
});

module.exports = HomeHandler;
