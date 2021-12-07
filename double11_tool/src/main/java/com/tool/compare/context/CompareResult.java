package com.tool.compare.context;

public enum CompareResult {
	/**
	 * 相等
	 */
	EQUAL(1),
	
	/***
	 * 不等
	 */
	NOT_EQUAL(2),
	
	/***
	 * 左边不存在
	 */
	LEFT_NOT_EXIST(3),
	
	/**
	 * 右边不存在
	 */
	RIGHT_NOT_EXIST(4);

	private int type;

	CompareResult(int type) {
		this.type = type;
	}
}
