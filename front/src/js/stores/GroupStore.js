var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var AppConstants = require('../constants/AppConstants');
var assign = require('object-assign');
var _ = require('lodash');

var _data = [];

var CHANGE_EVENT = 'change';

var GroupStore = assign({}, EventEmitter.prototype, {
    getGroups: function() {
        return _data;
    },
    getGroup: function(groupName) {
        return _.find(_data, function(val) {
            return val.group_name === groupName;
        });
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
    switch(action.actionType) {
        case AppConstants.GET_GROUPS:
            _data = action.data;
            GroupStore.emitChange();
            break;
        case AppConstants.GET_GROUP:
            var group = action.data;

            var index = _.findIndex(_data, function(val) {
                return val.group_name === group.group_name;
            });

            if (index >= 0) {
                _data[index] = group;
                GroupStore.emitChange();
            }
            break;
        default:
            // no operation
    }
});

module.exports = GroupStore;
