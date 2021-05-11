#include "globals.hpp"

volatile bool b_EnableLights;

volatile bool b_Input_Pedal;
volatile bool b_Input_Switch;
volatile bool b_Input_Gear;
volatile bool b_Input_Rocker_Manual;
volatile bool b_Input_Rocker_Remote;

volatile int i_DriveTargetPower;
volatile int i_SteerPower;

volatile float f_PowerVolt;
volatile float f_PowerAmp;

volatile bool b_LocalControl;

void InitialiseGlobals(){
    b_EnableLights = false;
    b_Input_Pedal = false;
    b_Input_Switch = false;
    b_Input_Gear = false;
    b_Input_Rocker_Manual = false;
    b_Input_Rocker_Remote = false;

    i_DriveTargetPower = 0;
    i_SteerPower = 0;
    
    f_PowerAmp = 0;
    f_PowerVolt = 0;

    b_LocalControl = false;
}