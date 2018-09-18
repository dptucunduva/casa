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

import javax.swing.JButton;

import org.casa.synthproxy.Command;

/**
 * This is a wrapper class that maps a button in CASASynthProxy GUI. It inherits from a basic AWT JButton
 * and adds a few attributes to store CASASynthProxy attributes, like command data e text to be synthesized.
 * 
 * @author Daniel Parra Tucunduva
 */
public class MyGUIButton extends JButton {

	/**
	 * Default generated serial ID 
	 */
	private static final long serialVersionUID = 3253435648946416440L;
	
	/** Command data - this is the data that will be sent to CASAActuator **/
	private String data;
	/** Text to be synthesized when this button is pressed **/
	private String tts;
	
	/**
	 * Constructor - Create a MyGUIButton with the label, data and text to be synthesized 
	 * @param label Button Label. This is the text that will be inside the button in the GUI
	 * @param data Data that will be sent as a command to CASAActuator when this button is pressed or this command is selected during a cycling process 
	 * @param tts Text that will be synthesized when this button is pressed or this command is selected during a cycling process
	 */
	public MyGUIButton(String label, String data, String tts) {
		super(label);
		setData(data);
		setTTS(tts);
	}
	
	/**
	 * Get the data that will be sent to CASAActuator when this command is issued.
	 * @return Data that will be sent to CASAActuator when this command is issued.
	 */
	public String getData() {
		return data;
	}

	/**
	 * Set the data that will be sent to CASAActuator when this command is issued.
	 * @param command Data that will be sent to CASAActuator when this command is issued.
	 */
	public void setData(String command) {
		this.data = command;
	}

	/**
	 * Get the text that will be synthesized when this command is issued 
	 * @return Text that will be synthesized when this command is issued
	 */
	public String getTTS() {
		return tts;
	}

	/**
	 * Set the text that will be synthesized when this command is issued
	 * @param tts Text that will be synthesized when this command is issued
	 */
	public void setTTS(String tts) {
		this.tts = tts;
	}
	
	/**
	 * Get a corresponding Command object. This method will return a command that
	 * can be sent to CASAActuator. 
	 * @return Command to be sent to CASAActuator
	 */
	public Command toCommand() {
		Command command = new Command();
		command.setCommandTp(Command.TP_STRING);
		command.setData(getData());
		command.setSvoice(getTTS());
		return command;
	}
	
	@Override
	public String toString() {
		return "[label : " + getText() + "],[command : " + getData() + "],[tts : " + getTTS() + "]";
	}
}
