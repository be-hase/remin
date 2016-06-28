var React = require('react');
var Router = require('react-router');
var Route = Router.Route;
var DefaultRoute = Router.DefaultRoute;
var NotFoundRoute = Router.NotFoundRoute;

var App = require('../components/App');
var HomeHandler = require('./HomeHandler');
var NotFoundHandler = require('./NotFoundHandler');
var GroupInfoHandler = require('./GroupInfoHandler');
var GroupMonitoringHandler = require('./GroupMonitoringHandler');
var GroupSlowLogHandler = require('./GroupSlowLogHandler');
var GroupSettingHandler = require('./GroupSettingHandler');
var ChangeProfileHandler = require('./ChangeProfileHandler');
var UsersHandler = require('./UsersHandler');

var appRoutes = (
    <Route path="/" handler={App}>
        <DefaultRoute name="home" handler={HomeHandler} />
        <NotFoundRoute handler={NotFoundHandler} />
        <Route name="group-monitoring" path="/group/:groupName" handler={GroupMonitoringHandler} />
        <Route name="group-slowlog" path="/group/:groupName/slowlog/:pageNo" handler={GroupSlowLogHandler} />
        <Route name="group-info" path="/group/:groupName/info" handler={GroupInfoHandler} />
        <Route name="group-setting" path="/group/:groupName/setting" handler={GroupSettingHandler} />
        <Route name="change-profile" path="/change-profile" handler={ChangeProfileHandler} />
        <Route name="users" path="/users" handler={UsersHandler} />
    </Route>
);

module.exports = appRoutes;
