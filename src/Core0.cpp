#include "Core0.hpp"

#include <Wire.h>
#include <esp_task_wdt.h>
#include <PCF8574.h>
#include <Adafruit_INA219.h>

PCF8574 PCF(0x22);
Adafruit_INA219 ina219;

TaskHandle_t secondCoreTask;


void Core0Run(void*);
void Core0Setup();
void Core0Loop();
void i2cScan();

void GpioUpdate();
void PwmUpdate();
void PowerUpdate();

unsigned long l_NextGPIORun = 0;
unsigned long GpioUpdateTime = 100;

unsigned long l_NextPwmRun = 0;
unsigned long PwmUpdateTime = 50;

unsigned long l_NextPowerRun = 0;
unsigned long PowerUpdateTime = 250;

void InitialiseCore0(){
    xTaskCreatePinnedToCore(
      Core0Run, /* Function to implement the task */
      "Core0Run", /* Name of the task */
      10000,  /* Stack size in words */
      NULL,  /* Task input parameter */
      0,  /* Priority of the task */
 
      &secondCoreTask,  /* Task handle. */
      0); /* Core where the task should run */
}


void Core0Run(void*){
    Core0Setup();
    for(;;){
        Core0Loop();
        delay(10);
    }
}

void Core0Setup(){
    Wire.begin(27,26);
    i2cScan();
    PCF.begin();
    if(PCF.isConnected()){
        Serial.println("Connected to remote GPIO");
        PCF.write8(0b00011111);
        Serial.println("Pullup state sent");
    }
    if (ina219.begin()) {
        Serial.println("Connected to INA219 chip");
    }   
}

void Core0Loop(){
 unsigned long thisMillis = millis();
 if(thisMillis > l_NextGPIORun){
     l_NextGPIORun = thisMillis + GpioUpdateTime;
     GpioUpdate();    
 }

 if(thisMillis > l_NextPwmRun){
     l_NextPwmRun = thisMillis + PwmUpdateTime;
     //Update motor drive
     PwmUpdate();
 }

 if(thisMillis > l_NextPowerRun){
     l_NextPowerRun = thisMillis + PowerUpdateTime;
     PowerUpdate();
 }
}

void i2cScan(){
    byte error, address; //variable for error and I2C address
  int nDevices;

  Serial.println("Scanning...");

  nDevices = 0;
  for (address = 1; address < 127; address++ )
  {
    // The i2c_scanner uses the return value of
    // the Write.endTransmisstion to see if
    // a device did acknowledge to the address.
    Wire.beginTransmission(address);
    error = Wire.endTransmission();

    if (error == 0)
    {
      Serial.print("I2C device found at address 0x");
      if (address < 16)
        Serial.print("0");
      Serial.print(address, HEX);
      Serial.println("  !");
      nDevices++;
    }
    else if (error == 4)
    {
      Serial.print("Unknown error at address 0x");
      if (address < 16)
        Serial.print("0");
      Serial.println(address, HEX);
    }
  }
  if (nDevices == 0)
    Serial.println("No I2C devices found\n");
  else
    Serial.println("done\n");
}

void GpioUpdate(){
    //Fetch GPIO
     uint8_t gpioState = PCF.read8();
     bool bPedal = gpioState & 0b0000001;
     bool bSwitch = gpioState & 0b0000010;
     bool bGear = gpioState & 0b0000100;
     bool bRockerManual = gpioState & 0b0001000;
     bool bRockerRemote = gpioState & 0b0010000;
     bool controlChanged = false;
     
    // PCF8574 is pulled up and driven to ground, logic is inverted here
    
     if(bPedal == b_Input_Pedal){
         b_Input_Pedal = !bPedal;
         controlChanged = true;
     }

     if(bSwitch == b_Input_Switch){
         b_Input_Switch = !bSwitch;
         controlChanged = true;
     }

     if(bGear == b_Input_Gear){
         b_Input_Gear = !bGear;
         controlChanged = true;
     }

     if(bRockerManual == b_Input_Rocker_Manual){
         b_Input_Rocker_Manual = !bRockerManual;
         controlChanged = true;
     }

     if(bRockerRemote == b_Input_Rocker_Remote){
         b_Input_Rocker_Remote = !bRockerRemote;
         controlChanged = true;
     }

     if(b_LocalControl){
         if(b_Input_Pedal)
         {
             i_DriveTargetPower = i_DriveLimit;
         }
         else
         {
            i_DriveTargetPower = 0;
         }
         if(b_Input_Gear){
             i_DriveTargetPower *= -1;
         }

     }

}

void PwmUpdate(){

}
void PowerUpdate(){
  float shuntvoltage = 0;
  float busvoltage = 0;
  float current_mA = 0;
  float loadvoltage = 0;
  
  shuntvoltage = ina219.getShuntVoltage_mV();
  busvoltage = ina219.getBusVoltage_V();
  current_mA = ina219.getCurrent_mA();
  loadvoltage = busvoltage + (shuntvoltage / 1000);
  
  f_PowerVolt = loadvoltage;
  f_PowerAmp = current_mA / 1000;
}