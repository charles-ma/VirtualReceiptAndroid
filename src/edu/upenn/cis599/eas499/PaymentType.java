/**
 * Copyright 2012 Annie Lee. All Rights Reserved.
 */

package edu.upenn.cis599.eas499;

import java.util.HashMap;
import java.util.Map;

public enum PaymentType {
	CASH (0, "Cash"), CREDIT (1, "Credit"), DEBIT (2, "Debit");
	
	private final int value;
	private final String text;
	private static final Map<Integer, PaymentType> lookup = new HashMap<Integer, PaymentType>();
	
	static {
		for (PaymentType p : PaymentType.values()) {
			lookup.put(p.getValue(), p);
		}
	}
	
	PaymentType(int value, String text) {
		this.value = value;
		this.text = text;
	}
	
	public int getValue() {
		return value;
	}
	
	public String getText() {
		return text;
	}
	
	public static PaymentType get(int value) {
		return lookup.get(value);
	}
	
	
}
