#pragma once

class MotorControl
{
private:
    int driveRamp;
    int currentPower;
    int chan1, chan2;
    int chan1Enable, chan2Enable;
public:
   
    MotorControl(int left_En, int left_Pwm, int right_En, int right_Pwm, int pwmChan1, int pwmChan2, int driveRamp);

    void WritePower(int newPower);
};