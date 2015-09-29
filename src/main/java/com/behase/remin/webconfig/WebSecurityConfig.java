package com.behase.remin.webconfig;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

import com.behase.remin.model.Role;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private UserDetailsService userDetailsService;

	@Value("${auth.enabled}")
	private boolean authEnabled;

	@Value("${auth.allowAnonymous}")
	private boolean authAllowAnonymous;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().disable();

		if (authEnabled) {
			http
				.rememberMe()
				.tokenRepository(new InMemoryTokenRepositoryImpl())
				.tokenValiditySeconds(Integer.MAX_VALUE).and()

				.authorizeRequests()
				.antMatchers("/login", "/css/**", "/js/**", "/img/**", "**/favicon.ico", "/vendor/**")
				.permitAll()
				.antMatchers(HttpMethod.POST, "/api/group/*", "/api/group/*/add-nodes", "/api/group/*/*/delete", "/api/group/*/delete", "/api/group/*/notice")
				.hasAuthority(Role.REMIN_ADMIN.getAuthority())
				.antMatchers(HttpMethod.POST, "/api/user/**")
				.hasAuthority(Role.REMIN_ADMIN.getAuthority())
				.and()

				.formLogin()
				.loginPage("/login")
				.defaultSuccessUrl("/")
				.and()

				.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/")
				.and()

				.exceptionHandling()
				.accessDeniedHandler((request, response, accessDeniedException) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN))
				.defaultAuthenticationEntryPointFor(
					(request, response, authException) -> response.setStatus(HttpServletResponse.SC_UNAUTHORIZED),
					new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest")
				);

			if (!authAllowAnonymous) {
				http
					.authorizeRequests()
					.anyRequest()
					.authenticated();
			}
		}

	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(new StandardPasswordEncoder());
	}
}
