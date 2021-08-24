#include "Core1.hpp"
#include <Arduino.h>
#include <ArduinoJson.h>
#include <WiFi.h>

#include <BluetoothSerial.h>

#include "esp_bt_main.h"
#include "esp_bt_device.h"

#include <IBusBM.h>

unsigned long sendUpdate = 0;
unsigned long updateInterval = 500;

DynamicJsonDocument baseDocument(2048);
BluetoothSerial SerialBT;

IBusBM IBus;    // IBus object

bool bWasConnected = false;

int radioDrive, radioSteer, radioRev, radioEn, radioLights;

void SendUpdateToClient(){

    JsonObject objControls = baseDocument["Controls"];
    JsonObject objDrive = baseDocument["Drive"];
    JsonObject objPower = baseDocument["Power"];
    JsonObject objRC = baseDocument["RC"];

    objControls["Pedal"] = b_Input_Pedal;
    objControls["Switch"] = b_Input_Switch;
    objControls["ToggleManual"] = b_Input_Rocker_Manual;
    objControls["ToggleRemote"] = b_Input_Rocker_Remote;
    objControls["Gear"] = b_Input_Gear;
    objDrive["Target"] = i_DriveTargetPower;
    objDrive["Steer"] = i_SteerPower;
    objDrive["Lights"] = b_EnableLights;
    objPower["Volt"] = f_PowerVolt;
    objPower["Amp"] = f_PowerAmp;

    objRC["radioDrive"] = radioDrive;
    objRC["radioSteer"] = radioSteer;
    objRC["radioRev"] = radioRev;
    objRC["radioEn"] = radioEn;
    objRC["radioLights"] = radioLights;


    String output;
    serializeJson(baseDocument, output);
    if(SerialBT.hasClient()){
        SerialBT.print(output);
        SerialBT.print('\n');
    }
    else{
        Serial.println(output);
    }
}

void InitialiseCore1(){

    Serial.println("Disabling WiFi core");
    WiFi.mode(WIFI_MODE_NULL);
    Serial.println("Starting BT core");
    SerialBT.begin("Imrie_Car");
    Serial.println("Started BT core");
    
    const uint8_t* point = esp_bt_dev_get_address();
    Serial.print("Got BT core: ");
  for (int i = 0; i < 6; i++) {
 
    char str[3];
 
    sprintf(str, "%02X", (int)point[i]);
    Serial.print(str);
 
    if (i < 5){
      Serial.print(":");
    }
 
  }
    Serial.println();
 

    JsonObject objControls = baseDocument.createNestedObject("Controls");
    JsonObject objDrive = baseDocument.createNestedObject("Drive");
    JsonObject objPower = baseDocument.createNestedObject("Power");
    JsonObject objRC = baseDocument.createNestedObject("RC");
    
    IBus.begin(Serial2,IBUSBM_NOTIMER, 18,17);    // iBUS object connected to serial2 RX2 pin and use timer 1

    Serial.println("Initialised RC Core");
}

DynamicJsonDocument rcvDoc(512);

void RunCore1(){
    unsigned long thisMillis = millis();

    IBus.loop();

    radioDrive = IBus.readChannel(2);
    radioSteer = IBus.readChannel(3);
    radioRev = IBus.readChannel(6);
    radioEn = IBus.readChannel(7);
    radioLights = IBus.readChannel(4);

    if(radioEn == 2000)
    {
        if(radioRev == 1500)
            i_DriveTargetPower = 0;
        else if(radioRev == 2000)
            i_DriveTargetPower = (1000 - radioDrive);    
        else
            i_DriveTargetPower = (radioDrive - 1000);
        
        i_SteerPower = (radioSteer - 1500) * 2;
        b_EnableLights = radioLights > 1500;
    }

    if(thisMillis > sendUpdate){
        sendUpdate = thisMillis + updateInterval;
        SendUpdateToClient();
    }
    if(SerialBT.hasClient()){
        //Check for read
        if(SerialBT.available()){
            String cmd = SerialBT.readStringUntil('\n');
            Serial.print(cmd);

            DeserializationError processResult = deserializeJson(rcvDoc, cmd);
            if(processResult.code() == DeserializationError::Code::Ok){
            JsonObject response = rcvDoc["Control"];
                b_LocalControl = response.getMember("LocalControl");
                if(!b_LocalControl && (radioEn != 2000)){
                    int newSteer = response.getMember("Steer").as<int>() * 1000;
                    int newDrive = response.getMember("Drive").as<int>() * 1000;
                    if(newSteer != i_SteerPower && newDrive != i_DriveTargetPower)
                    {
                        i_DriveTargetPower = newDrive;
                    } 
                    else
                    {
                        i_DriveTargetPower = newDrive;
                        i_SteerPower = newSteer;
                    }
                    b_EnableLights = response.getMember("Lights").as<bool>();
                }
                SerialBT.println("OK");
                Serial.println("OK");
            }
            else{
                SerialBT.print("ERROR:");
                SerialBT.println(processResult.c_str());
                Serial.println("ERROR");
            }
            
        }
        bWasConnected = true;
    }
    else{
        if(bWasConnected){
            Serial.println("Disconnection, disabling output");
            bWasConnected = false;
            i_DriveTargetPower = 0;
            i_SteerPower = 0;
            b_EnableLights = false;
        }
    }
}