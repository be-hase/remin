package com.behase.remin.model;

import org.springframework.security.core.GrantedAuthority;

import com.behase.remin.exception.InvalidParameterException;

public class Role implements GrantedAuthority {
	private static final long serialVersionUID = -4168971547041673977L;

	private String authority;

	private Role(String authority) {
		this.authority = authority;
	}

	@Override
	public String getAuthority() {
		return authority;
	}

	public static final Role VIEWER = new Role("VIEWER");
	public static final Role REMIN_ADMIN = new Role("REMIN_ADMIN");

	public static Role get(String role) {
		switch (role) {
			case "VIEWER":
				return VIEWER;
			case "REMIN_ADMIN":
				return REMIN_ADMIN;
			default:
		}
		throw new InvalidParameterException(String.format("'%s' is invalid role.", role));
	}
}
