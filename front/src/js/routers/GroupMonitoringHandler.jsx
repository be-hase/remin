var React = require('react');
var Router = require('react-router');

var GroupStore = require('../stores/GroupStore');
var NodeMetricsQueryStore = require('../stores/NodeMetricsQueryStore');
var NoGroupRender = require('../mixins/NoGroupRender');
var GroupTab = require('../components/GroupTab');
var GroupMonitoring = require('../components/GroupMonitoring');
var GroupMonitoringQuery = require('../components/GroupMonitoringQuery');
var Utils = require('../utils/Utils');

var GroupMonitoringHandler = React.createClass({
    mixins: [Router.State, NoGroupRender],
    componentDidMount: function() {
        Utils.pageChangeInit();
    },
    render: function() {
        var group = GroupStore.getGroup(this.getParams().groupName);
        var key = 'group-monitoring-' + this.getParams().groupName;

        if (group) {
            return (
                <div key={key}>
                    <GroupTab group={group} />
                    <GroupMonitoringQuery group={group} />
                    <GroupMonitoring group={group} />
                </div>
            );
        } else {
            return this.renderNoGroup(this.props.params.groupName, key);
        }
    }
});

module.exports = GroupMonitoringHandler;
