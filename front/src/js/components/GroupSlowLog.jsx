var React = require('react');
var Router = require('react-router');

var GroupSlowLogPager = require('../components/GroupSlowLogPager');
var GroupSlowLogGraph = require('../components/GroupSlowLogGraph');
var GroupSlowLogTable = require('../components/GroupSlowLogTable');

var GroupActions = require('../actions/GroupActions');
var GroupSlowLogStore = require('../stores/GroupSlowLogStore');
var Utils = require('../utils/Utils');

var GroupSlowLog = React.createClass({
    mixins: [Router.State],
    getInitialState: function() {
        return {
            slowLog: GroupSlowLogStore.getSlowLog(this.props.group.group_name, this.props.pageNo)
        };
    },
    componentDidMount: function() {
        GroupSlowLogStore.addChangeListener(this.handleChangeGroupSlowLog);

        var limit = 1000;
        var offset = (this.props.pageNo - 1) * limit;
        GroupActions.getSlowLog(this.props.group.group_name, {
            offset: offset,
            limit: limit
        });
    },
    componentWillUnmount: function() {
        GroupSlowLogStore.removeChangeListener(this.handleChangeGroupSlowLog);
    },
    render: function() {
        if (!this.state.slowLog) {
            return (
                <div></div>
            );
        }

        if (this.state.slowLog.data.length === 0) {
            return (
                <div className="well">
                    No slow log.
                </div>
            );
        }

        return (
            <div className="group-slowlog-components">
                <GroupSlowLogPager group={this.props.group} slowLog={this.state.slowLog} pageNo={this.props.pageNo} />
                <GroupSlowLogGraph group={this.props.group} slowLog={this.state.slowLog} />
                <GroupSlowLogTable group={this.props.group} slowLog={this.state.slowLog} />
            </div>
        );
    },
    handleChangeGroupSlowLog: function() {
        this.setState({
            slowLog: GroupSlowLogStore.getSlowLog(this.props.group.group_name, this.props.pageNo)
        });
    }
});

module.exports = GroupSlowLog;
