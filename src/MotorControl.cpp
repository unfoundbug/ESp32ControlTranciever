#include "MotorControl.hpp"

#include <Arduino.h>

MotorControl::MotorControl(int driver_en, int left_Pwm, int right_Pwm, int pwmChan1, int pwmChan2, int driveRamp)
{

    currentPower = 0;

    this->chan1 = pwmChan1;
    this->chan2 = pwmChan2;
    this->chanEnable = driver_en;
    this->driveRamp = driveRamp;

    ledcSetup(pwmChan1, 5000, 10);
    ledcAttachPin(left_Pwm, pwmChan1);
    ledcWrite(pwmChan1, 0);

    ledcSetup(pwmChan2, 5000, 10);
    ledcAttachPin(right_Pwm, pwmChan2);
    ledcWrite(pwmChan2, 0);

    pinMode(this->chanEnable, GPIO_MODE_OUTPUT);

}

void MotorControl::WritePower(int newPower)
{
    if(newPower > currentPower){
        currentPower += driveRamp;
    }
    else if(newPower < currentPower){
        currentPower -= driveRamp;
    }
    int targetPower = currentPower;
    if(targetPower == 0){
        ledcWrite(this->chan1, 0);
        ledcWrite(this->chan2, 0);
        digitalWrite(this->chanEnable, 0);
        currentPower = 0;
    }
    else{
        int targetChannel = this->chan1;
        if(targetPower < 0)
        {
            targetPower *= -1;
            targetChannel = this->chan2;
            
        }

        digitalWrite(this->chanEnable, 1);
        ledcWrite(targetChannel, targetPower);
    }
}