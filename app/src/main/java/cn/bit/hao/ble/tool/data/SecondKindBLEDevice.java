/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data;

/**
 * @author wuhao on 2016/7/15
 */
public class SecondKindBLEDevice extends BLEDevice {

	protected static final int FIELD_CHANGED_BASE = BLEDevice.RESPONSE_CODE_BASE + 0x00200000;

	private static final String DEFAULT_NAME = "Main2ActivityData";

	protected int daughterValue;

	public static final int DAUGHTER_VALUE_CHANGED = FIELD_CHANGED_BASE + 0x001;

	public SecondKindBLEDevice() {
		super.value = 0;
		super.friendlyName = DEFAULT_NAME;
		this.daughterValue = 2;
	}

	public int getDaughterValue() {
		return daughterValue;
	}

	public void setDaughterValue(int daughterValue) {
		this.daughterValue = daughterValue;
	}
}
