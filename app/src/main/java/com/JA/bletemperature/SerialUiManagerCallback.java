package com.JA.bletemperature;

import com.JA.bletemperature.vspservice.VSPUiCallback;



public interface SerialUiManagerCallback extends VSPUiCallback{
    public void uiOnResponse(String dataReceived);
}