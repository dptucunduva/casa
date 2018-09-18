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

import java.awt.Toolkit;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import org.casa.synthproxy.config.Config;
import org.casa.synthproxy.config.Macro;
import org.casa.synthproxy.ide.GUI;

/**
 * This class is in charge of handling commands coming from a TCP/IP connection.
 * 
 * Each connection should create a new instante of this class. Then this newly created
 * instance should be started as a separate thread.
 * 
 * BitVoicer has a "keep alive" behavior - i.e., when BitVoicer is started, a connection
 * is established and kept open until BitVoicer is running. So, this thread is capable of
 * receiving several commands in the same connection, until the connection is alive.
 *  
 * @author Daniel Parra Tucunduva
 */
public class ConnectionHandler implements Runnable {

	// CASASynthProxy configuration
	private Config config;
	// TCP/IP socket that this handler will use to communicate
	private Socket socket;
	// Activation period limit in miliseconds.
	private long activatedUntil = 0L;
	// Serial port that will be used to comunicate with CASAActuator 
	private CommPort commPort;

	/**
	 * Connection Handler constructor.
	 * 
	 * There is no default constructor as a Connection Handler requires
	 * a COM port and Configuration to be able to receve, process and send
	 * commands to CASAActuator.
	 *  
	 * @param commPort COM port for serial communication with CASAActuator.
	 * @param config CASASynthProxy configuration, loaded by the main class
	 */
	public ConnectionHandler(CommPort commPort, Config config) {
		this.commPort = commPort;
		this.config = config;
	}

	/**
	 * Get COM port that is being used to communicate with CASAActuator
	 * @return COM port that is being used to communicate with CASAActuator
	 */
	private CommPort getCommPort() {
		return commPort;
	}

	/**
	 * Get TCP/IP socket that this handler uses to receive commands
	 * @return TCP/IP socket that this handler uses to receive commands
	 */
	private Socket getSocket() {
		return socket;
	}

	/**
	 * Set TCP/IP socket that this handler uses to receive commands
	 * @param socket TCP/IP socket that this handler uses to receive commands
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Set the activation period finish. This value should be set to 
	 * System.currentTimeInMillis() + ACTIVATION_PERIOD, where ACTIVATION_PERIOD
	 * is how long the connection will be executing commands.
	 * @param activatedUntil Activation timestamp - system will be executing commands
	 * until this timestamp.
	 */
	public void setActivatedUntil(Long activatedUntil) {
		this.activatedUntil = activatedUntil;
	}
	
	/**
	 * Get the activation period finish.
	 * @return Activation period finish.
	 */
	public long getActivatedUntil() {
		return activatedUntil;
	}
	
	/**
	 * Main method execution. As a connection handling is something that
	 * should happen in a separate thread, this implementation is done
	 * through runnable interface (this method).
	 */
	@Override
	public void run() {
		Command command = null;
		try {
			do {
				command = readCommand();
				GUI.addMessage("Comando lido via rede:" + command);
				
				// Translate the command if there is macro with this command's data
				command = checkMacro(command);
				
				// If this is an activation command, set the activation period accordingly
				if (command.getCommandTp() == Command.TP_ENABLE) {
					setActivatedUntil(System.currentTimeMillis() + command.getActivatedUntil());
				}
				
				boolean globalSwitch = command.getCommandTp() != Command.TP_STATUS ? 
						(command.getCommandTp() == Command.TP_SHUTDOWN ? false : getCommPort().getGlobalSwitchStatus()): true;
				if (globalSwitch) {
					// Run it only if we are in an activation period
					if (command.getCommandTp() != Command.TP_STRING || 
							(getActivatedUntil() >= System.currentTimeMillis() && command.getCommandTp() == Command.TP_STRING)) {
						if (command.getSvoice() != null) {
							// If the text to be synthesized is "beep", trigger a default notification sound from OS. 
							if ("beep".equalsIgnoreCase(command.getSvoice())) {
								Toolkit.getDefaultToolkit().beep();
							} else {
								GUI.addMessage("Sintetizando texto: " + command.getSvoice());
								new VoiceSynth(command.getSvoice()).sayit();
							}
						}
						
						// Build command list to be sent. This will split the command in a list if it is a composite command.
						List<Command> commands = command.splitCommand();
						
						synchronized(getCommPort().getSerialPort()) {
							for (Command cmdToSend : commands) {
								// Send the command to CASAActuator
								GUI.addMessage("Enviando comando ao arduino: " + cmdToSend.toString());
								getCommPort().sendCommand(cmdToSend);
								getCommPort().getGlobalSwitchStatus();
							}
						}
					} else {
						GUI.addMessage("Comando enviado fora do período de ativação, ignorando...");
					}
				} else {
					if (command.getCommandTp() != Command.TP_SHUTDOWN) {
						GUI.addMessage("Chave geral desligada no arduino, ignorando...");
					} else {
						GUI.addMessage("Shutdown solicitado, a conexão será finalizada...");
					}
				}
			} while (getSocket().isConnected() && command != null && command.getCommandTp() != Command.TP_SHUTDOWN);
		} catch (Exception e) {
			// Error handling the connection. Nothing left to do.
			e.printStackTrace();
		}
		
		GUI.addMessage("Finalizando conexão");
	}
	
	/**
	 * Translate this command if its content maps to a macro in CASASynthProxy configuration
	 * @param command Command to be translated
	 * @return Translated command. If there is no macro, the same command is returned.
	 */
	private Command checkMacro(Command command) {
		Macro macro = config.getMacros().get(command.getData());
		if (macro != null) {
			command.setData(macro.getData());
			GUI.addMessage("Comando traduzido:" + command);
		}
		return command;
	}
	
	/**
	 * Read a command from TCP/IP connection. This method blocks until a command is received.
	 * 
	 * Commands should follow the same envelope as BitVoicer commands. Basically, the envelope is:
	 * 
	 * <ul>
	 * <li>byte 1: value 0x01 - envelope start</li>
	 * <li>byte 2: Command type as defined in {@link org.casa.synthproxy.Command} class constants (Ex.: Command.TP_STRING)</li>
	 * <li>byte 3: Data length - up to 256 bytes when CASASynthProxy is receiving, but only 59 bytes to be sent to CASAActuator</li>
	 * <li>byte 4 to n: Command data, n is the data length read in byte 3.</li>
	 * <li>byte n+1:  value 0x04 - envelope end</li>
	 * </ul>
	 * 
	 * @return Command that was read.
	 */
	private Command readCommand() {
		Command command = null;
		byte[] bAux = new byte[1];
		byte[] bData;
		
		try {
			command = new Command();
			InputStream is = getSocket().getInputStream();

			// First byte - envelope start.
			int status = is.read(bAux);
			if (status == -1) {
				// If status is -1, that means that the socket is closed.
				// Generate a SHUTDOWN command
				command = new Command();
				command.setCommandTp(Command.TP_SHUTDOWN);
				for (int i = 0; i < is.available(); i++) {
					is.read(bAux);
				}
				return command;
			}
			if (bAux[0] != 0x01) {
				throw new Exception ("Primeiro byte do envelope inválido");
			}
			
			// Second byte - command type
			is.read(bAux);
			if (bAux[0] == Command.TP_STATUS) {
				command.setCommandTp(Command.TP_STATUS);
			} else if (bAux[0] == Command.TP_STRING) {
				command.setCommandTp(Command.TP_STRING);
			} else {
				// Command type not supported or not recognized. 
				// Assume a status command, that is a "dummy" one.
				command.setCommandTp(Command.TP_STATUS);
				return command;
			}
			
			// Third byte - data package length.
			is.read(bAux);
			int envelopeSize = bAux[0] & 0xFF;
			
			// Forth + n bytes - Data package
			if (envelopeSize > 0) {
				bData = new byte[envelopeSize];
				is.read(bData, 0, envelopeSize);
				command.setData(new String(bData));
				
				// If this is a ACTIVATION command, build a activation command
				// setting the activation period in the command.
				if (command.getData() != null && command.getData().startsWith("E")) {
					command.setCommandTp(Command.TP_ENABLE);
					command.setActivatedUntil(Long.parseLong(command.getData().substring(1)));
					GUI.addMessage("Habilitando comandos por " + command.getActivatedUntil() + " milissegundos");
				}
			}
			
			// Last byte - envelope end.
			is.read(bAux);
			if (bAux[0] != 0x04) {
				throw new Exception ("Fim de envelope inválido");
			}
		} catch (Exception e) {
			// There was an error reading the command. Generate e STATUS command
			e.printStackTrace();
			command = new Command();
			command.setCommandTp(Command.TP_STATUS);
		}
		return command;
	}
}
