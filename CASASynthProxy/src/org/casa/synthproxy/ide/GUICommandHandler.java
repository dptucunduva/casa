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
package org.casa.synthproxy.ide;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.casa.synthproxy.CommandSender;

/**
 * Handles commands issued by the GUI. Basically, when a user clicks on buttons.
 * @author Daniel Parra Tucunduva
 */
public class GUICommandHandler extends CommandSender implements ActionListener {

	/** Button connected to this command handler **/
	private final MyGUIButton button;
	
	/**
	 * Creates a new GUICommandHandler connected to a button
	 * @param button Button that will be monitored for events
	 */
	public GUICommandHandler(MyGUIButton button) {
		this.button = button;
	}
	
	/**
	 * A button was pressed. If there is no cycling going on, send the command to CASAActuator.
	 * @param e Event containing informations. Not used in this scope  
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (CycleThread.getInstance().isAlive()) {
			GUI.addMessage("Comando \"" + button.getText() + "\" está sendo ignorado pois um comando enviado pelo Arduino está em execução");
		} else {
			sendCommand(button.toCommand());
		}
	}
}
