package com.JA.bletemperature;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.lang.Math;

import com.JA.bletemperature.gap.LairdGapBase;
import com.JA.bletemperature.misc.DebugWrapper;

public class MyActivity extends com.JA.bletemperature.BleProfileActivity implements com.JA.bletemperature.SerialUiManagerCallback {
    private static final int MAX_DATA_TO_READ_FROM_RX_BUFFER = 19;
    private com.JA.bletemperature.SerialManager mSerialManager;
    private Button mBtnSend;
    private EditText mValueVspInputEt;
    private TextView mValueVspOutTv;
    private TextView mLabTemperature;
    private TextView mTempA;
    private TextView mTempB;
    private TextView mTempC;
    private TextView mTempD;
    private TextView mTempE;
    private TextView mTempF;
    private TextView mRecvCnt;
    //private TextView mLabStatus;
    private TextView mLabCopyright;
    private TextView mLabCellVolt;
    int[] mTemperatureArray;
    String[] mResistorArray;
    int[] mAlphaArray;
    int intWaiting;
    private ImageView iLogo;
    Bitmap originalImage;
    int width;
    int height;
    float scaleWidth ;
    float scaleHeight;
    Matrix matrix;
    Bitmap resizedBitmap;
    ByteArrayOutputStream outputStream;
    LinearLayout bkLayout;
    boolean FlagLogo;
    Integer recvCnt = 0;

        @Override
    protected void onCreate(final Bundle savedInstanceState) {
        FlagLogo = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if( getIntent().getBooleanExtra("Exit me", false)){
           finish();
           return; // add this to prevent from doing unnecessary stuffs
        }
        bindViews();
        mTemperatureArray = getResources().getIntArray(R.array.Temperature);
        mResistorArray = getResources().getStringArray(R.array.Resistor);
        mAlphaArray = getResources().getIntArray(R.array.Alpha);
        bkLayout=(LinearLayout)findViewById(R.id.backLayout);
        intWaiting = 0;
    }
    
    public void bindViews(){

        //mValueVspOutTv = (TextView) findViewById(R.id.valueVspOutTv);
        //mLabTemperature = (TextView) findViewById(R.id.labTemperature);
        mTempA = (TextView) findViewById(R.id.tempA);
        mTempB = (TextView) findViewById(R.id.tempB);
        mTempC = (TextView) findViewById(R.id.tempC);
        mTempD = (TextView) findViewById(R.id.tempD);
        mTempE = (TextView) findViewById(R.id.tempE);
        mTempF = (TextView) findViewById(R.id.tempF);
        mRecvCnt = (TextView) findViewById(R.id.recvCntTxt);
        //mLabStatus = (TextView) findViewById(R.id.labStatus);
        mLabCopyright = (TextView) findViewById(R.id.labCopyright);
        mLabCopyright.setText("Pfadi Sturmvogel - Pizzaofen\nBTB8 Test Program");
        mLabCellVolt = (TextView) findViewById(R.id.labCellVolt);
        mLabCellVolt.setText("");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //etMenuInflater().inflate(R.menu.my, menu);

        if (FlagLogo == false) {
            iLogo = (ImageView) findViewById(R.id.imageButton);
            int tempo;
            tempo = iLogo.getHeight();
            originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            width = originalImage.getWidth();
            height = originalImage.getHeight();
            matrix = new Matrix();
            scaleWidth = ((float) tempo) / 200;
            scaleHeight = ((float) tempo) / 200;
            matrix.postScale(scaleWidth, scaleHeight);
        }
        FlagLogo = true;
        resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, true);
        outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        iLogo.setImageBitmap(resizedBitmap);
        return true;
    }

    public void onClick(View view){
        int btnClickedId = view.getId();
        DebugWrapper.errorMsg("MyActivity / onClick", " <<>> ");
        //DebugWrapper.errorMsg("MyActiviy / Click bouton"," <<>> ");

        //mSerialManager.disconnect();
        super.onClick(view);
    }

    public void onClickButton(View view){
        int btnClickedId = view.getId();
        DebugWrapper.errorMsg("MyActivity / onClickButton", " <<>> ");
        super.onClick(view);
    }
    
    private void disableBtn(final Button btn){
        if(btn != null){
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    DebugWrapper.errorMsg("MyActiviy / btn false", " <<>> ");
                    btn.setEnabled(false);
                }
            });
        }
    }
    
    private void enableBtn(final Button btn){
        if(btn != null){
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    DebugWrapper.errorMsg("MyActiviy / btn true", " <<>> ");
                    btn.setEnabled(true);
                }
            });
        }
    }

    // *** DATA RECEIVE ***
    @Override
    public void uiOnResponse(final String dataReceived) {

        DebugWrapper.errorMsg("MyActiviy / RECEIVED DATA: " + dataReceived, " <<>> ");

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mValueVspOutTv.append(dataReceived + "\n");
                DebugWrapper.errorMsg("MyActiviy / " + dataReceived, " <<>> ");
                if(dataReceived.contains("\n00\r") || dataReceived.contains("\n01\t")){
                    //mValueVspOutTv.append("\n**************\n");
                }
                recvCnt = recvCnt + 1;
                mRecvCnt.setText(recvCnt.toString());
                String valTemperature = calculate(dataReceived);
                //mValueVspOutTv.append(valTemperature + "\n");
                //mLabTemperature.setText(valTemperature);
                //BackColor(valTemperature);
            }
        });
    }

    public String calculate(String myString) {
        Integer ValA = 0;
        Integer ValB = 0;
        Integer ValC = 0;
        Integer ValD = 0;
        Integer ValE = 0;
        Integer ValF = 0;
        String strTempA = "";
        String strTempB = "";
        String strTempC = "";
        String strTempD = "";
        String strTempE = "";
        String strTempF = "";
        if (myString.length() > 1) {
            Integer IntRange0 = -1;
            Integer IntRange1 = -1;
            Integer IntRange2 = -1;
            Integer IntRange3 = -1;
            Integer IntRange4 = -1;
            Integer IntRange5 = -1;
            try {
                IntRange0 = myString.indexOf("A");
                //ValueReturn = String.valueOf(IntRange0);
            } catch (Exception e) {
                DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
            }
            try {
                IntRange1 = myString.indexOf("B");
                //ValueReturn = String.valueOf(IntRange1);
            } catch (Exception e) {
                DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
            }
            try {
                IntRange2 = myString.indexOf("C");
                //ValueReturn = String.valueOf(IntRange2);
            } catch (Exception e) {
                DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
            }
            try {
                IntRange3 = myString.indexOf("D");
                //ValueReturn = String.valueOf(IntRange3);
            } catch (Exception e) {
                DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
            }
            try {
                IntRange4 = myString.indexOf("E");
                //ValueReturn = String.valueOf(IntRange4);
            } catch (Exception e) {
                DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
            }
            try {
                IntRange5 = myString.indexOf("F");
                //ValueReturn = String.valueOf(IntRange5);
            } catch (Exception e) {
                DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
            }
            if (IntRange0 > IntRange1) {
                return "";
            }
            // Debugging:
            mLabCellVolt.setText(myString);

            String shortStringTitle0;
            String shortStringTitle1;
            String shortStringTitle2;
            String shortStringTitle3;
            String shortStringTitle4;
            String shortStringTitle5;


            // Read ADC values from string
            // Convert string to integer
            // Get input voltage from ADC on BL600 (in mV)
            // Calculate Temperature per voltage (assume linear response with 5mV/°C)
            Integer ADCSc = 10240 / 36;

            if(IntRange0 != -1 && IntRange1 != -1) {
                shortStringTitle0 = myString.substring(IntRange0 + 1, IntRange1);
                ValA = Integer.valueOf(shortStringTitle0);
                ValA = ValA * 1000 / ADCSc;
                ValA = ValA / 5;
                strTempA = ValA.toString() + "°C";
                mTempA.setText(strTempA);
            }
            if(IntRange1 != -1 && IntRange2 != -1) {
                shortStringTitle1 = myString.substring(IntRange1 + 1, IntRange2);
                ValB = Integer.valueOf(shortStringTitle1);
                ValB = ValB * 1000 / ADCSc;
                ValB = ValB / 5;
                strTempB = ValB.toString() + "°C";
                mTempB.setText(strTempB);
            }
            if(IntRange2 != -1) {
                shortStringTitle2 = myString.substring(IntRange2 + 1, myString.length()-1);
                ValC = Integer.valueOf(shortStringTitle2);
                ValC = ValC * 1000 / ADCSc;
                ValC = ValC / 5;
                strTempC = ValC.toString() + "°C";
                mTempC.setText(strTempC);
            }

            if(IntRange3 != -1 && IntRange4 != -1) {
                shortStringTitle3 = myString.substring(IntRange3 + 1, IntRange4);
                ValD = Integer.valueOf(shortStringTitle3);
                ValD = ValD * 1000 / ADCSc;
                ValD = ValD / 5;
                strTempD = ValD.toString() + "°C";
                mTempD.setText(strTempD);
            }
            if(IntRange4 != -1 && IntRange5 != -1) {
                shortStringTitle4 = myString.substring(IntRange4 + 1, IntRange5);
                ValE = Integer.valueOf(shortStringTitle4);
                ValE = ValE * 1000 / ADCSc;
                ValE = ValE / 5;
                strTempE = ValE.toString() + "°C";
                mTempE.setText(strTempE);
            }
            if(IntRange5 != -1) {
                shortStringTitle5 = myString.substring(IntRange5 + 1, myString.length()-1);
                ValF = Integer.valueOf(shortStringTitle5);
                ValF = ValF * 1000 / ADCSc;
                ValF = ValF / 5;
                strTempF = ValF.toString() + "°C";
                mTempF.setText(strTempF);
            }


            DebugWrapper.errorMsg("MyActiviy / calculate: " + strTempA + " " + strTempB + " " + strTempC + " " + strTempD + " " + strTempE + " " + strTempF, " <<>> ");

            /*
            if ((ValX != 0) && (ValB != 0)) {
                double Rt = 0;
                //self.labStatus.text = @"Calcul";
                try {
                    Rt = (550 * ValX * 1000) / ((55 * ValB) - (56 * ValX));
                    //Rt = 10865;
                } catch (Exception e) {
                    DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
                }
                double latdouble = 0;
                Integer count = mResistorArray.length;
                Integer position = 0;
                for (int i = 0; i < count; i++) {
                    latdouble = Double.valueOf(mResistorArray[i]).doubleValue();
                    if (latdouble > Rt) {
                        position = position + 1;
                    }
                }
                position = position - 1;
                double valueAlpha = Double.valueOf(mAlphaArray[position]).doubleValue();
                double valueResistor = Double.valueOf(mResistorArray[position]).doubleValue();
                double valueTemperature = Double.valueOf(mTemperatureArray[position]).doubleValue();

                double TempSecond = 0;
                try {
                    //T=1/(((100/(Alpha*((Tx)*(Tx))))*LN(Rt/Rtx))+(1/Tx))
                    // Alpha = valueAlpha
                    // Rtx = valueResistor
                    // Tx = valueTemperature
                    double TempFirst;
                    double Tempa;
                    double Tempb;
                    double Templn;
                    valueAlpha = valueAlpha / 10;
                    Tempa = 273.15 + valueTemperature;
                    Templn = Math.log(Rt / valueResistor);
                    Tempb = valueAlpha * Tempa * Tempa;
                    TempFirst = 100 / Tempb;
                    TempFirst = TempFirst * Templn;
                    TempFirst = TempFirst + (1 / Tempa);
                    TempSecond = 1 / TempFirst;
                    TempSecond = TempSecond - 273.15;
                } catch (Exception e) {
                    DebugWrapper.errorMsg("ERROR ERROR ERROR", e.getMessage());
                }
                int intTemperature;
                if (TempSecond >= 0) {
                    intTemperature = (int) (TempSecond * 1000);
                } else {
                    intTemperature = (int) (TempSecond * -1000);
                }
                intTemperature += 40; // aroundi
                String strValue = String.valueOf(intTemperature);
                String strstrValue1 = "--";
                String strstrValue2 = "-";
                String strstrValue;
                if (strValue.length() < 3) {
                    strstrValue1 = "0";
                    strstrValue2 = "0";
                }
                if (strValue.length() == 3) {
                    strstrValue1 = "0";
                    strstrValue2 = strValue.substring(0, 1);
                }
                if (strValue.length() == 4) {
                    strstrValue1 = strValue.substring(0, 1);
                    strstrValue2 = strValue.substring(1, 2);
                }
                if (strValue.length() == 5) {
                    strstrValue1 = strValue.substring(0, 2);
                    strstrValue2 = strValue.substring(2, 3);
                }
                if (TempSecond >= 0) {
                    strstrValue = strstrValue1 + "." + strstrValue2 + "°C";
                } else {
                    strstrValue = "-" + strstrValue1 + "." + strstrValue2 + "°C";
                }
                if (strstrValue2 != "-") {
                    ValueReturn = strstrValue;
                    intWaiting = 0;
                    /////////[[self view] setBackgroundColor:[_currentJaTemp background:strstrValue]];
                    //NSDate *saveDate = [[NSDate alloc] init];
                    //NSLog(@"€= <<<< %@ >>>> %@ ==", strstrValue, saveDate);
                } else {
                    ValueReturn = "ERROR";
                }
            } else {
                intWaiting = intWaiting + 1;
                if (intWaiting > 4) {
                    ValueReturn = "WAITING";
                    intWaiting = 0;
                }
            }*/
        }

        String ValueReturn = "";
        return ValueReturn;
     }

    public void BackColor(String myTemp) {
        myTemp = myTemp.substring(0,myTemp.length()-4);
        int Temperature = 99;
        try {
            Temperature = Integer.parseInt(myTemp);
        } catch(NumberFormatException nfe) {
        }
        int ColorRed = 255;
        int ColorGreen = 255;
        int ColorBlue = 255;
        switch (Temperature) {
            case 33:
            case 32:
            case 31:
            case 30:
            case 29:
            case 28:
                ColorRed = 102;
                ColorGreen = 27;
                ColorBlue = 28;
                break;
            case 27:
            case 26:
                ColorRed = 162;
                ColorGreen = 50;
                ColorBlue = 40;
                break;
            case 25:
            case 24:
                ColorRed = 221;
                ColorGreen = 120;
                ColorBlue = 52;
                break;
            case 23:
            case 22:
                ColorRed = 229;
                ColorGreen = 144;
                ColorBlue = 61;
                break;
            case 21:
            case 20:
                ColorRed = 233;
                ColorGreen = 170;
                ColorBlue = 103;
                break;
            case 19:
            case 18:
                ColorRed = 242;
                ColorGreen = 207;
                ColorBlue = 117;
                break;
            case 17:
            case 16:
                ColorRed = 227;
                ColorGreen = 219;
                ColorBlue = 105;
                break;
            case 15:
            case 14:
                ColorRed = 221;
                ColorGreen = 224;
                ColorBlue = 69;
                break;
            case 13:
            case 12:
                ColorRed = 194;
                ColorGreen = 208;
                ColorBlue = 75;
                break;
            case 11:
            case 10:
                ColorRed = 148;
                ColorGreen = 194;
                ColorBlue = 79;
                break;
            case 9:
            case 8:
                ColorRed = 131;
                ColorGreen = 197;
                ColorBlue = 197;
                break;
            case 7:
            case 6:
                ColorRed = 127;
                ColorGreen = 202;
                ColorBlue = 244;
                break;
            case 5:
            case 4:
                ColorRed = 92;
                ColorGreen = 123;
                ColorBlue = 188;
                break;
            case 3:
            case 2:
                ColorRed = 71;
                ColorGreen = 82;
                ColorBlue = 169;
                break;
            case 1:
            case 0:
                ColorRed = 63;
                ColorGreen = 81;
                ColorBlue = 162;
                break;
            case -1:
            case -2:
                ColorRed = 111;
                ColorGreen = 79;
                ColorBlue = 151;
                break;
            case -3:
            case -4:
            case -5:
            case -6:
                ColorRed = 153;
                ColorGreen = 90;
                ColorBlue = 156;
                break;
        }
        bkLayout.setBackgroundColor(Color.rgb(ColorRed, ColorGreen, ColorBlue));
    }


        @Override
    public void uiServiceVSPFound(BluetoothGatt gatt) {
        //DebugWrapper.toastMsg(this, "VSP Service found");
        DebugWrapper.errorMsg("MyActiviy / VSP Service found", " <<>> ");
        //mLabStatus.setText("Connect");
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                enableBtn(mBtnSend);
            }
        });
        uiInvalidateBtnState();
    }
    
    @Override
    public void uiServiceVSPNotFound(BluetoothGatt gatt) {
        DebugWrapper.toastMsg(this, "VSP Service not found!!");
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                disableBtn(mBtnSend);
            }
        });
        uiInvalidateBtnState();
        
    }
    
    @Override
    public void uiModuleVSPCharsFound(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiModuleVSPCharsNotFound(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiStartScanning() {
        uiInvalidateBtnState();
    }
    
    @Override
    public void uiStopScanning() {
        uiInvalidateBtnState();
    }
    
    @Override
    public void uiOnLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        DebugWrapper.errorMsg("MyActiviy / Handle Found Device: ", device.toString());
        handleFoundDevice(device, rssi, scanRecord);
    }
    
    @Override
    public void uiOnConnectionStateChangeConnected(BluetoothGatt gatt) {
        uiInvalidateBtnState();
    }
    
    @Override
    public void uiOnConnectionStateChangeDisconnected(BluetoothGatt gatt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableBtn(mBtnSend);
            }
        });
        uiInvalidateBtnState();
    }
    
    @Override
    public void uiOnConnectionStateChangeConnecting(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnConnectionStateChangeDisconnecting(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnConnectionStateChangeFailure(BluetoothGatt gatt,
                                                 int status, int newState) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnReadRemoteRssiSuccess(BluetoothGatt gatt, int rssi) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnReadRemoteRssiFailure(BluetoothGatt gatt, int rssi,
                                          int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnServicesDiscoveredSuccess(BluetoothGatt gatt) {
        uiInvalidateBtnState();
    }
    
    @Override
    public void uiOnServicesDiscoveredFailure(BluetoothGatt gatt, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnDescriptorReadSuccess(BluetoothGatt gatt,
                                          BluetoothGattDescriptor ch) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnDescriptorReadFailure(BluetoothGatt gatt,
                                          BluetoothGattDescriptor ch, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnDescriptorWriteSuccess(BluetoothGatt gatt,
                                           BluetoothGattDescriptor descriptor) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnDescriptorWriteFailure(BluetoothGatt gatt,
                                           BluetoothGattDescriptor descriptor, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnCharacteristicWriteSuccess(BluetoothGatt gatt,
                                               BluetoothGattCharacteristic ch) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnCharacteristicWriteFailure(BluetoothGatt gatt,
                                               BluetoothGattCharacteristic ch, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnCharacteristicReadSuccess(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic ch) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnCharacteristicReadFailure(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic ch, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnReliableWriteCompletedSuccess(BluetoothGatt gatt) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void uiOnReliableWriteCompletedFailure(BluetoothGatt gatt, int status) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected int setContentView() {
        // TODO Auto-generated method stub
        return R.layout.activity_my;
    }
    
    @Override
    protected Activity setActivity() {
        // TODO Auto-generated method stub
        return this;
    }
    
    @Override
    protected LairdGapBase setLairdGapBase() {
        // TODO Auto-generated method stub
        return mSerialManager = new SerialManager(this, this, this, MAX_DATA_TO_READ_FROM_RX_BUFFER);
    }
    
    @Override
    protected void onScanningCancel() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBackPressed(){
        if(mSerialManager.isConnected() == true){
            mSerialManager.disconnect();
        }
        disableBtn(mBtnSend);
        uiInvalidateBtnState();
        finish();
    }
}