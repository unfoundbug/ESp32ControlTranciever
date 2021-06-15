#include <Arduino.h>

#include "globals.hpp"
#include "Core0.hpp"
#include "Core1.hpp"
#include "soc/soc.h"
#include "soc/rtc_cntl_reg.h"

void setup() 
{

  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 0); //disable brownout detector   

  // let things power up
  delay(500);

  WRITE_PERI_REG(RTC_CNTL_BROWN_OUT_REG, 1); //enable brownout detector

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