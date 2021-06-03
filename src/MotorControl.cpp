#include "MotorControl.hpp"

#include <Arduino.h>

MotorControl::MotorControl(int left_En, int left_Pwm, int right_En, int right_Pwm, int pwmChan1, int pwmChan2, int driveRamp)
{

    currentPower = 0;

    this->chan1 = pwmChan1;
    this->chan2 = pwmChan2;
    this->chan1Enable = left_En;
    this->chan2Enable = right_En;
    this->driveRamp = driveRamp;

    ledcSetup(pwmChan1, 5000, 10);
    ledcAttachPin(left_Pwm, pwmChan1);
    ledcWrite(pwmChan1, 0);

    ledcSetup(pwmChan2, 5000, 10);
    ledcAttachPin(right_Pwm, pwmChan2);
    ledcWrite(pwmChan2, 0);

    pinMode(this->chan1Enable, GPIO_MODE_OUTPUT);
    pinMode(this->chan2Enable, GPIO_MODE_OUTPUT);

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
        digitalWrite(this->chan1Enable, 0);
        digitalWrite(this->chan2Enable, 0);
        currentPower = 0;
    }
    else{
        int targetChannel = this->chan1;

        if(targetPower < 0)
        {
            targetPower *= -1;
            targetChannel = this->chan2;
            digitalWrite(this->chan2Enable, 1);
        }
        else{
         digitalWrite(this->chan1Enable, 1);
        }

        ledcWrite(targetChannel, targetPower);
    }
}