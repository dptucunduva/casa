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

import java.net.Socket;

import org.casa.synthproxy.config.Config;

/**
 * This is an abstract class that maps all basic operations in order to
 * send commands to CASAActuator.
 * 
 * All classes that want to send commands to CASAActuator should extend from
 * this one.
 * 
 * This class also extends thread as the communication might require asynchronous behavior.
 * 
 * @author Daniel Parra Tucunduva
 *
 */
public abstract class CommandSender extends Thread {
	
	/**
	 * Get a activation command, with a fixed activation period of 10 seconds.
	 * @return 10 second activation command
	 */
	protected static Command getActivationCommand() {
		Command c = new Command();
		c.setCommandTp(Command.TP_ENABLE);
		// TODO: Move this timing info (10000) to a configurable part
		c.setData("E10000");
		return c;
	}

	/**
	 * Send the command to CASAActuator.
	 * This method actually connects through the network to CASASynthProxy as any
	 * other application would do and requests the commend to be sent.
	 * @param c Command to be sent
	 */
	protected static void sendCommand(Command c) {
		Socket socket = null;
		try {
			// Retrieve config for host and Port
			String host = Config.getGeneralConfigEntry("bindHost", "localhost");
			Integer port = Integer.parseInt(Config.getGeneralConfigEntry("bindPort", "11000"));
			socket = new Socket(host, port);
			socket.getOutputStream().write(getActivationCommand().toBytes(false));
			socket.getOutputStream().write(c.toBytes(true));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
