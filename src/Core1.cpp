#include "Core1.hpp"
#include <Arduino.h>
#include <ArduinoJson.h>
#include <WiFi.h>

#include <BluetoothSerial.h>

#include "esp_bt_main.h"
#include "esp_bt_device.h"

unsigned long sendUpdate = 0;
unsigned long updateInterval = 500;

DynamicJsonDocument baseDocument(2048);
BluetoothSerial SerialBT;


void SendUpdateToClient(){

    JsonObject objControls = baseDocument["Controls"];
    JsonObject objDrive = baseDocument["Drive"];
    JsonObject objPower = baseDocument["Power"];

    objControls["Pedal"] = b_Input_Pedal;
    objControls["Switch"] = b_Input_Switch;
    objControls["ToggleManual"] = b_Input_Rocker_Manual;
    objControls["ToggleRemote"] = b_Input_Rocker_Remote;
    objControls["Gear"] = b_Input_Gear;
    objDrive["Target"] = i_DriveTargetPower;
    objDrive["Lights"] = b_EnableLights;
    objPower["Volt"] = f_PowerVolt;
    objPower["Amp"] = f_PowerAmp;

    String output;
    serializeJson(baseDocument, output);
    if(SerialBT.hasClient()){
        SerialBT.print(output);
        SerialBT.print('\n');
    }
    else{
        //Serial.println(output);
    }
}

void InitialiseCore1(){

    Serial.println("Disabling WiFi core");
    WiFi.mode(WIFI_MODE_NULL);
    Serial.println("Starting BT core");
    SerialBT.begin("Imrie_Car");
    SerialBT.enableSSP();

  const uint8_t* point = esp_bt_dev_get_address();
 
  for (int i = 0; i < 6; i++) {
 
    char str[3];
 
    sprintf(str, "%02X", (int)point[i]);
    Serial.print(str);
 
    if (i < 5){
      Serial.print(":");
    }
 
    Serial.println();
  }
 

    JsonObject objControls = baseDocument.createNestedObject("Controls");
    JsonObject objDrive = baseDocument.createNestedObject("Drive");
    JsonObject objPower = baseDocument.createNestedObject("Power");
    objControls["Pedal"] = 0;
    objControls["Switch"] = 0;
    objControls["ToggleManual"] = 0;
    objControls["ToggleRemote"] = 0;
    objControls["Gear"] = 0;
    objDrive["Current"] = 0;
    objDrive["Target"] = 0;
    objDrive["Limit"] = 0;
    objDrive["Lights"] = 0;
    objDrive["Mode"] = "Remote";
    objPower["Volt"] = 11.1;
    objPower["Amp"] = 1.0;
}

DynamicJsonDocument rcvDoc(512);

void RunCore1(){
    unsigned long thisMillis = millis();
    if(thisMillis > sendUpdate){
        sendUpdate = thisMillis + updateInterval;
        SendUpdateToClient();
    }
    if(SerialBT.hasClient()){
        //Check for read
        if(SerialBT.available()){
            String cmd = SerialBT.readStringUntil('\n');
            deserializeJson(rcvDoc, cmd);
            JsonObject response = rcvDoc["Control"];
            b_LocalControl = response.getMember("LocalControl");
            if(!b_LocalControl){
                i_DriveTargetPower = response.getMember("Drive").as<int>() * 1000;
                i_SteerPower = response.getMember("Steer").as<int>() * 1000;
                b_EnableLights = response.getMember("Lights").as<bool>();
            }
        }
    }
}