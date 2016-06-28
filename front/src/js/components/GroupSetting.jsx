var React = require('react');
var moment = require('moment');

var GroupActions = require('../actions/GroupActions');
var GroupNoticeStore = require('../stores/GroupNoticeStore');
var GroupSettingGeneral = require('../components/GroupSettingGeneral');
var GroupSettingThreshold = require('../components/GroupSettingThreshold');
var GroupSettingDelete = require('../components/GroupSettingDelete');
var GroupSettingChangeName = require('../components/GroupSettingChangeName');
var Utils = require('../utils/Utils');

var GroupSetting = React.createClass({
    getInitialState: function() {
        return {
            notice: GroupNoticeStore.getGroupNotice(this.props.group.group_name)
        };
    },
    componentDidMount: function() {
        var _this = this;

        GroupNoticeStore.addChangeListener(this.handleChangeGroupNotice);

        GroupActions.getGroupNotice(this.props.group.group_name);
    },
    componentWillUnmount: function() {
        GroupNoticeStore.removeChangeListener(this.handleChangeGroupNotice);
    },
    render: function() {
        return (
            <div className="group-setting-components">
                <GroupSettingGeneral group={this.props.group} notice={this.state.notice} />
                <GroupSettingThreshold group={this.props.group} notice={this.state.notice} />
                <GroupSettingChangeName group={this.props.group} />
                <GroupSettingDelete group={this.props.group} />
            </div>
        );
    },
    handleChangeGroupNotice: function() {
        this.setState({
            notice: GroupNoticeStore.getGroupNotice(this.props.group.group_name)
        });
    }
});

module.exports = GroupSetting;
