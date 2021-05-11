#pragma once

class MotorControl
{
private:
    int currentPower;

    int chan1, chan2;
public:
   
    MotorControl(int pin1, int pin2, int pwmChan1, int pwmChan2);

    void WritePower(int newPower);
};