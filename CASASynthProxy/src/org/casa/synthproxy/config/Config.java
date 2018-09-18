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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Main configuration class. This class holds all CASASynthProxy configuration information.
 * 
 * Configuration comes from a XML file named config.xml. The file structure is explained in the sample below:<br><br>
 *
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <config>
 * 	<!-- Macros
 * 	Each macro must have:
 * 	- key: This is the name of the macro that should be used in "macro" attribute in commands.
 * 	- data: This is the data that will be set in the command that uses this macro.
 * 	-->
 * 	<macros>
 * 		<macro key="TVIOD" data="IR[RC6|0x38|20|50,RC6|0x38|20|50,RC6|0x38|20|250]#IR[RC6|0x59|20|50,RC6|0x59|20|50,RC6|0x59|20|250]#IR[RC6|0x5C|20|50,RC6|0x5C|20|50,RC6|0x5C|20|0]" />
 * 	</macros>
 * 	
 * 	<!-- Command group
 * 	Each command must have:
 * 	- label: This is the text that will be shown inside the button.
 * 	- macro: This is the macro name and will be translated to an actual command.
 * 	- data: This is the command data that will be sent to CASAActuator. 
 * 	- tts: Text that will be synthesized when this command is issued.
 * 	"macro" and "data" cannot be used together. If macro is set, data is overriden.
 * 	-->
 * 	<groups>
 * 		<group name="Canais">
 * 			<command label="Canal Globo" macro="TDC517" tts="Globo" />
 * 		</group>
 * 		<group name="Televisão e NET">
 * 			<command label="Aumentar Volume" macro="TDVU" tts="Aumentando" />
 * 		</group>
 * 	</groups>
 * 
 *  <!-- General configuration section. Any entry here will be set as a JVM system property, and
 *   can be retrieved anywhere in the code using System.getProperty("NAME"), where name is the property name.
 *   It is also possible to use the static method from this class "getGeneralConfigEntry(String, String)". This method
 *   will return the default value (second argument) if the configuration is not set for the provided key (first argument). 
 *   Known configurable values:
 *  - sourceTVDelay: Waiting time, in miliseconds, between an Arduino command is received and command cycling starts. Default value is "8000".
 *  - cyclingInterval: Waiting time, in miliseconds, for each command during cycling in command selection screen. Default value is "2500".
 *  - bindHost: IP address or Host that will be used to bind and wait for command coming from the network. Default value is "localhost".
 *  - bindPort: Port that will be used to bind and wait for command coming from the network. Default value is "11000".
 *  - COMPort: CASASynthProxy enabled Arduino COM port. There is no default value, if this is not set, the application will scan all COM ports.
 * 	<generalConfig>
 *		<entry name="sourceTVDelay" value="8000" />
 *		<entry name="cyclingInterval" value="2500" />
 *		<entry name="bindHost" value="localhost" />
 *		<entry name="bindPort" value="11000" />
 *		<entry name="COMPort" value="COM3" />
 *		<entry name="BitVoicerEXE" value="C:\Program Files\BitSophia\BitVoicer\BitVoicer.exe" />
 *	</generalConfig>
 * </config>}
 * </pre>
 * 
 * @author Daniel Parra Tucunduva
 *
 */
public class Config {

	/** Macro list **/
	private Map<String, Macro> macros;
	
	/** Command Group list **/
	private List<CommandGroup> groups;
	
	/**
	 * Constructor that builds configuration objects from config file
	 * @param configFile File object pointing to XML config file.
	 */
	public Config(File configFile) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document doc = db.parse(configFile);

			// First step is to build the macro list - this list will be used in possible substitutions below.
			buildMacroList(doc);
			
			// For each group (up to 4)
			NodeList groups = doc.getElementsByTagName("group");
			setGroups(new ArrayList<CommandGroup>());
			for (int i = 0; i < groups.getLength(); i++) {
				CommandGroup commandGroup = new CommandGroup();
				
				NodeList commands = groups.item(i).getChildNodes();
				for (int j = 0; j < commands.getLength(); j++) {
					Node commandNode = commands.item(j);
					if (commandNode.getNodeType() == Node.ELEMENT_NODE) {
						Command command = new Command();
						command.setLabel(commandNode.getAttributes().getNamedItem("label").getTextContent());
						command.setTts(commandNode.getAttributes().getNamedItem("tts").getTextContent());
						if (commandNode.getAttributes().getNamedItem("data") != null) {
							command.setData(commandNode.getAttributes().getNamedItem("data").getTextContent());
						}
						
						// Translate data if we have a macro
						String macroKey = null;
						if (commandNode.getAttributes().getNamedItem("macro") != null) {
							macroKey = commandNode.getAttributes().getNamedItem("macro").getTextContent();
						}
						if (macroKey != null) {
							Macro macro = getMacros().get(macroKey);
							if (macro != null) {
								command.setData(macro.getData());
							}
						}
						
						// Add this command to this command group
						commandGroup.addCommand(command);
					}
				}
				
				// Add this command group to configuration.
				getGroups().add(commandGroup);
			}
			
			// Now general configuration
			NodeList entries = doc.getElementsByTagName("entry");
			for (int i = 0; i < entries.getLength(); i++) {
				Node generalConfigNode = entries.item(i);
				if (generalConfigNode.getNodeType() == Node.ELEMENT_NODE &&
						generalConfigNode.getAttributes().getNamedItem("name") != null &&
						generalConfigNode.getAttributes().getNamedItem("value") != null) {
					System.setProperty(generalConfigNode.getAttributes().getNamedItem("name").getTextContent(), 
							generalConfigNode.getAttributes().getNamedItem("value").getTextContent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the macro list defined in configuration file
	 * @return Macro list defined in configuration file
	 */
	public Map<String, Macro> getMacros() {
		return macros;
	}

	/**
	 * Set the macro list defined in configuration file
	 * @param macros list defined in configuration file
	 */
	public void setMacros(Map<String, Macro> macros) {
		this.macros = macros;
	}

	/**
	 * Get the command group list defined in configuration file
	 * @return Command group list defined in configuration file
	 */
	public List<CommandGroup> getGroups() {
		return groups;
	}

	/**
	 * Set the command group list defined in configuration file
	 * @param groups Command group list defined in configuration file
	 */
	public void setGroups(List<CommandGroup> groups) {
		this.groups = groups;
	}
	
	/**
	 * Retrieve configuration value if set. If it is not set, the default value is returned.
	 * @param key Configuration key. It is the "key" attribute in configuration file entry node
	 * @param defaultValue If this configuration entry is not set, the default value is returned
	 * @return Configuration value - if not found, the default value is returned.
	 */
	public static String getGeneralConfigEntry(String key, String defaultValue) {
		String configValue = System.getProperty(key);
		return configValue == null ? defaultValue : configValue;
	}

	/**
	 * Build macro list. This method takes all "macro" nodes from config file and stores
	 * them for further translation of commands
	 * @param doc Config file DOM Document main object
	 */
	private void buildMacroList(Document doc) {
		
		// Init macro list
		setMacros(new HashMap<String, Macro>());

		// Get all macros from XML config file
		NodeList macros = doc.getElementsByTagName("macro");
		for (int j = 0; j < macros.getLength(); j++) {
			Node command = macros.item(j);
			Macro macro = new Macro();
			macro.setKey(command.getAttributes().getNamedItem("key").getTextContent());
			macro.setData(command.getAttributes().getNamedItem("data").getTextContent());
			
			// Add this macro to macro list
			getMacros().put(macro.getKey(), macro);
		}
	}
}
