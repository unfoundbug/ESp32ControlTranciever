#pragma once

class MotorControl
{
private:
    int driveRamp;
    int currentPower;
    int chan1, chan2;
    int chanEnable;
public:
   
    MotorControl(int driver_en, int left_Pwm, int right_Pwm, int pwmChan1, int pwmChan2, int driveRamp);

    void WritePower(int newPower);
};