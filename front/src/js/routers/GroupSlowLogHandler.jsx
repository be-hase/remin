var React = require('react');
var Router = require('react-router');
var $ = require('jquery');

var GroupSlowLog = require('../components/GroupSlowLog');
var GroupStore = require('../stores/GroupStore');
var NoGroupRender = require('../mixins/NoGroupRender');
var GroupTab = require('../components/GroupTab');
var Utils = require('../utils/Utils');

var GroupSlowLogHandler = React.createClass({
    mixins: [Router.State, NoGroupRender],
    componentDidMount: function() {
        Utils.pageChangeInit();
    },
    render: function() {
        var group = GroupStore.getGroup(this.getParams().groupName);
        var pageNo = this.getParams().pageNo;
        if (!pageNo || !$.isNumeric(pageNo) || pageNo <= 0) {
            pageNo = 1;
        }
        var key = 'group-slowlog-' + this.getParams().groupName + '-' + pageNo;

        if (group) {
            return (
                <div key={key}>
                    <GroupTab group={group} />
                    <GroupSlowLog group={group} pageNo={pageNo} />
                </div>
            );
        } else {
            return this.renderNoGroup(this.props.params.groupName, key);
        }
    }
});

module.exports = GroupSlowLogHandler;
