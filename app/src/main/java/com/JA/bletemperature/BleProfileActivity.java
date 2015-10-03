package com.JA.bletemperature;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.JA.bletemperature.gap.LairdGapBase;
import com.JA.bletemperature.misc.DebugWrapper;

public abstract class BleProfileActivity extends Activity implements OnClickListener{
    public static final int ENABLE_BT_REQUEST_ID = 1;
    protected LairdGapBase mLairdGapBase;
    private com.JA.bletemperature.DeviceListAdapter mDevicesListAdapter = null;
    private TextView mLabStatus;
    //protected Dialog dialog;
    //private Button mBtnConnect;
    protected Activity mActivity;
    long DateSince;
    
    /**
     * sets the view
     */
    protected abstract int setContentView();
    /**
     * sets the activity
     */
    protected abstract Activity setActivity();
    /**
     * set the BLE class base
     * @return
     */
    protected abstract LairdGapBase setLairdGapBase();
    /**
     * callback for when the scanning has been cancelled
     */
    protected abstract void onScanningCancel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DebugWrapper.errorMsg("BleProfileActivity / Create", "<<>>");
        super.onCreate(savedInstanceState);
        setContentView(setContentView());
        mActivity = setActivity();
        mLairdGapBase = setLairdGapBase();
        //mLabStatus = (TextView) findViewById(R.id.labStatus);
        //mLabStatus.setText("toto");
        //DateSince = System.currentTimeMillis()/1000/60;
        DateSince = System.currentTimeMillis();

        // check if we have BT and BLE on board
        if (mLairdGapBase.checkBleHardwareAvailable() == false) {
            DebugWrapper.toastMsg(this, "BLE Hardware is required but not available!");
            finish();
        }
        
        if(mLairdGapBase.initialize() == false){
            DebugWrapper.toastMsg(this, "Could not initialize Bluetooth");
            finish();
        }
    }
    
    private void setDefaultViewValues(){
        //mBtnConnect.setText("Connect");
    }

    public void ScanConnect(){
        DebugWrapper.errorMsg("BleProfileActivity / ScanConnect: entered", " <<>> ");
        if(mLairdGapBase.isBtEnabled() == true){
            if(mLairdGapBase.isScanning() == false && mLairdGapBase.isConnected() == false){
                    DebugWrapper.errorMsg("BleProfileActivity / mLairdGapBase startScanning", " <<>> ");
                    mLairdGapBase.startScanning();
                    //mLabStatus.setText("Scanning");
                    devicesListDialog();
                } else if(mLairdGapBase.isScanning() == true && mLairdGapBase.isConnected() == false){
                    DebugWrapper.errorMsg("BleProfileActivity / mLairdGapBase stopScanning", " <<>> ");
                    mLairdGapBase.stopScanning();
                    //mLairdGapBase.startScanning();
                //mLabStatus.setText("Stop Scanning");
                } else if(mLairdGapBase.isConnected() == true){
                    DebugWrapper.errorMsg("BleProfileActivity / mLairdGapBase disconnect", " <<>> ");
                    mLairdGapBase.disconnect();
                    //mLabStatus.setText("DisConnect");
                    runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        setDefaultViewValues();
                    }
                });
             }
        } else if(mLairdGapBase.isBtEnabled() == false){
            DebugWrapper.toastMsg(this, "Bluetooth must be enabled");
        }
        uiInvalidateBtnState();
    }


    @Override
    public void onClick(View view){
        DebugWrapper.errorMsg("BleProfileActivity / onClick", " <<>> ");
        int btnClickedId = view.getId();
        //uiInvalidateBtnState();


        mLairdGapBase.stopScanning();
        mLairdGapBase.disconnect();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ScanConnect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        DebugWrapper.errorMsg("<<<<<<<<<<>>>>>>>>>>>>> / on Pause", " <<>> ");
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

        @Override
    protected void onResume(){
        DebugWrapper.errorMsg("<<<<<<<<<<>>>>>>>>>>>>> / on Resume", " <<>> ");
        super.onResume();
        /*
         *  on every Resume check if BT is enabled
         *  user could turn it off while app was in background etc
         */
        if (mLairdGapBase.isBtEnabled() == false) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
            // see onActivityResult to check what is the status of our request
        }
        else {
            mDevicesListAdapter = new com.JA.bletemperature.DeviceListAdapter(this);
            ScanConnect();
        }
    }

    //@Override
    //protected void onStart() {
    //    DebugWrapper.errorMsg("<<<<<<<<<<>>>>>>>>>>>>> / on Start", " <<>> ");
    //}

    protected void uiInvalidateBtnState(){
        DebugWrapper.errorMsg("BleProfileActivity / uiInvalidateBtnState", " <<>> ");
        //mLabStatus.setText("Scanning");
        long NewDateSince;
        NewDateSince = System.currentTimeMillis();
        if (((NewDateSince - DateSince) / 1000) > 300)
        {
            android.os.Process.killProcess(android.os.Process.myPid());
            super.onDestroy();
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mLairdGapBase.isScanning() == false && mLairdGapBase.isConnected() == false){
                    DebugWrapper.errorMsg("BleProfileActivity / bouton = connect"," <<>> ");
                    //mBtnConnect.setText(R.string.connect);
                    DebugWrapper.toastMsg(mActivity, "Disconnect");
                    ScanConnect();
                } else if(mLairdGapBase.isScanning() == true && mLairdGapBase.isConnected() == false){
                    DebugWrapper.errorMsg("BleProfileActivity / bouton = scanning"," <<>> ");
                    DebugWrapper.toastMsg(mActivity, "Scanning");
                    //mBtnConnect.setText(R.string.searching);
                } else if(mLairdGapBase.isConnected() == true){
                    DebugWrapper.errorMsg("BleProfileActivity / bouton = disconnect"," <<>> ");
                    //mBtnConnect.setText(R.string.disconnect);
                    DebugWrapper.toastMsg(mActivity, "Connect");
                }
                invalidateOptionsMenu();
            }
        });
    }
    
    // add device to the current list of devices
    protected void handleFoundDevice(final BluetoothDevice device,
            final int rssi,
            final byte[] scanRecord)
    {
        // adding to the UI have to happen in UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // OK
                //DebugWrapper.errorMsg("BleProfileActivity / mDevicesListAdapter addDevice"," <<>> ");
                mDevicesListAdapter.addDevice(device, rssi, scanRecord);
                mDevicesListAdapter.notifyDataSetChanged();
                int i = mDevicesListAdapter.getCount();
                for(int x = 1 ; x <= i ; x ++)
                {
                    BluetoothDevice device = mDevicesListAdapter.getDevice(x-1);
                    String name = device.getName();
                    // In case we found a Bluetooth device without a name, skip this iteration of the loop here:
                    if(name == null) {
                        continue;
                    }
                    DebugWrapper.errorMsg("BleProfileActivity / handleFoundDevice: name: " + name, " <<<>>> ");
                    //if (name.equals("JATEMP")) {
                    if (name.equals("BTB8_STVO")) {
                        if (mLairdGapBase.isConnected() == false)
                        {
                            DebugWrapper.errorMsg("BleProfileActivity / CONNECT Auto", " <<>> ");
                            mLairdGapBase.connect(device.getAddress());
                            DebugWrapper.toastMsg(mActivity, "Connect");
                            tempo();
                        }
                    }
                }
             }
        });
    }


    protected void devicesListDialog(){
        DebugWrapper.errorMsg("BleProfileActivity / Devices List Dialog", " <<>> ");
    }
    
    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_ID) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // user didn't enabled BT
                DebugWrapper.toastMsg(this, "Sorry, BT has to be turned ON for this App!");
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void tempo() {
        for(int i = 0;i<2;i++){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //do your Ui task here
                }
            });

            try {

                Thread.sleep(500);

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
