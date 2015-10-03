package com.JA.bletemperature.gap;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

public interface LairdGapUiCallback {
    /*
     * scanning
     */
    public void uiStartScanning();
    public void uiStopScanning();
    public void uiOnLeScan(
            final BluetoothDevice device,
            final int rssi,
            final byte[] scanRecord);
    
    /*
     * connection state change
     */
    public void uiOnConnectionStateChangeConnected(
            final BluetoothGatt gatt);
    public void uiOnConnectionStateChangeDisconnected(
            final BluetoothGatt gatt);
    public void uiOnConnectionStateChangeConnecting(
            final BluetoothGatt gatt);
    public void uiOnConnectionStateChangeDisconnecting(
            final BluetoothGatt gatt);
    public void uiOnConnectionStateChangeFailure(
            final BluetoothGatt gatt,
            final int status,
            final int newState);
    
    /*
     * read remote RSSI
     */
    public abstract void uiOnReadRemoteRssiSuccess(
            final BluetoothGatt gatt,
            final int rssi);
    public abstract void uiOnReadRemoteRssiFailure(
            final BluetoothGatt gatt,
            final int rssi,
            final int status);
    
    /*
     * services discovered
     */
    public abstract void uiOnServicesDiscoveredSuccess(
            final BluetoothGatt gatt);
    public abstract void uiOnServicesDiscoveredFailure(
            final BluetoothGatt gatt,
            final int status);
    
    /*
     * descriptor read
     */
    public abstract void uiOnDescriptorReadSuccess(
            final BluetoothGatt gatt,
            final BluetoothGattDescriptor ch);
    public abstract void uiOnDescriptorReadFailure(
            final BluetoothGatt gatt,
            final BluetoothGattDescriptor ch,
            final int status);
    
    /*
     * descriptor write
     */
    public abstract void uiOnDescriptorWriteSuccess(
            final BluetoothGatt gatt,
            final BluetoothGattDescriptor descriptor);
    public abstract void uiOnDescriptorWriteFailure(
            final BluetoothGatt gatt,
            final BluetoothGattDescriptor descriptor,
            final int status);
    
    /*
     * characteristic write
     */
    public abstract void uiOnCharacteristicWriteSuccess(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic ch);
    public abstract void uiOnCharacteristicWriteFailure(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic ch,
            final int status);
    
    /*
     * characteristic read
     */
    public abstract void uiOnCharacteristicReadSuccess(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic ch);
    public abstract void uiOnCharacteristicReadFailure(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic ch,
            final int status);
    
    /*
     * characteristic changed
     */
//    public abstract void uiOnCharacteristicChangedSuccess(
//            final BluetoothGatt gatt,
//            final BluetoothGattCharacteristic ch);
    
    /*
     * reliable write
     */
    public abstract void uiOnReliableWriteCompletedSuccess(
            final BluetoothGatt gatt);
    public abstract void uiOnReliableWriteCompletedFailure(
            final BluetoothGatt gatt,
            final int status);
}