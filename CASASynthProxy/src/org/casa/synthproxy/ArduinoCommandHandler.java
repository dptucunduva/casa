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

import org.casa.synthproxy.ide.CycleThread;
import org.casa.synthproxy.ide.GUI;

/**
 * Arduino Command Handler is the class that handles all commands received from 
 * CASAActuator.
 * 
 * @author Daniel Parra Tucunduva
 */
public class ArduinoCommandHandler extends CommandSender {

	/**
	 * This is the main command handling method. Commands received
	 * from CASAActuator will be sorted and delegated from within
	 * this method
	 * @param commandData Command information received from CASAActuator.
	 */
	public void handleCommand(String commandData) {
		if ("B;".equals(commandData)) {
			buttonPressed();
		} else if ("R;".equals(commandData)) {
			ring();
		} else if (commandData.startsWith("I")) {
			irCode(commandData);
		}
	}
	
	/**
	 * This method will handle a simple "button pressed" event.
	 * It interacts with the cycling thread that interacts with the
	 * user. 			
	 */
	public void buttonPressed() {
		try {
			// First we need to know if we are in sleep mode or in Cycle mode.
			if (CycleThread.getInstance().isAlive()) {
				// There is a cycling thread, request selecion
				CycleThread.getInstance().selectionRequested();
			} else {
				// Sleep mode, as there is no active thread. Start it
				CycleThread.getInstance().start();
			}
		} catch (Exception e) {
			// Nothing to do here, just print the error
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the method that will do something that should call the attention
	 * of someone else. It is mapped in CASAActuator to a continuous pressing of the 
	 * command button 
	 */
	public void ring() {
		// TODO: This message should not be hard coded here.
		new VoiceSynth("Por favor").sayit();
	}
	
	/**
	 * This method will handle a CASAActuator IR receiving event.
	 * The goal is to print the command information in the console
	 * so that the CASA system could be configured. 
	 * @param commandData IRCode information received from CASAActuator
	 */
	public void irCode(String commandData) {
		String irData = commandData.substring(1);
		
		GUI.addMessage("Comando IR recebido: " + irData);
	}
}
