package com.example.demo.domain.model;

import lombok.Data;

@Data
public class Employee {
	
	private Integer id;
	private String name;
	private Integer age;
	private Integer gender;
	private String genderString;

	/** 性別の文字列を数値に変換 */
	public void convertGenderStringToInt() {
		// 文字列を数値に変換
		if("男性".equals(genderString)) {
			gender = 1;
		} else if ("女性".equals(genderString)) {
			gender = 2;
		} else {
			String errorMsg = "Gender string is invalid: " + genderString;
			throw new IllegalStateException(errorMsg);
		}
	}
}
