package com.JA.bletemperature.gap;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.JA.bletemperature.misc.DebugWrapper;


public abstract class LairdGapBase{
    /*
     * *************************************
     * public variables
     * *************************************
     */
    /**
     * make sure that potential scanning will take no longer
     * than <SCANNING_TIMEOUT> seconds from now on 
     */
    final static public String TAG = "BTB8_STVO";
    final static public long SCANNING_TIMEOUT = 15000;
    //final static public long SCANNING_TIMEOUT = -1;
    
    /*
     * protected variables    
     */
    protected BluetoothGatt mBluetoothGatt = null;
    protected boolean mScanning = false;
    protected boolean mConnected = false;
    protected Activity mActivity;
    
    /*
     * *************************************
     * private variables
     * *************************************
     */
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice  mBluetoothDevice = null;
    private Context mContext;
    private Handler mScanningTimeoutHandler = new Handler();
    
    /*
     * *************************************
     * constructor
     * *************************************
     */
    public LairdGapBase(Activity activity){
        if(activity == null) {
            throw new NullPointerException("Activity object passed is NULL");
        } else{
            mActivity = activity;
            mContext = mActivity.getApplicationContext();
        }
    }
    
    /*
     * *************************************
     * getter methods
     * *************************************
     */
    public BluetoothDevice getBluetoothDevice(){return mBluetoothDevice;}
    public BluetoothGatt getBluetoothGatt(){return mBluetoothGatt;}
    public boolean isScanning(){return mScanning;}
    public boolean isConnected(){return mConnected;}
    
    /*
     * *************************************
     * public methods
     * *************************************
     */
    /**
     * initiates scan operation
     */
    public void startScanning(){
        scanLeDevice(true);
    }
    
    /**
     * initiates stop scanning operation
     */
    public void stopScanning(){
        scanLeDevice(false);
    }
    
    /**
     * Disconnect from device
     */
    public void disconnect(){
        mBluetoothGatt.disconnect(); //callback in BLuetoothGattCallback -> onConnectionStateChange
    }

    /** 
     * Initialise BLE and get BT Manager & Adapter
     * @return boolean
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        if(mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }
    
    /**
     * Connect to a specific device
     * @return boolean
     */
    public boolean connect(final String deviceAddress){
        if(mBluetoothAdapter == null) return false;
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        DebugWrapper.errorMsg("Trying to connect to:", mBluetoothDevice.getName());
        if (mBluetoothDevice == null) {
            // we got wrong address - that device is not available!
            DebugWrapper.errorMsg("Device is not available", TAG);
            return false;
        }
        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mBluetoothGattCallback);
        return true;
    }
    
    /** Before any action check if BT is turned ON and enabled,
     * call this in onResume to be always sure that BT is ON when Your
     * application is put into the foreground 
     * @return boolean
     */
    public boolean isBtEnabled() {
        final BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager == null) return false;

        final BluetoothAdapter adapter = manager.getAdapter();
        if(adapter == null) return false;

        return adapter.isEnabled();
    }

    /**
     * run test and check if this device has BT and BLE hardware available
     * @return boolean
     */
    public boolean checkBleHardwareAvailable() {
        // First check general Bluetooth Hardware:
        // get BluetoothManager...
        final BluetoothManager manager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        if(manager == null) return false;
        // .. and then get adapter from manager
        final BluetoothAdapter adapter = manager.getAdapter();
        if(adapter == null) return false;
        // and then check if BT LE is also available
        boolean hasBle = mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        return hasBle;
    }
    
    
    /*
     * *************************************
     * protected methods
     * *************************************
     */
    protected abstract void startScanningCallback();
    protected abstract void stopScanningCallback();
    
    /** 
     * close GATT client completely 
     */
    protected void closeGatt() {
        if(mBluetoothGatt == null) return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    
    /**
     * Start the discovery of services
     */
    protected void discoverServices(){
        if(mBluetoothGatt.discoverServices() == true){ // callback: onServicesDiscovered
            DebugWrapper.infoMsg("Success initiating remote service discovery!", TAG);
        } else{
            DebugWrapper.errorMsg("Failed to initiate remote service discovery!", TAG);
            disconnect();
        }
    }
    
    /**
     * enable/disable notifications/indications for a characteristic
     * @return boolean
     */
    protected boolean setNotificationsForCharacteristic(BluetoothGattCharacteristic ch, boolean enabled) {
        if (mBluetoothGatt == null) return false;   
        
        boolean success = mBluetoothGatt.setCharacteristicNotification(ch, enabled);
        if(!success) {
            DebugWrapper.errorMsg("Failed to set proper notification status for characteristic with UUID: " + ch.getUuid(), TAG);
            return false;
        }
        return writeDescriptor(ch, enabled);
    }
    
    /**
     * write the CCCD descriptor for enabling/disabling notifications/indications to a specific characteristic
     * @return boolean
     */
    protected boolean writeDescriptor(BluetoothGattCharacteristic ch, boolean enabled){
        DebugWrapper.errorMsg("LairdGapBase / write Derscriptor: Entered fct" + ch.toString(), " <<>> ");
        // see: https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
        BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        ////BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"));
        ////BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c"));
        if(descriptor == null) return false;

        // set notifications, heart rate measurement etc
        byte[] val = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

        descriptor.setValue(val);
        DebugWrapper.errorMsg("LairdGapBase / write Descriptor: Descriptor set val: " + val.toString() + " / en: " + BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE + " / dis: " + BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE , " <<>> ");
        
        boolean success = mBluetoothGatt.writeDescriptor(descriptor); // callback in BLuetoothGattCallback -> onDescriptorWrite
        return success;
    }
    
    /**
     * write a characteristic to the connected device
     * @return boolean
     */
    protected boolean writeCharacteristicToDevice(BluetoothGattCharacteristic ch){
        boolean success = mBluetoothGatt.writeCharacteristic(ch); // callback in BLuetoothGattCallback -> onCharacteristicWrite
        return success;
    }
    
    
    /*
     * *************************************
     * BLE abstract callbacks to the child of this class
     * *************************************
     */
    
    /**
     * Callback reporting an LE device found during a device scan initiated by the BluetoothAdapter.startLeScan function.
     * @param device
     * @param rssi
     * @param scanRecord
     */
    protected abstract void onLeScanSuccess(
            BluetoothDevice device,
            int rssi,
            byte[] scanRecord);
    
    
    /**
     * Callback indicating when GATT client has connected to/from a remote GATT server.
     * @param gatt
     */
    protected abstract void onConnectionStateChangeConnected(
            BluetoothGatt gatt);
    /**
     * Callback indicating when GATT client is connecting to/from a remote GATT server.
     * @param gatt
     */
    protected abstract void onConnectionStateChangeConnecting(
            BluetoothGatt gatt);
    /**
     * Callback indicating when GATT client has disconnected to/from a remote GATT server.
     * @param gatt
     */
    protected abstract void onConnectionStateChangeDisconnected(
            BluetoothGatt gatt);
    /**
     * Callback indicating when GATT client is disconnecting to/from a remote GATT server.
     * @param gatt
     */
    protected abstract void onConnectionStateChangeDisconnecting(
            BluetoothGatt gatt);
    /**
     * Callback indicating that a GATT operation failed while connecting/disconnecting
     * @param gatt
     * @param status
     * @param newState
     */
    protected abstract void onConnectionStateChangeFailure(
            BluetoothGatt gatt,
            int status,
            int newState);
    
    
    /**
     * Callback reporting the RSSI for a remote device connection. This callback is triggered in response to the BluetoothGatt.readRemoteRssi function.
     * @param gatt
     * @param rssi
     */
    protected abstract void onReadRemoteRssiSuccess(
            BluetoothGatt gatt,
            int rssi);
    /**
     * Callback indicating that a GATT operation failed while reading remote RSSI
     * @param gatt
     * @param rssi
     * @param status
     */
    protected abstract void onReadRemoteRssiFailure(
            BluetoothGatt gatt,
            int rssi,
            int status);
    
    
    /**
     * Callback invoked when the list of remote services, characteristics and descriptors
     * for the remote device have been updated, ie new services have been discovered.
     * @param gatt
     */
    protected abstract void onServicesDiscoveredSuccess(
            BluetoothGatt gatt);
    /**
     * Callback indicating that a GATT operation failed while discovering services
     * @param gatt
     * @param status
     */
    protected abstract void onServicesDiscoveredFailure(
            BluetoothGatt gatt,
            int status);
    
    /**
     * Callback reporting the result of a descriptor read operation.
     * @param gatt
     * @param ch
     */
    protected abstract void onDescriptorReadSuccess(
            BluetoothGatt gatt,
            BluetoothGattDescriptor ch);
    /**
     * Callback indicating that a GATT operation failed while reading descriptor
     * @param gatt
     * @param ch
     * @param status
     */
    protected abstract void onDescriptorReadFailure(
            BluetoothGatt gatt,
            BluetoothGattDescriptor ch,
            int status);
    
    /**
     * Callback indicating the result of a descriptor write operation.
     * @param gatt
     * @param descriptor
     */
    protected abstract void onDescriptorWriteSuccess(
            BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor);
    /**
     * Callback indicating that a GATT operation failed while writing descriptor
     * @param gatt
     * @param descriptor
     * @param status
     */
    protected abstract void onDescriptorWriteFailure(
            BluetoothGatt gatt,
            BluetoothGattDescriptor descriptor,
            int status);
    
    /**
     * Callback indicating the result of a characteristic write operation.
     * @param gatt
     * @param ch
     */
    protected abstract void onCharacteristicWriteSuccess(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch);
    /**
     * Callback indicating that a GATT operation failed while writing characteristic
     * @param gatt
     * @param ch
     * @param status
     */
    protected abstract void onCharacteristicWriteFailure(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch,
            int status);
    
    /**
     * Callback reporting the result of a characteristic read operation.
     * @param gatt
     * @param ch
     */
    protected abstract void onCharacteristicReadSuccess(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch);
    /**
     * Callback indicating that a GATT operation failed while reading characteristic.
     * @param gatt
     * @param ch
     * @param status
     */
    protected abstract void onCharacteristicReadFailure(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch,
            int status);
    
    /**
     * Callback triggered as a result of a remote characteristic notification.
     * @param gatt
     * @param ch
     */
    protected abstract void onCharacteristicChangedSuccess(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch);
    
    /**
     * Callback invoked when a reliable write transaction has been completed successfully.
     * @param gatt
     */
    protected abstract void onReliableWriteCompletedSuccess(
            BluetoothGatt gatt);
    /**
     * Callback indicating that a GATT operation failed while writing reliable
     * @param gatt
     * @param status
     */
    protected abstract void onReliableWriteCompletedFailure(
            BluetoothGatt gatt,
            int status);
    
    /*
     * *************************************
     * private methods
     * *************************************
     */
    /**
     * Starts or stops scanning for BLE devices
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            DebugWrapper.errorMsg("Start ScanLeDevice", " <<>> ");
            // starts and stops scanning after a pre-defined scan period.
            mScanningTimeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    stopScanningCallback();
                }
            }, SCANNING_TIMEOUT);
            
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            startScanningCallback();
        } else {
            DebugWrapper.errorMsg("Stop ScanLeDevice", " <<>> ");
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            stopScanningCallback();
        }
    }
    
    /**
     * defines callback for scanning results
     */
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override // comes from: startLeScan
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            onLeScanSuccess(device, rssi, scanRecord);
        }
    };
    


    /*
     * *************************************
     * BLE callbacks
     * *************************************
     */
    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    onConnectionStateChangeConnected(gatt);
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    onConnectionStateChangeConnecting(gatt);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    onConnectionStateChangeDisconnected(gatt);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    onConnectionStateChangeDisconnecting(gatt);
                }
            } else{
                onConnectionStateChangeFailure(gatt, status, newState);
            }
        }
        
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                onReadRemoteRssiSuccess(gatt, rssi);
            }
            else{
                onReadRemoteRssiFailure(gatt, rssi, status);
            }
        }
        
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                onServicesDiscoveredSuccess(gatt);
            }
            else{
                onServicesDiscoveredFailure(gatt, status);
            }
        }
        
        public void onDescriptorRead(BluetoothGatt gatt,
                BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                onDescriptorReadSuccess(gatt, descriptor);
            }
            else{
                onDescriptorReadFailure(gatt, descriptor, status);
            }            
        };
        
        
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                onDescriptorWriteSuccess(gatt, descriptor);
            }
            else{
                onDescriptorWriteFailure(gatt, descriptor, status);
            }
        }
        
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                BluetoothGattCharacteristic ch, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                onCharacteristicWriteSuccess(gatt, ch);
            } else{
                onCharacteristicWriteFailure(gatt, ch, status);
            }
        }
        
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                onCharacteristicReadSuccess(gatt, characteristic);
            }
            else{
                onCharacteristicReadFailure(gatt, characteristic, status);
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                BluetoothGattCharacteristic ch) {
            onCharacteristicChangedSuccess(gatt, ch);
        }
        
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                onReliableWriteCompletedSuccess(gatt);
            }
            else{
                onReliableWriteCompletedFailure(gatt, status);
            }
        }
    };    
}