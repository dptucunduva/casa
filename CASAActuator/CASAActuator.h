#ifndef CASAActuator_h
#define CASAActuator_h

/** Global command control */
unsigned long time;
boolean enabled = true;
unsigned long buttonPressed = 0;
// Command code definition
const int COMMAND_EMPTY = 0;
const int COMMAND_ENABLE = 99;
const int COMMAND_CHECK = 98;
const int COMMAND_GENERIC_IR = 3;
const int COMMAND_GENERIC_RELAY = 4;
// IR command
const String GENERIC_IR = "IR";
// Relay command
const String GENERIC_RELAY = "RL";
// For how long the touch button should be pressed to trigger a RING command to PC
const int BUTTON_PRESSED_TIME = 3000;

/** Pin definition */
const int PIN_RELAY_1 = 35;
const int PIN_RELAY_2 = 36;
const int PIN_RELAY_3 = 37;
const int PIN_RELAY_4 = 38;
const int PIN_RELAY_5 = 39;
const int PIN_RELAY_6 = 40;
const int PIN_RELAY_7 = 41;
const int PIN_RELAY_8 = 42;
const int RELAY_ARRAY[] = {PIN_RELAY_1, PIN_RELAY_2, PIN_RELAY_3, PIN_RELAY_4, PIN_RELAY_5, PIN_RELAY_6, PIN_RELAY_7, PIN_RELAY_8};
const int PIN_ENABLED_INDICATOR = 28;
const int PIN_GLOBAL_SWITCH = 52;
const int PIN_GLOBAL_SWITCH_LED = 53;
const int PIN_SWITCH_BUTTON = 2;
const int PIN_IR_RECV = 6;

/** IRLib receving feature **/
IRrecv irrecv(PIN_IR_RECV);
decode_results results;

/** IRLib sending feature **/
IRsend irsend;

/** BitVoicer entries */
BitVoicerSerial bitVoicer;

#endif
