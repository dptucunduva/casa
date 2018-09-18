/**
 * CASASynthProxy - Proxy application that receives commands from 
 * the network and send then to CASAActuator in Arduino. 
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

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.casa.synthproxy.config.Config;
import org.casa.synthproxy.ide.GUI;

/**
 * This is the MAIN class of the CASASynthProxy.
 * 
 * It reads the configuration and starts the IDE and network daemon.
 * 
 * @author Daniel Parra Tucunduva
 */
public class CASASynthProxy {

	/**
	 * Main method - it starts everything
	 * @param args command line arguments.
	 * @throws Exception Standard exception throwing
	 */
	public static void main(String[] args) throws Exception {
		
		int port = 0;
		String host = null;

		// Read config file
		Config config = new Config(new File("config.xml"));
		
		GUI.initGUI(config);

		GUI.addMessage("Iniciando CASASynthProxy...");

		// Check IP and port.
		host = Config.getGeneralConfigEntry("bindHost", "localhost");
		port = Integer.parseInt(Config.getGeneralConfigEntry("bindPort", "11000"));
		GUI.addMessage("HOSTNAME/PORTA: " + host + ":" + port);
		
		// Search for arduino in all COM ports
		GUI.addMessage("Procurando Arduino...");
		CommPort commPort = null;
		try {
			commPort = CommPort.getArduinoCommPort();
			GUI.addMessage("Conexão serial com arduino iniciada com sucesso na porta " + commPort.getSerialPort().getPortName());
		} catch (Exception e) {
			GUI.addMessage("Erro abrindo porta serial - " + e.getMessage());
		}

		// Bind port to wait for connections
		ServerSocket s = new ServerSocket();
		s.bind(new InetSocketAddress(host, port));
		GUI.addMessage("Aguardando comando...");
		
		// Start BitVoicer if configuration is set
		try {
			String bitVoicer = Config.getGeneralConfigEntry("BitVoicerEXE",null);
			if (bitVoicer != null) {
				Runtime.getRuntime().exec(bitVoicer);
			}
		} catch (Exception e) {
			GUI.addMessage("Erro iniciando BitVoicer");
		}

		// For each connection, starts a thread that will handle commands 
		while (s.isBound()) {
			ConnectionHandler n = new ConnectionHandler(commPort, config);
			Thread thread = new Thread(n);
			n.setSocket(s.accept());
			GUI.addMessage("Conexão de rede recebida!");
			thread.start();
		}
		
		// Bind has ended, shut down.
		try {
			GUI.addMessage("Fechando porta serial");
			commPort.close();
		} catch (Exception e) {
			// Nothing left to do other than closing the port
			e.printStackTrace();
		}

		GUI.addMessage("Finalizando CASASynthProxy.");
		s.close();
	}
}
