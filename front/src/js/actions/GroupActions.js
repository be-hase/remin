var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppConstants = require('../constants/AppConstants');
var ApiUtils = require('../utils/ApiUtils');
var Utils = require('../utils/Utils');

var GroupActions = {
    getGroups: function(callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_GROUPS,
                data: data
            });
        });

        ApiUtils.Group.getGroups({full: true}, callbacks);
    },
    getGroup: function(groupName, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_GROUP,
                data: data
            });
        });

        ApiUtils.Group.getGroup(groupName, callbacks);
    },
    setGroup: function(data, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            GroupActions.getGroups();
        });

        ApiUtils.Group.setGroup(data, callbacks);
    },
    getNodeMetrics: function(groupName, data, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_NODE_METRICS,
                groupName: groupName,
                data: data
            });
        });

        ApiUtils.Group.getNodeMetrics(groupName, data, callbacks);
    },
    setNodeMetricsQuery: function(groupName, query) {
        AppDispatcher.dispatch({
            actionType: AppConstants.SET_NODE_METRICS_QUERY,
            groupName: groupName,
            data: query
        });
    },
    setNodeMetricsQueryOnlyAutoRefresh: function(groupName, autoRefresh) {
        AppDispatcher.dispatch({
            actionType: AppConstants.SET_NODE_METRICS_QUERY_ONLY_AUTO_REFRESH,
            groupName: groupName,
            autoRefresh: autoRefresh
        });
    },
    getGroupNotice: function(groupName, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_GROUP_NOTICE,
                groupName: groupName,
                data: data
            });
        });

        ApiUtils.Group.getGroupNotice(groupName, callbacks);
    },
    setGroupNotice: function(groupName, data, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_GROUP_NOTICE,
                groupName: groupName,
                data: data
            });
        });

        ApiUtils.Group.setGroupNotice(groupName, data, callbacks);
    },
    deleteGroup: function(groupName, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            GroupActions.getGroups();
        });

        ApiUtils.Group.deleteGroup(groupName, callbacks);
    },
    deleteGroupNode: function(groupName, hostAndPort, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_GROUP,
                data: data
            });
        });

        ApiUtils.Group.deleteGroupNode(groupName, hostAndPort, callbacks);
    },
    addGroupNodes: function(groupName, data, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            AppDispatcher.dispatch({
                actionType: AppConstants.GET_GROUP,
                data: data
            });
        });

        ApiUtils.Group.addGroupNodes(groupName, data, callbacks);
    },
    changeGroupName: function(groupName, data, callbacks) {
        callbacks = callbacks || {};

        Utils.wrapSuccess(callbacks, function(data) {
            GroupActions.getGroups();
        });

        ApiUtils.Group.changeGroupName(groupName, data, callbacks);
    }
};

module.exports = GroupActions;
