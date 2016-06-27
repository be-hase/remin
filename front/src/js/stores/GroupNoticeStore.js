var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var AppConstants = require('../constants/AppConstants');
var assign = require('object-assign');

var _data = {};

var CHANGE_EVENT = 'change';

var GroupNoticeStore = assign({}, EventEmitter.prototype, {
    getGroupNotice: function(groupName) {
        if (!_data[groupName]) {
            return {
                mail: {
                    to: '',
                    from: ''
                },
                http: {
                    url: ''
                },
                invalid_end_time: '',
                items: [],
                notify_when_disconnected: false
            };
        }
        return _data[groupName];
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
        case AppConstants.GET_GROUP_NOTICE:
            _data[action.groupName] = action.data;
            GroupNoticeStore.emitChange();
            break;
        default:
            // no operation
    }
});

module.exports = GroupNoticeStore;
