package com.behase.remin.util;

import org.apache.commons.lang3.StringUtils;

import com.behase.remin.exception.InvalidParameterException;

public class ValidationUtils {
	private ValidationUtils() {
	}

	private static final String GROUP_NAME_REGEX = "^[a-zA-Z0-9_-]{1,50}$";
	private static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{4,20}$";

	public static void groupName(String name) {
		if (!name.matches(GROUP_NAME_REGEX)) {
			throw new InvalidParameterException(String.format("groupName is invalid. (%s)", name));
		}
	}

	public static void hostAndPort(String node) {
		String[] hostAndPortArray = StringUtils.split(node, ":");
		if (hostAndPortArray.length != 2) {
			throw new InvalidParameterException(String.format("Node is invalid. (%s)", node));
		}
		try {
			Integer.valueOf(hostAndPortArray[1]);
		} catch (Exception e) {
			throw new InvalidParameterException(String.format("Node's port is invalid. (%s)", hostAndPortArray[1]));
		}
	}

	public static void hostAndPortRange(String node) {
		String[] hostAndPortRangeArray = StringUtils.split(node, ":");
		if (hostAndPortRangeArray.length != 2) {
			throw new InvalidParameterException(String.format("Node is invalid. (%s)", node));
		}
		String[] portRange = StringUtils.split(hostAndPortRangeArray[1], "-");
		for (String port : portRange) {
			try {
				Integer.valueOf(port);
			} catch (Exception e) {
				throw new InvalidParameterException(String.format("Node's port is invalid. (%s)", port));
			}
		}
	}

	public static void notBlank(String str, String paramName) {
		if (StringUtils.isBlank(str)) {
			throw new InvalidParameterException(String.format("%s must be not blank.", paramName));
		}
	}

	public static void numeric(String str, String paramName) {
		if (!StringUtils.isNumeric(str)) {
			throw new InvalidParameterException(String.format("%s must be numeric. (%s)", paramName, str));
		}
	}

	public static void number(String str, String paramName) {
		try {
			Integer.valueOf(str);
		} catch (Exception e) {
			throw new InvalidParameterException(String.format("%s must be number. (%s)", paramName, str));
		}
	}

	public static void username(String username) {
		if (!username.matches(USERNAME_REGEX)) {
			throw new InvalidParameterException(String.format("User ID is invalid. (%s)", username));
		}
	}

}
