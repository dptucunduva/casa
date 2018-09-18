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

import java.util.ArrayList;
import java.util.List;

/**
 * This class maps a command group in configuration. CASASynthProxy IDE provides
 * up to 4 groups of commands.
 *  
 * @author Daniel Parra Tucunduva
 *
 */
public class CommandGroup {

	/** Command group name **/
	private String name;
	
	/** Command List in this group. **/
	private List<Command> commands;

	/**
	 * Get the command group name. This is the label of the group, shown in CASASynthProxy IDE 
	 * @return Command group name. This is the label of the group, shown in CASASynthProxy IDE
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the command group name. This is the label of the group, shown in CASASynthProxy IDE
	 * @param name Command group name. This is the label of the group, shown in CASASynthProxy IDE
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the commands that are in this command group.
	 * @return Commands that are in this command group.
	 */
	public List<Command> getCommands() {
		return commands;
	}

	/**
	 * Set the commands that are in this command group.
	 * @param commands Commands that are in this command group.
	 */
	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}
	
	/**
	 * Add a command to this command group
	 * @param command Command to be added
	 */
	public void addCommand(Command command) {
		if (getCommands() == null) {
			setCommands(new ArrayList<Command>());
		}
		getCommands().add(command);
	}
}
