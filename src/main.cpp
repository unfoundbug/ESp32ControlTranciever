#include <Arduino.h>

#include "globals.hpp"
#include "Core0.hpp"
#include "Core1.hpp"

void setup() 
{
  Serial.begin(115200);
  Serial.println("Init Globals");
  // put your setup code here, to run once:
  InitialiseGlobals();

  Serial.println("Init Core1");

  // Core 1 is responsible for communications
  InitialiseCore1();

  Serial.println("Init Core0");
  // Core 0 is responsible for WorldIO
  InitialiseCore0();
}

void loop() {
  // put your main code here, to run repeatedly:
  RunCore1();
}