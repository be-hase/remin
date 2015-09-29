var $ = require('jquery');
var _ = require('lodash');
var moment = require('moment');

var Utils = require('./Utils');

var BASE_URL;
if (__DEV__) {
    BASE_URL = 'http://localhost:8080';
} else {
    BASE_URL = '';
}

var ajaxPool = [];

function commonErrorHandle(jqXHR, textStatus) {
    var data;

    // abortのときはなにもエラー表示しない
    if (textStatus === 'abort') {
        return;
    }

    data = parseError(jqXHR);

    if (data && _.has(data, 'error') && _.has(data.error, 'message')) {
        Utils.showAlert({
            message: data.error.message,
            level: 'error'
        });
        return;
    }

    Utils.showAlert({
        message: 'Server error.',
        level: 'error'
    });
}

function parseError(jqXHR) {
    try {
        return $.parseJSON(jqXHR.responseText);
    } catch(e) {
        return false;
    }
}

var ApiUtils = {
    clearAllAjax: function() {
        ajaxPool = [];
    },
    abortAllAjax: function() {
        _.each(ajaxPool, function(value){
                value.abort();
        });
        ajaxPool = [];
    },
    addAjax: function(jqXHR) {
        ajaxPool.push(jqXHR);
    },
    completeAjax: function(jqXHR) {
        if (jqXHR) {
            ajaxPool = _.without(ajaxPool, jqXHR);
        }
    },
    Group: {
        getGroups: function(options, callbacks) {
            var apiUrl = BASE_URL + '/api/groups';
            if (options.full) {
                apiUrl += '?full=true';
            }

            var ajaxOptions = {
                type: 'GET',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        getGroup: function(groupName, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName;

            var ajaxOptions = {
                type: 'GET',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        setGroup: function(data, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + data.groupName;

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data: data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        getNodeMetrics: function(groupName, data, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName + '/metrics';

            if (!data.start) {
                data.start = moment().subtract(24, 'h').format('x');
            }
            if (!data.end) {
                data.end = moment().format('x');
            }

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data: data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        getGroupNotice: function(groupName, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName + '/notice';

            var ajaxOptions = {
                type: 'GET',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        setGroupNotice: function(groupName, data, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName + '/notice';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data: data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        deleteGroup: function(groupName, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName + '/delete';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        deleteGroupNode: function(groupName, hostAndPort, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName + '/' + hostAndPort + '/delete';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        addGroupNodes: function(groupName, data, callbacks) {
            var apiUrl = BASE_URL + '/api/group/' + groupName + '/add-nodes';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data: data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        }
    },
    User: {
        changePassword: function(data, callbacks) {
            var apiUrl = BASE_URL + '/api/me/change-password';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data : data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        updateMe: function(data, callbacks) {
            var apiUrl = BASE_URL + '/api/me/update';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data : data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        getUsers: function(callbacks) {
            var apiUrl = BASE_URL + '/api/users';

            var ajaxOptions = {
                type: 'GET',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        addUser: function(username, data, callbacks) {
            var apiUrl = BASE_URL + '/api/user/' + username;

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data : data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        updateUser: function(username, data, callbacks) {
            var apiUrl = BASE_URL + '/api/user/' + username + '/update';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl,
                data : data
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        },
        deleteUser: function(username, callbacks) {
            var apiUrl = BASE_URL + '/api/user/' + username + '/delete';

            var ajaxOptions = {
                type: 'POST',
                url: apiUrl
            };
            _.assign(ajaxOptions, callbacks);
            return $.ajax(ajaxOptions);
        }
    }
};

$.ajaxSetup({
    dataType: 'json',
    timeout: 10 * 60 * 1000,
    cache: false,
    error: function(jqXHR, textStatus) {
        if (jqXHR.status === 401) {
            location.href = "/login";
            return;
        }
        commonErrorHandle(jqXHR, textStatus);
    }
});

module.exports = ApiUtils;
