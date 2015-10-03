package com.JA.bletemperature.vspservice;

import java.util.Queue;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.JA.bletemperature.gap.LairdGapBase;
import com.JA.bletemperature.misc.DebugWrapper;
import com.JA.bletemperature.misc.StringHandler;

/**
 * Responsible for the communication between the android device and the module
 */
public abstract class VSPManager extends LairdGapBase{

    /*
     * *************************************
     * public variables
     * *************************************
     */
    final static public String TAG = "Laird VSP";
    final static public UUID UUID_SERVICE_VSP = UUID.fromString("569a1101-b87f-490c-92cb-11ba5ea5167c");
    final static public UUID UUID_CHARACTERISTIC_RX = UUID.fromString("569a2001-b87f-490c-92cb-11ba5ea5167c");
    final static public UUID UUID_CHARACTERISTIC_TX = UUID.fromString("569a2000-b87f-490c-92cb-11ba5ea5167c");
    final static public String NAME_VSP_SERVICE = "VSP";
    final static public String NAME_RX_CHARACTERISTIC = "RX";
    final static public String NAME_TX_CHARACTERISTIC = "TX";
    
    /*
     * *************************************
     * protected variables
     * *************************************
     */
    protected VSPUiCallback mUiVSPCallback;
    protected int mMaxDataToBeReadFromRxBuffer;
    
    /*
     * *************************************
     * private variables
     * *************************************
     */
    private boolean mIsServiceDiscoveryFinished = false;
    private BluetoothGattService mVspService;
    private BluetoothGattCharacteristic mModuleCharRX;
    private BluetoothGattCharacteristic mModuleCharTX;
    /**
     * Sending data to module buffer
     */
    private FifoQueue mRxBuffer;
    /**
     * Receiving data from module buffer
     */
    private StringBuilder mTxBuffer = new StringBuilder();
    
    /*
     * *************************************
     * constructor
     * *************************************
     */
    public VSPManager(Activity activity, VSPUiCallback uiVSPCallback, int maxDataToBeReadFromRxBuffer)
            throws NullPointerException{
        super(activity);
        if(uiVSPCallback == null) {
            throw new NullPointerException("VSPUiCallback object is NULL");
        } else{
            mMaxDataToBeReadFromRxBuffer = maxDataToBeReadFromRxBuffer;
            mUiVSPCallback = uiVSPCallback;
            mRxBuffer = new FifoQueue(maxDataToBeReadFromRxBuffer);
        }
    }
    
    /*
     * *************************************
     * getter methods
     * *************************************
     */
    public BluetoothGattService getVspService() {return mVspService;}
    public BluetoothGattCharacteristic getModuleCharRX() {return mModuleCharRX;}
    public BluetoothGattCharacteristic getModuleCharTX() {return mModuleCharTX;}
    public boolean getIsServiceDiscoveryFinished(){
        return mIsServiceDiscoveryFinished;
    }
    
    protected Queue<Character> getRxBuffer(){
        return mRxBuffer.getBufferData();
    };
    
    /*
     * *************************************
     * protected methods
     * *************************************
     */
    
    /**
     * Callback which gets called whenever data from the module is being received
     */
    protected abstract void onTxData();
    
    /**
     * writes the string data passed to the connected device
     * @param data
     */
    protected void write(String data){
        mRxBuffer.write(data);
        writeFromFIFOToModule();
    };
    
    /**
     * clears the RX buffer and the TX buffer
     */
    protected void flush(){
        mRxBuffer.flush();
        mTxBuffer.delete(0, mTxBuffer.length());
    };
    
    /**
     * reads all the data from the TX buffer and stores it into
     * the StringBuilder parameter "dest"
     * @param dest the object to store the data read
     * @return the total number of characters read
     */
    protected int read(StringBuilder dest){
        if(mTxBuffer != null){
            dest.append(mTxBuffer);
            mTxBuffer.delete(0, mTxBuffer.length());
            return dest.length();
        }
        return 0;
    };
    
    /**
     * reads data from TX buffer from index 0 to including end index.
     * The data that was read is removed from the TX buffer.
     * if the getUpto > TX buffer returns the whole data from the TX buffer
     * @param dest the object to store the data read
     * @param end the index to read to, included
     * @return the total number of characters read
     */
    protected int read(StringBuilder dest, int end){
        if(mTxBuffer != null && end > 0){
            if(end < mTxBuffer.length()){
                // get only the data asked
                dest.append(mTxBuffer.substring(0, end));
                mTxBuffer.delete(0, end);
                return dest.length();
            } else{
                // get all data
                return read(dest);
            }
        }
        return 0;
    };
    
    /**
     * finds the specified string from the TX buffer
     * and returns the data up to that string.
     * The data that was read is removed from the TX buffer.
     * @param dest the object to store the data read
     * @param searchFor the string to search for
     * @return the total number of characters read
     */
    protected int read(StringBuilder dest, String searchFor){
        if(mTxBuffer != null){
            int stringIndex = mTxBuffer.indexOf(searchFor);
            if(stringIndex != -1){
                dest.append(mTxBuffer.substring(0, stringIndex+1));
                mTxBuffer.delete(0, stringIndex+1);
                return dest.length();
            }
        }
        return 0;
    };
    
    /**
     * Callback indicating when GATT client has connected to/from a remote GATT server.
     * super.onConnectionStateChangeConnected(gatt) should be called to stop scanning, set to connected state
     * and to start service discovery.
     * 
     */
    @Override
    protected void onConnectionStateChangeConnected(
            BluetoothGatt gatt) {
        stopScanning();
        mConnected = true;
        discoverServices();
    }
    
    /**
     * Callback indicating when GATT client has disconnected to/from a remote GATT server.
     * super.onConnectionStateChangeDisconnected(gatt) should be called to set VSP values
     * to default. The GATT client gets closed just before this method comes to an end
     */
    @Override
    protected void onConnectionStateChangeDisconnected(
            BluetoothGatt gatt) {
        stopListeningModuleTX();
        setToDefault();
        if(mBluetoothGatt != null){
            closeGatt();
        }
    }
    
    /**
     * Callback invoked when the list of remote services, characteristics and descriptors
     * for the remote device have been updated, ie new services have been discovered.
     * super.onServicesDiscoveredSuccess(gatt) should be called to initiate VSP service
     * search.
     */
    @Override
    protected void onServicesDiscoveredSuccess(
            BluetoothGatt gatt) {
        mVspService = gatt.getService(UUID_SERVICE_VSP);   
        mIsServiceDiscoveryFinished = true;
        if(mVspService == null) {
            mUiVSPCallback.uiServiceVSPNotFound(gatt);
        } else{
            mUiVSPCallback.uiServiceVSPFound(gatt);
            mModuleCharRX = mVspService.getCharacteristic(UUID_CHARACTERISTIC_RX);        
            mModuleCharTX = mVspService.getCharacteristic(UUID_CHARACTERISTIC_TX);
            
            if(mModuleCharRX == null || mModuleCharTX == null){
                mUiVSPCallback.uiModuleVSPCharsNotFound(gatt);
            } else{
                mUiVSPCallback.uiModuleVSPCharsFound(gatt);
                startListeningModuleTX();
            }
        }
    }
    
    
    /**
     * 
     */
    @Override
    protected void onCharacteristicWriteSuccess(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch) {
        UUID serviceUUID = ch.getService().getUuid();
        UUID charUUID = ch.getUuid();
        
        if(VSPManager.UUID_SERVICE_VSP.equals(serviceUUID)){
            if(VSPManager.UUID_CHARACTERISTIC_RX.equals(charUUID)){
                
                if(mRxBuffer.getBufferData().size() > 0){
                    writeFromFIFOToModule();
                }
            }
        }
    }
    
    /**
     * 
     */
    @Override
    protected void onCharacteristicChangedSuccess(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic ch) {
        UUID serviceUUID = ch.getService().getUuid();
        UUID charUUID = ch.getUuid();
        
        if(VSPManager.UUID_SERVICE_VSP.equals(serviceUUID)){
            if(VSPManager.UUID_CHARACTERISTIC_TX.equals(charUUID)){
                
                DebugWrapper.infoMsg(StringHandler.printWithNonPrintableChars("YOUSSIF ch.getStringValue(0)" + ch.getStringValue(0)), TAG);
                mTxBuffer.append(ch.getStringValue(0));
                onTxData();
            }
        }
    }
    
    /*
     * *************************************
     * private methods
     * *************************************
     */
    /**
     * enables the TX characteristic notifications
     * @return true if was successful initiated, false otherwise
     */
    private boolean startListeningModuleTX(){
        if(mBluetoothGatt == null || mVspService == null ||
                mModuleCharTX == null) return false;
        boolean success = setNotificationsForCharacteristic(mModuleCharTX, true);
        return success;
    }
    
    /**
     * disables the TX characteristic notifications
     * @return true if was successful initiated, false otherwise
     */
    private boolean stopListeningModuleTX(){
        if(mBluetoothGatt == null || mVspService == null ||
                mModuleCharTX == null) return false;
        boolean success = setNotificationsForCharacteristic(mModuleCharTX, false);
        return success;
    }
    
    /**
     * initialises all values to default
     */
    private void setToDefault(){
        mConnected = false;
        mIsServiceDiscoveryFinished = false;
        mVspService = null;
        mModuleCharRX = null;
        mModuleCharTX = null;
        flush();
    }
    
    /**
     * 
     */
    private void writeFromFIFOToModule(){
        if(mBluetoothGatt == null || mConnected == false) {
            return;
        }
        String dataToBeSend = mRxBuffer.read();
        
        mModuleCharRX.setValue(dataToBeSend);
        writeCharacteristicToDevice(mModuleCharRX);
    }
    
    
    
    
}