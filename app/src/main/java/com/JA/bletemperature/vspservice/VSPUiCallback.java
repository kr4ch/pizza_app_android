package com.JA.bletemperature.vspservice;

import android.bluetooth.BluetoothGatt;

import com.JA.bletemperature.gap.LairdGapUiCallback;

public interface VSPUiCallback extends LairdGapUiCallback{
    /**
     * called when the VSP service is found
     * @param gatt
     */
    public void uiServiceVSPFound(
            final BluetoothGatt gatt);
    /**
     * called when the VSP service is not found
     * @param gatt
     */
    public void uiServiceVSPNotFound(
            final BluetoothGatt gatt);
    /**
     * called when the VSP chars are found
     * @param gatt
     */
    public void uiModuleVSPCharsFound(
            final BluetoothGatt gatt);
    /**
     * called when the VSP chars are not found
     * @param gatt
     */
    public void uiModuleVSPCharsNotFound(
            final BluetoothGatt gatt);
}