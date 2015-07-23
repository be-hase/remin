package com.behase.remin.model;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.Getter;

@Data
public class NoticeItem {
	private String metricsName;
	private String operator;
	private String valueType;
	private String value;

	public static enum NoticeValueType {
		NUMBER("number"),
		STRING("string");

		@Getter
		private String value;

		private NoticeValueType(String value) {
			this.value = value;
		}

		public static NoticeValueType getNoticeValueType(String value) {
			return Arrays.stream(values()).filter(v -> {
				return StringUtils.equalsIgnoreCase(v.value, value);
			}).findFirst().orElse(null);
		}
	}

	public static enum NoticeOperator {
		EQ("eq", " == "),
		NE("ne", " != "),
		GT("gt", " > "),
		GE("ge", " >= "),
		LT("lt", " < "),
		LE("le", " <= ");

		@Getter
		private String value;
		@Getter
		private String label;

		private NoticeOperator(String value, String label) {
			this.value = value;
			this.label = label;
		}

		public static NoticeOperator getNoticeOperator(String value) {
			return Arrays.stream(values()).filter(v -> {
				return StringUtils.equalsIgnoreCase(v.value, value);
			}).findFirst().orElse(null);
		}
	}
}
