var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');
var _ = require('lodash');
var $ = require('jquery');
var moment = require('moment');

var AppConstants = require('../constants/AppConstants');
var GroupStore = require('../stores/GroupStore');

var _data = {};

var PREFIX = "remin.monitoring.query.";
var CHANGE_EVENT = 'change';

var defaultNodeMetricsNames = [
    'used_memory', 'used_memory_rss', 'used_memory_peak', 'mem_fragmentation_ratio',
    'total_connections_received', 'total_commands_processed', 'instantaneous_ops_per_sec',
    'keyspace_hits', 'keyspace_misses',
].join(',');

function getFromData(groupName) {
    var query = _data[groupName];

    if (!query) {
        return false;
    }

    return query;
}

function getFromLocalStorage(groupName) {
    if (!window.localStorage) {
        return false;
    }

    var savedStr = window.localStorage.getItem(PREFIX + groupName);
    try {
        var saved = $.parseJSON(savedStr);
        saved.start = false;
        saved.end = false;

        return saved;
    } catch (e) {
        return false;
    }
}

function filterHostAndPorts(groupName, query) {
    var group = GroupStore.getGroup(groupName);
    var hostAndPortsArray = [];
    _.each(query.hostAndPorts.split(','), function(hostAndPort) {
        var index = _.findIndex(group.nodes, function(node) {
            return node.host_and_port === hostAndPort;
        });
        if (index >= 0) {
            hostAndPortsArray.push(hostAndPort);
        }
    });

    if (hostAndPortsArray.length === 0) {
        query.hostAndPorts = getDefault(groupName).hostAndPorts;
        return;
    }

    query.hostAndPorts = hostAndPortsArray.join(',');
}

function setLocalStorage(groupName, query) {
    if (!window.localStorage) {
        return;
    }

    try {
        var savedStr = JSON.stringify(query);
        window.localStorage.setItem(PREFIX + groupName, savedStr);
    } catch (e) {
    }
}

function getDefault(groupName) {
    var group = GroupStore.getGroup(groupName);
    var hostAndPortsArray = _.map(group.nodes, function(node) {
        return node.host_and_port;
    });

    var query = {
        start: false,
        end: false,
        hostAndPorts: hostAndPortsArray.join(','),
        fields: defaultNodeMetricsNames,
        autoRefresh: true
    };

    return query;
}

var NodeMetricsQueryStore = assign({}, EventEmitter.prototype, {
    getNodeMetricsQuery: function(groupName) {
        var query = getFromData(groupName);
        if (!query) {
            query = getFromLocalStorage(groupName);
        }
        if (!query) {
            query = getDefault(groupName);
        }

        filterHostAndPorts(groupName, query);

        _data[groupName] = query;
        return query;
    },
    getDefaultNodeMetricsNames: function() {
        return defaultNodeMetricsNames;
    },
    emitChange: function() {
        this.emit(CHANGE_EVENT);
    },
    addChangeListener: function(callback) {
        this.on(CHANGE_EVENT, callback);
    },
    removeChangeListener: function(callback) {
        this.removeListener(CHANGE_EVENT, callback);
    }
});

AppDispatcher.register(function(action) {
    var query;
    switch(action.actionType) {
        case AppConstants.SET_NODE_METRICS_QUERY:
            query = NodeMetricsQueryStore.getNodeMetricsQuery(action.groupName);
            query = _.assign(query, action.data);

            _data[action.groupName] = query;
            setLocalStorage(action.groupName, _data[action.groupName]);
            NodeMetricsQueryStore.emitChange();
            break;
        case AppConstants.SET_NODE_METRICS_QUERY_ONLY_AUTO_REFRESH:
            query = NodeMetricsQueryStore.getNodeMetricsQuery(action.groupName);
            query.autoRefresh = action.autoRefresh;

            _data[action.groupName] = query;
            setLocalStorage(action.groupName, _data[action.groupName]);
            break;
        default:
            // no operation
    }
});

module.exports = NodeMetricsQueryStore;
