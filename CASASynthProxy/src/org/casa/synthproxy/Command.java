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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class maps a command in CASA system.
 * It uses the same envelope as BitVoicer commands.
 * 
 * @author Daniel Parra Tucunduva
 */
public class Command {

	// Command type definition
	/** STATUS Command - sent to tell that a connection is starting or being closed **/
	public static final byte TP_STATUS = (byte)0xFF;
	/** SHUTDOWN Command - sent to tell that the connection will be shut down  **/
	public static final byte TP_SHUTDOWN = (byte)0xAA;
	/** ENABLE Command - This is a special command that will tell the system that an actual command
	 *  will be triggered soon. STRING commands are executed only during an activation period **/
	public static final byte TP_ENABLE = (byte)0xBB;
	/** CHECK Command - This command will query CASAActuator for its status - if command issuing is enabled or not **/
	public static final byte TP_CHECK = (byte)0xCC;
	/** STRING Command - This command type is the one that is used to send data to CASAActuator. This
	 * is the type that contain the real commands, like changing the channel or turning the TV set on **/ 
	public static final byte TP_STRING = (byte)0x04;
	
	// Command type
	private byte commandTp;
	// Command data 
	private String data;
	// Text that will be synthetized when the command is issued
	private String svoice;
	// If this is a ENABLE command, this is the activation period in miliseconds.
	private long activatedUntil;

	/**
	 * Get command data
	 * @return command data
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * Set command data. IF the content contains a semi-coma, it is split in two.
	 * The first part is the actual data, that will be sent to CASAActuator.
	 * The second part is the text that will be synthetized.
	 * This allows you to configure external agents like bitvoicer to send
	 * both command data and voice feedback.
	 * @param data command data
	 * @return this very same object
	 */
	public Command setData(String data) {
		if (data != null) {
			String tokens[] = data.split(";");
			this.data = tokens[0];
			if (tokens.length > 1) {
				this.setSvoice(tokens[1]);
			}
		}
		return this;
	}

	/**
	 * Get activation period in miliseconds
	 * @return activation period in miliseconds
	 */
	public long getActivatedUntil() {
		return activatedUntil;
	}

	/**
	 * Set activation period in miliseconds
	 * @param activatedUntil activation period in miliseconds
	 * @return this very same object
	 */
	public Command setActivatedUntil(long activatedUntil) {
		this.activatedUntil = activatedUntil;
		return this;
	}

	/**
	 * Get command type
	 * @return command type
	 */
	public byte getCommandTp() {
		return commandTp;
	}

	/**
	 * Set command type
	 * @param commandTp command type
	 * @return this very same object
	 */
	public Command setCommandTp(byte commandTp) {
		this.commandTp = commandTp;
		return this;
	}
	
	/**
	 * Get text that will synthetized
	 * @return text that will synthetized
	 */
	public String getSvoice() {
		return svoice;
	}

	/**
	 * Set text that will synthetized
	 * @param svoice text that will synthetized
	 * @return this very same object
	 */
	public Command setSvoice(String svoice) {
		// Se existe um | (pipe) no texto, randomiza qual texto usar
		if (svoice != null) {
			String options[] = svoice.split("\\|");
			int rnd = new Random().nextInt(options.length);
			this.svoice = options[rnd];
		}
		return this;
	}

	/**
	 * Retrieve a byte array representing this command. This byte array is the one 
	 * to be sent to CASAActuator though COM port.
	 * @param includeTTS Boolean flag to tell this method if the voice part should be included. 
	 * When sending command to CASAActuator, because of package size limitations, it is a good
	 * idea to exclude voice information as it is not relevant to CASAActuator.
	 * @return Byte array representation of this command
	 */
	public byte[] toBytes(boolean includeTTS) {
		int dataLength = getData() == null ? 0 : getData().length();
		int ttsLength = 0;
		if (includeTTS) {
			ttsLength = getSvoice() == null ? 0 : getSvoice().length() + 1;
		}
		
		byte b[] = new byte[dataLength+ttsLength+4];
		b[0] = 1;
		b[1] = 4;
		b[2] = (byte)(dataLength+ttsLength);
		int i=0;
		for (; i < dataLength; i++) {
			b[3+i] = (byte) getData().charAt(i);
		}
		if (includeTTS) {
			b[3+i] = ';';
			for (int j=0; j < ttsLength-1; j++) {
				b[4+i+j] = (byte) getSvoice().charAt(j);
			}
		}
		
		b[dataLength+ttsLength+3] = 4;
		return b;
	}
	
	/**
	 * Split this command in several other commands, if relevant.
	 * A command can represent several other commands if the DATA part is 
	 * composed of several command put together with a "#" as separator.
	 * If this is the case, this method will split this command into several other commands
	 * and return this list.
	 * If the command is actually a single one, it will be the only element in the return list.
	 * @return Command list
	 */
	public List<Command> splitCommand() {
		String commandDataArray[] = getData().split("#");
		List<Command> commandList = new ArrayList<Command>();
		for (String commandData : commandDataArray) {
			Command command = new Command();
			command.setActivatedUntil(getActivatedUntil());
			command.setCommandTp(getCommandTp());
			command.setSvoice(getSvoice());
			command.setData(commandData);
			
			commandList.add(command);
		}
		return commandList;
	}
	
	@Override
	public String toString() {
		return new StringBuffer("Command Type=[").append(String.format("%02X", getCommandTp()))
				.append("], Command data=[").append(getCommandTp() == TP_STRING || getCommandTp() == TP_ENABLE ? getData() : "")
				.append("], Command speech=[").append(getCommandTp() == TP_STRING || getCommandTp() == TP_ENABLE ? getSvoice() : "")
				.append("]").toString();
	}
}
