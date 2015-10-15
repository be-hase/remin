var $ = require('jquery');
require('bootstrap');
var React = require('react');
var Router = require('react-router');
var Highcharts = require('react-highcharts');

var appRoutes = require('./routers/AppRoutes');
var GroupActions = require('./actions/GroupActions');
var UserActions = require('./actions/UserActions');

$(function(){
    var getQuery = new URI().query(true);
    if (_.has(getQuery, 'iframe')) {
        // if access from iframe
        $('head').append('<style>.dis-iframe {display: none;}</style>');
        USER = {
            login: false,
            username: ''
        };
    }

    UserActions.getUsers();

    GroupActions.getGroups({
        complete: function(){
            Router.run(appRoutes, function(Root) {
                React.render(<Root />,  document.getElementById('app'));
            });

            setInterval(function(){
                GroupActions.getGroups();
                UserActions.getUsers();
            }, 1 * 60 * 1000);
        }
    });
});
