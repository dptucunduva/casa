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
package org.casa.synthproxy.config;

/**
 * CASASynthProxy configuration
 * 
 * Command definition 
 * 
 * @author Daniel Parra Tucunduva
 *
 */
public class Command {
	
	/** Command label **/
	private String label;
	
	/** Command macro **/
	private String macro;
	
	/** Command data **/
	private String data;
	
	/** Text to be synthesized when this command is issued **/
	private String tts;

	/**
	 * Get the command Label - This is the text that will be shown inside the button
	 * @return Command Label - This is the text that will be shown inside the button
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the command Label - This is the text that will be shown inside the button
	 * @param label command Label - This is the text that will be shown inside the button
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the macro associated with this command. This is the macro name and will be translated to an actual command.
	 * @return Macro associated with this command. This is the macro name and will be translated to an actual command.
	 */
	public String getMacro() {
		return macro;
	}

	/**
	 * Set the macro associated with this command. This is the macro name and will be translated to an actual command.
	 * @param macro Macro associated with this command. This is the macro name and will be translated to an actual command.
	 */
	public void setMacro(String macro) {
		this.macro = macro;
	}

	/**
	 * Get the command Data. This is the command data that will be sent to CASAActuator. 
	 * @return Command Data. This is the command data that will be sent to CASAActuator.
	 */
	public String getData() {
		return data;
	}

	/**
	 * Set the command Data. This is the command data that will be sent to CASAActuator.
	 * @param data Command Data. This is the command data that will be sent to CASAActuator.
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * Get the text that will be synthesized when this command is issued.
	 * @return Text that will be synthesized when this command is issued.
	 */
	public String getTts() {
		return tts;
	}

	/**
	 * Set the text that will be synthesized when this command is issued.
	 * @param tts Text that will be synthesized when this command is issued.
	 */
	public void setTts(String tts) {
		this.tts = tts;
	}
}
