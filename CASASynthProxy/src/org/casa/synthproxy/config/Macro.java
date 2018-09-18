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
 * This class maps a macro in CASASynthProxy configuraion.
 * A macro is a translation from a command text (received through network) and the actual command sent to CASAActuator. 
 * 
 * @author Daniel Parra Tucunduva
 */
public class Macro {
	
	/** Macro key */
	private String key;
		
	/** Data that will be sent to Arduino */
	private String data;

	/**
	 * Get the macro key - This will be matched to the command received through the network
	 * @return Macro key - This will be matched to the command received through the network
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the macro key - This will be matched to the command received through the network
	 * @param key Macro key - This will be matched to the command received through the network
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the macro data - If there is a match, this is the actual command that will be sent to CASAActuator
	 * @return Macro data - If there is a match, this is the actual command that will be sent to CASAActuator
	 */
	public String getData() {
		return data;
	}

	/**
	 * Set the macro data - If there is a match, this is the actual command that will be sent to CASAActuator
	 * @param data Macro data - If there is a match, this is the actual command that will be sent to CASAActuator
	 */
	public void setData(String data) {
		this.data = data;
	}
	
}
