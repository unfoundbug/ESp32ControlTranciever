#include "MotorControl.hpp"

#include <Arduino.h>

MotorControl::MotorControl(int pin1, int pin2, int pwmChan1, int pwmChan2, int driveRamp)
{

    currentPower = 0;

    this->chan1 = pwmChan1;
    this->chan2 = pwmChan2;
    this->driveRamp = driveRamp;

    ledcSetup(pwmChan1, 5000, 10);
    ledcAttachPin(pin1, pwmChan1);
    ledcWrite(pwmChan1, 0);

    ledcSetup(pwmChan2, 5000, 10);
    ledcAttachPin(pin2, pwmChan2);
    ledcWrite(pwmChan2, 0);

}

void MotorControl::WritePower(int newPower)
{
    if(newPower > currentPower){
        currentPower += driveRamp;
    }
    else if(newPower < currentPower){
        currentPower -= driveRamp;
    }
    Serial.print("Motor Update: ");
    Serial.print(currentPower);
    Serial.print(" ");
    Serial.println(newPower);
    int targetPower = currentPower;
    if(targetPower == 0){
        ledcWrite(this->chan1, 0);
        ledcWrite(this->chan2, 0);
        currentPower = 0;
    }
    else{
        int targetChannel = this->chan1;

        if(targetPower < 0)
        {
            targetPower *= -1;
            targetChannel = this->chan2;
        }
        ledcWrite(targetChannel, targetPower);
    }
}