package com.behase.remin.controller;

import com.behase.remin.config.SchedulerConfig;
import com.behase.remin.model.LoginUser;
import com.behase.remin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebController {
    @Autowired
    UserService userService;

    @Value("${build.number}")
    private String buildNumber;

    @Value("${scheduler.collectStaticsInfoIntervalMillis:"
            + SchedulerConfig.DEFAULT_COLLECT_STATICS_INFO_INTERVAL_MILLIS + "}")
    private String collectStaticsInfoIntervalMillis;

    @Value("${auth.enabled}")
    private boolean authEnabled;

    @Value("${auth.allowAnonymous}")
    private boolean authAllowAnonymous;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getClusterList(
            Authentication authentication,
            Model model
    ) throws Exception {
        model.addAttribute("buildNumber", buildNumber);
        model.addAttribute("collectStaticsInfoIntervalMillis", collectStaticsInfoIntervalMillis);
        model.addAttribute("authEnabled", authEnabled);
        model.addAttribute("authAllowAnonymous", authAllowAnonymous);
        if (authEnabled && authentication != null) {
            LoginUser loginUser = userService.getUser(authentication.getName());
            model.addAttribute("loginUsername", loginUser.getUsername());
            model.addAttribute("login", true);
        } else {
            model.addAttribute("loginUsername", "");
            model.addAttribute("login", false);
        }
        return "/index";
    }
}
