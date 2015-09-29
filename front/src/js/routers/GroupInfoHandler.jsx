var React = require('react');
var Router = require('react-router');

var GroupStore = require('../stores/GroupStore');
var NoGroupRender = require('../mixins/NoGroupRender');
var GroupTab = require('../components/GroupTab');
var GroupInfo = require('../components/GroupInfo');
var Utils = require('../utils/Utils');

var GroupInfoHandler = React.createClass({
    mixins: [Router.State, NoGroupRender],
    componentDidMount: function() {
        Utils.pageChangeInit();
        GroupStore.addChangeListener(this.onChangeHandle);
    },
    componentWillUnmount: function() {
        GroupStore.removeChangeListener(this.onChangeHandle);
    },
    render: function() {
        var group = GroupStore.getGroup(this.getParams().groupName);
        var key = 'group-info-' + this.getParams().groupName;

        if (group) {
            return (
                <div key={key}>
                    <GroupTab group={group} />
                    <GroupInfo group={group} />
                </div>
            );
        } else {
            return this.renderNoGroup(this.getParams().groupName, key);
        }
    },
    onChangeHandle: function() {
        this.forceUpdate();
    }
});

module.exports = GroupInfoHandler;
