#include <IRremote.h>
#include <BitVoicer11.h>
#include "CASAActuator.h"

//The setup function is called once at startup of the sketch
void setup()
{
  // Start Serial
  Serial.begin(9600);

  // Start BitVoicer Serial communication
  bitVoicer = BitVoicerSerial();

  // Pin setup
  pinMode(PIN_RELAY_1, OUTPUT);
  pinMode(PIN_RELAY_2, OUTPUT);
  pinMode(PIN_RELAY_3, OUTPUT);
  pinMode(PIN_RELAY_4, OUTPUT);
  pinMode(PIN_RELAY_5, OUTPUT);
  pinMode(PIN_RELAY_6, OUTPUT);
  pinMode(PIN_RELAY_7, OUTPUT);
  pinMode(PIN_RELAY_8, OUTPUT);
  pinMode(PIN_ENABLED_INDICATOR, OUTPUT);
  pinMode(PIN_GLOBAL_SWITCH, INPUT);
  pinMode(PIN_IR_RECV, INPUT);

  // Reset pins and check if global operation is enabled or not
  resetPins();

  // Enable IR receiving
  irrecv.enableIRIn();

  // Setup "interrupt mode" for switch button
  pinMode(PIN_SWITCH_BUTTON, INPUT);
  attachInterrupt(digitalPinToInterrupt(PIN_SWITCH_BUTTON), commandButtonStateChanged, CHANGE);
}

// The loop function is called in an endless loop
void loop()
{
  // get command nature
  int type = getCommandNature();

  // Only analyse command if global switch is enabled
  if (type == COMMAND_CHECK) {
        if (enabled) {
          Serial.print("E;");
        } else {
          Serial.print("D;");
        }
  } else if (enabled) {
    // Check command
    if (type == COMMAND_ENABLE) {
        time = millis() + bitVoicer.strData.substring(1).toInt();
        digitalWrite(PIN_ENABLED_INDICATOR, HIGH);
    } else if (time > millis()) {
        if (type == COMMAND_GENERIC_IR) {
            handleGenericIR(bitVoicer.strData);
        } else if (type == COMMAND_GENERIC_RELAY) {
            handleGenericRelay(bitVoicer.strData);
        } else if (type == COMMAND_EMPTY) {
            // nothing to do!
        }
    } else {
        digitalWrite(PIN_ENABLED_INDICATOR, LOW);
    }
  }

  // Ensure that all ditital outs are set to HIGH
  resetPins();

  // Check if button is pressed continuously (more than 3s) - that should trigger a command as well.
  if (buttonPressed > 0 && buttonPressed + BUTTON_PRESSED_TIME < millis()) {
    Serial.print("R;");
    buttonPressed = 0;
  }

  // Check if there is a IR code reading
  receiveIR();
}

// Reset pins and global switch
void resetPins() {
  digitalWrite(PIN_RELAY_1, HIGH);
  digitalWrite(PIN_RELAY_2, HIGH);
  digitalWrite(PIN_RELAY_3, HIGH);
  digitalWrite(PIN_RELAY_4, HIGH);
  digitalWrite(PIN_RELAY_5, HIGH);
  digitalWrite(PIN_RELAY_6, HIGH);
  digitalWrite(PIN_RELAY_7, HIGH);
  digitalWrite(PIN_RELAY_8, HIGH);

  // Check global enabled/disabled switch
  checkGlobalSwitch();
}

// Check if the command button was pressed
void commandButtonStateChanged() {
  if (enabled) {
    // If singal is HIGH, that means that the button is pressed
    if (digitalRead(PIN_SWITCH_BUTTON) == HIGH) {
      // Set the time that the button was pressed, so that we check wat to do when it is released
      buttonPressed = millis();
    } else if (digitalRead(PIN_SWITCH_BUTTON) == LOW) {
      // That means that the button was pressed and now was released.
      // If it was released in less than 3 seconds, trigger a simples "button pressed command"
      if (buttonPressed > millis()-BUTTON_PRESSED_TIME) {
        Serial.print("B;");
      }
      buttonPressed = 0;
    }
  }
}

// This is the global switch interrupt function
void checkGlobalSwitch() {
  if (digitalRead(PIN_GLOBAL_SWITCH) == LOW) {
    enabled = false;
    digitalWrite(PIN_GLOBAL_SWITCH_LED, LOW);
  } else {
    enabled = true;
    digitalWrite(PIN_GLOBAL_SWITCH_LED, HIGH);
  }
}

// Receive IR code
void receiveIR() {
  if (irrecv.decode(&results)) {
    // Send the command only if global switch is set to false
    if (!enabled) {
      Serial.print("I");
      Serial.print(getIRCommandFamily(results.decode_type));
      Serial.print("|0x");
      Serial.print(results.value, HEX);
      Serial.print("|");
      Serial.print(results.bits, DEC);
      Serial.print(";");
    }
    irrecv.resume(); // Receive the next value
  }
}

// Retrieve IR family code constant
String getIRCommandFamily(int intCode) {
  String code = "UNKNOWN";
  
  switch(intCode) {
    case RC5:
      code = "RC5";
      break;
    case RC6:
      code = "RC6";
      break;
    case NEC:
      code = "NEC";
      break;
    case SONY:
      code = "SONY";
      break;
    case PANASONIC:
      code = "PANASONIC";
      break;
    case JVC:
      code = "JVC";
      break;
    case SAMSUNG:
      code = "SAMSUNG";
      break;
    case WHYNTER:
      code = "WHYNTER";
      break;
    case AIWA_RC_T501:
      code = "AIWA_RC_T501";
      break;
    case LG:
      code = "LG";
      break;
    case DENON:
      code = "DENON";
      break;
    default:
      code = "NOT_SUPPORTED";
      break;
  }

  return code;
}

// Get BitVoice command
int getCommandNature() {
    byte dataType = bitVoicer.getData();
    // All commands are sent as strings
    if (dataType == BV_STR) {
        // We have a command. Check its nature
        if (bitVoicer.strData.startsWith("E")) {
            return COMMAND_ENABLE;
        } else if (bitVoicer.strData.startsWith("C")) {
            return COMMAND_CHECK;
        } else if (bitVoicer.strData.startsWith(GENERIC_RELAY)) {
            return COMMAND_GENERIC_RELAY;
        } else if (bitVoicer.strData.startsWith(GENERIC_IR)) {
            return COMMAND_GENERIC_IR;
        } else {
            return COMMAND_EMPTY;
        }
    } else {
        return COMMAND_EMPTY;
    }
}

// Handle a generic Relay command 
void handleGenericRelay(String command) {
  String commandData = command.substring(GENERIC_RELAY.length()+1, commandData.length()-1);
  runRelayCommand(commandData);
}

// Run Relay Command
void runRelayCommand(String commandData) {
  // Relay number
  int relayNumber = commandData.substring(0, commandData.indexOf("|")).toInt();
  int relayPin = RELAY_ARRAY[relayNumber-1];
  commandData = commandData.substring(commandData.indexOf("|")+1);

  // Relay enabling duration
  int enabledForMs = commandData.substring(0, commandData.indexOf("|")).toInt();
  commandData = commandData.substring(commandData.indexOf("|")+1);

  // Delay
  int delayInMs = commandData.toInt();

  // Up to 8 relays
  if (relayNumber > 0 && relayNumber <= 7) {
    digitalWrite(relayPin, LOW);
    delay(enabledForMs);
    digitalWrite(relayPin, HIGH);
  }

  // Delay until return
  if (delayInMs > 0) {
    delay(delayInMs);
  }
}

// Handle a generic IR command 
void handleGenericIR(String command) {
  String commandData = command.substring(GENERIC_IR.length()+1, commandData.length()-1);

  // First count how many commas do we have - each comma means a IR code to be executed
  int numberOfCodes = 0;
  for (int i=0; i < commandData.length(); i++) {
    numberOfCodes += (commandData.charAt(i) == ',');  
  }
  
  // Parse each code and run it.
  for (int i=0; i < numberOfCodes +1; i++) {
    String nextCommand;
    if (commandData.indexOf(",") > 0) {
      nextCommand = commandData.substring(0, commandData.indexOf(","));
      commandData = commandData.substring(commandData.indexOf(",") + 1);
    } else {
      nextCommand = commandData;
    }
    
    runIRCommand(nextCommand);
  }
}

// Execute IR command
void runIRCommand(String commandData) {
  
  // Family type
  String irType = commandData.substring(0, commandData.indexOf("|"));
  commandData = commandData.substring(commandData.indexOf("|")+1);
  
  // Command data
  char *p;
  unsigned long irCode = strtoul(commandData.substring(0, commandData.indexOf("|")).c_str(),&p,16);
  commandData = commandData.substring(commandData.indexOf("|")+1);

  // Bits
  int bits = commandData.substring(0, commandData.indexOf("|")).toInt();
  commandData = commandData.substring(commandData.indexOf("|")+1);
  
  // Delay
  int delayInMs = commandData.toInt();

  // Send command
  if (irType == "RC5") {
    irsend.sendRC5(irCode, bits);
  } else if (irType == "RC6") {
    irsend.sendRC6(irCode, bits);
  } else if (irType == "NEC") {
    irsend.sendNEC(irCode, bits);
  } else if (irType == "SONY") {
    irsend.sendSony(irCode, bits);
  } else if (irType == "PANASONIC") {
    irsend.sendPanasonic(irCode, bits);
  } else if (irType == "JVC") {
    irsend.sendJVC(irCode, bits, false);
  } else if (irType == "SAMSUNG") {
    irsend.sendSAMSUNG(irCode, bits);
  } else if (irType == "WHYNTER") {
    irsend.sendWhynter(irCode, bits);
  } else if (irType == "AIWA_RC_T501") {
    irsend.sendAiwaRCT501(irCode);
  } else if (irType == "LG") {
    irsend.sendLG(irCode, bits);
  } else if (irType == "DENON") {
    irsend.sendDenon(irCode, bits);
  }

  // Delay until return
  if (delayInMs > 0) {
    delay(delayInMs);
  }
}
