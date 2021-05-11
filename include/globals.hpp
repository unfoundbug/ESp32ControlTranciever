#pragma once

extern volatile bool b_EnableLights;

extern volatile bool b_Input_Pedal;
extern volatile bool b_Input_Switch;
extern volatile bool b_Input_Gear;
extern volatile bool b_Input_Rocker_Manual;
extern volatile bool b_Input_Rocker_Remote;

extern volatile int i_DriveTargetPower;
extern volatile int i_SteerPower;
extern volatile float f_PowerVolt;
extern volatile float f_PowerAmp;

extern volatile bool b_LocalControl;

void InitialiseGlobals();