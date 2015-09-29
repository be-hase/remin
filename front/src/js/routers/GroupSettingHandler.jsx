var React = require('react');
var Router = require('react-router');

var GroupStore = require('../stores/GroupStore');
var NoGroupRender = require('../mixins/NoGroupRender');
var GroupTab = require('../components/GroupTab');
var GroupSetting = require('../components/GroupSetting');
var Utils = require('../utils/Utils');

var GroupSettingHandler = React.createClass({
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
        var key = 'group-setting-' + this.getParams().groupName;

        if (group) {
            return (
                <div key={key}>
                    <GroupTab group={group} />
                    <GroupSetting group={group} />
                </div>
            );
        } else {
            return this.renderNoGroup(this.props.params.groupName, key);
        }
    },
    onChangeHandle: function() {
        this.forceUpdate();
    }
});

module.exports = GroupSettingHandler;
