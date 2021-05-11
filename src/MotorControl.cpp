#include "MotorControl.hpp"

#include <Arduino.h>

MotorControl::MotorControl(int pin1, int pin2, int pwmChan1, int pwmChan2)
{
    this->chan1 = pwmChan1;
    this->chan2 = pwmChan2;

    ledcSetup(pwmChan1, 5000, 10);
    ledcAttachPin(pin1, pwmChan1);
    ledcWrite(pwmChan1, 0);

    ledcSetup(pwmChan2, 5000, 10);
    ledcAttachPin(pin2, pwmChan2);
    ledcWrite(pwmChan2, 0);

}

void MotorControl::WritePower(int newPower)
{
    if(newPower > 0 && currentPower < 0)
    {
        newPower = 0;
    }
    if(newPower < 0 && currentPower > 0)
    {
        newPower = 0;
    }

    currentPower = newPower;

    if(newPower == 0){
        ledcWrite(this->chan1, 0);
        ledcWrite(this->chan2, 0);
    }

    int targetChannel = this->chan1;

    if(newPower < 0)
    {
        newPower *= -1;
        targetChannel = this->chan2;
    }

    ledcWrite(targetChannel, newPower);
}