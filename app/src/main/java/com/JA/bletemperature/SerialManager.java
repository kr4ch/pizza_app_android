package com.JA.bletemperature;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import com.JA.bletemperature.misc.DebugWrapper;
import com.JA.bletemperature.misc.StringHandler;
import com.JA.bletemperature.vspservice.VSPManager;
import com.JA.bletemperature.vspservice.VSPUiCallback;

public class SerialManager extends VSPManager{
    final static public String TAG = "BL600 Serial";
    private com.JA.bletemperature.SerialUiManagerCallback mSerialUiManagerCallback;
    
    
    public SerialManager(Activity activity, VSPUiCallback uiVSPCallback,
            com.JA.bletemperature.SerialUiManagerCallback serialUiManagerCallback, int maxDataToBeReadFromRxBuffer) throws NullPointerException {
        super(activity, uiVSPCallback, maxDataToBeReadFromRxBuffer);
        mSerialUiManagerCallback = serialUiManagerCallback;
    }
    
    protected void sendData(String data){
        if(mBluetoothGatt == null)  return;
        write(data+"\r");
    }

    @Override
    protected void onTxData() {
        StringBuilder dest = new StringBuilder();
            while(read(dest, "\r") != 0){
            /*
             * found data we want
             */
            DebugWrapper.infoMsg(StringHandler.printWithNonPrintableChars("onTxData: " + dest), TAG);
            mSerialUiManagerCallback.uiOnResponse(dest.toString());
            dest.delete(0, dest.length());
        }
    }

    @Override
    protected void startScanningCallback() {
        mUiVSPCallback.uiStartScanning();
    }

    @Override
    protected void stopScanningCallback() {
        mUiVSPCallback.uiStopScanning();
    }

    @Override
    protected void onLeScanSuccess(BluetoothDevice device, int rssi,
            byte[] scanRecord) {
        mUiVSPCallback.uiOnLeScan(device, rssi, scanRecord);
    }

    @Override
    protected void onConnectionStateChangeConnected(BluetoothGatt gatt) {
        super.onConnectionStateChangeConnected(gatt);
        mUiVSPCallback.uiOnConnectionStateChangeConnected(gatt);
    }

    @Override
    protected void onConnectionStateChangeConnecting(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void onConnectionStateChangeDisconnected(
            BluetoothGatt gatt) {
        super.onConnectionStateChangeDisconnected(gatt);
        mUiVSPCallback.uiOnConnectionStateChangeDisconnected(gatt);
    }
    
    @Override
    protected void onConnectionStateChangeDisconnecting(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onConnectionStateChangeFailure(BluetoothGatt gatt,
            int status, int newState) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onReadRemoteRssiSuccess(BluetoothGatt gatt, int rssi) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onReadRemoteRssiFailure(BluetoothGatt gatt, int rssi,
            int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void onServicesDiscoveredFailure(BluetoothGatt gatt, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected void onDescriptorReadSuccess(BluetoothGatt gatt,
            BluetoothGattDescriptor ch) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onDescriptorReadFailure(BluetoothGatt gatt,
            BluetoothGattDescriptor ch, int status) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onDescriptorWriteSuccess(BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onDescriptorWriteFailure(BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor, int status) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onCharacteristicWriteFailure(BluetoothGatt gatt,
            BluetoothGattCharacteristic ch, int status) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onCharacteristicReadSuccess(BluetoothGatt gatt,
            BluetoothGattCharacteristic ch) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onCharacteristicReadFailure(BluetoothGatt gatt,
            BluetoothGattCharacteristic ch, int status) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onReliableWriteCompletedSuccess(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
    	
    }

    @Override
    protected void onReliableWriteCompletedFailure(BluetoothGatt gatt,
            int status) {
        // TODO Auto-generated method stub
        
    }
}