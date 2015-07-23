package com.behase.remin.exception;

import org.springframework.http.HttpStatus;

import com.behase.remin.Constants;

public class InvalidParameterException extends ApiException {
	private static final long serialVersionUID = -6416726302671334601L;

	public InvalidParameterException(String message) {
		super(Constants.ERR_CODE_INVALID_PARAMETER, String.format("Invalid parameter. %s", message), HttpStatus.BAD_REQUEST);
	}
}
