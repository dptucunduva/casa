/**
 * CASASynthProxy - Proxy application that receives commands from 
 * the network and send them to CASAActuator in Arduino. 
 * 
 * Copyright (C) 2015  Daniel Parra Tucunduva
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.casa.synthproxy;

import org.casa.synthproxy.config.Config;
import org.casa.synthproxy.ide.GUI;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * This class maps a serial communication port that is used to communicate with CASAActuator.
 * 
 * It uses JSSC as a library to achieve serial communication.
 * 
 * @author Daniel Parra Tucunduva
 */
public class CommPort implements SerialPortEventListener {

	/** Serial Port that will be used in communication **/
	private SerialPort serialPort;
	/** Data buffer that will be used to store received data **/ 
	private String dataBuffer;
	/** Local communication flag - if it is true, it is a communication issued by this class. If set
	to false, it is a communication that came from CASAActuator **/
	private boolean local = true;

	/** 
	 * This method will get all COM ports available in the computer and issue a command
	 * to try to find a CASAActuator enabled Arduino system.
	 * If no CASAACtuator system is found, a RuntimeException is throw
	 * @return CASAActuator COM port.
	 */
	public static CommPort getArduinoCommPort() {
		// First check if there ia pre-set port.
		CommPort commPort = null;
		String configPort = Config.getGeneralConfigEntry("COMPort", null);
		if (configPort != null) {
			try {
				commPort = new CommPort(configPort);
			} catch (Exception e) {
				// Preconfigured port is not valid. Move on and keep scanning.
			}
		}
		
		if (commPort == null || commPort.getSerialPort() == null) {
			String ports[] = SerialPortList.getPortNames();
			for (String port : ports) {
				try {
					CommPort candidate = new CommPort(port);
					commPort = candidate;
					break;
				} catch (Exception e) {
					// Probably no CASAActuator system is connected to this port, ignore.
				}
			}
		}
		
		if (commPort == null || commPort.getSerialPort() == null) {
			throw new RuntimeException("Arduino não encontrado!");
		} 
		
		return commPort;
	}
	
	/**
	 * Constructor that receives a COM port, opens it and check for a 
	 * CASAActuator enabled Arduino system
	 * @param serial Serial por identification (Ex.: COM3)
	 * @throws Exception Error opening the port and checking for a CASAActuator 
	 * enabled Arduino system. If this happens, you can assume that there is no system connected to this port. 
	 */
	public CommPort(String serial) throws Exception {
		serialPort = new SerialPort(serial);
		serialPort.openPort();
		serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
		serialPort.addEventListener(this, SerialPort.MASK_RXCHAR);
		Thread.sleep(2000);
		try {
			getGlobalSwitchStatus();
		} catch (Exception e) {
			serialPort.removeEventListener();
			serialPort.closePort();
			throw new RuntimeException("Arduino não encontrado!");
		}
	}

	/**
	 * Get the JSSC serial port object 
	 * @return JSSC serial port object
	 */
	public SerialPort getSerialPort() {
		return serialPort;
	}
	
	/**
	 * Send a command to this Serial port.
	 * @param command Command to be sent
	 * @throws Exception Error writing bytes to the port
	 */
	public void sendCommand(Command command) throws Exception {
		serialPort.writeBytes(command.toBytes(false));
	}
	
	/**
	 * Close this serial connection
	 * @throws Exception Error closing this serial connection
	 */
	public void close() throws Exception {
		synchronized(serialPort) {
			serialPort.closePort();
		}
	}
	
	/**
	 * Set serial port connection data buffer
	 * @param data serial port connection data buffer
	 */
	public void setDataBuffer(String data) {
		this.dataBuffer = data;
	}

	/**
	 * Get serial port connection data buffer
	 * @return serial port connection data buffer
	 */
	public String getDataBuffer() {
		return this.dataBuffer;
	}
	/**
	 * Set a LOCAL communication type. This flag will tell the system to stop looking for 
	 * command issued from CASAActuator and handle incoming data as a response. 
	 */
	private void setLocalCommunication() {
		this.local = true;
	}

	/**
	 * Set a GLOBAL communication type. This flag will tell the system to handle incoming data
	 * as a new command issued from CASAActuator 
	 */
	private void setGlobalCommunication() {
		this.local = false;
	}

	/**
	 * Check if the global switch in CASAActuator is on. This method can be used to 
	 * check if commands can be sent or just to check if the connection with CASAActuator if fine.
	 * @return <b>true</b> if connection is OK and CASAActuator is enabled and ready to receive commands, 
	 * <b>false</b> if connection is OK but the switch is OFF
	 * @throws Exception There is no valid connection with CASAActuator
	 */
	public boolean getGlobalSwitchStatus() throws Exception {
		synchronized(serialPort) {
			try {
				// Set LOCAL communication sets a data buffer to store the response
				setDataBuffer(new String());
				setLocalCommunication();
				
				// Send command
				Command command = new Command();
				command.setCommandTp(Command.TP_CHECK);
				command.setData("C");
				sendCommand(command);
				
				// Read return byte
				int i=0;
				while (!getDataBuffer().endsWith(";") && i < 200) {
					i++;
					Thread.sleep(25);
				}
		
				if (!getDataBuffer().endsWith(";")) {
					throw new RuntimeException("Resposta não recebida ou envelope incompleto!");
				}
				
				if (!"E;".equals(getDataBuffer()) && !"D;".equals(getDataBuffer())) {
					throw new RuntimeException("Resposta inválida!");
				}
				
				return "E;".equals(getDataBuffer());
			} finally {
				// Set global communication behavior back
				setDataBuffer(new String(""));
				setGlobalCommunication();
			}
		}
	}

	/**
	 * This method handles serial communication events, including data receiving.
	 * @param event Event information. This should contain everything (including data) related to the event
	 * issued by communication library (JSSC).
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		if(event.isRXCHAR() && event.getEventValue() > 0) {
			try {
                String receivedData = serialPort.readString(event.getEventValue());
                setDataBuffer(getDataBuffer() + receivedData);
                if (receivedData.endsWith(";")) {

                    if (!this.local) {
                    	// This is an external triggered command - hand it to a Command Handler
                    	String data = getDataBuffer();
                    	setDataBuffer(new String(""));
                    	new ArduinoCommandHandler().handleCommand(data);
                    }
                }
            }
            catch (SerialPortException ex) {
            	GUI.addMessage("Erro recebendo dados da porta serial! " + ex);
            }
		}
	}
}
