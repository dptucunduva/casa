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

import java.awt.Color;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.casa.synthproxy.Command;
import org.casa.synthproxy.CommandSender;
import org.casa.synthproxy.config.Config;

/**
 * This class handles the action cycling mechanism of CASASynthProxy IDE.
 * @author Daniel Parra Tucunduva
 */
public class CycleThread extends CommandSender {

	/** CycleThread instance */
	private static CycleThread cycleThread;

	/** Level 1 component list - This is the equivalent to commandGroups that we have in the configuration **/  
	private static List<Component> level1ComponentList = new ArrayList<Component>();
	/** Level 2 component map - This is the equivalent to commands that we have in the configuration. Each
	 * command is stored in a command group **/  
	private static HashMap<Component, List<Component>> level2ComponentMap = new HashMap<Component, List<Component>>();

	/** This stores the currently active component in the screen (painted yellow) **/
	private Component activeComponent;
	/** This stores the currently selected component in the screen (i.e., the component that the user selected by a input button) **/
	private Component selectedComponent;
	/** Flag to signal that we need to stop sycling through components **/
	private boolean stopCycling = false;
	
	private static final Color YELLOW = new Color(0xFF, 0xFF, 0x00);

	/**
	 * Get the active component - That is the component that is painted yellow in the screen
	 * @return Active component - That is the component that is painted yellow in the screen
	 */
	public Component getActiveComponent() {
		return activeComponent;
	}

	/**
	 * Set the active component - That is the component that is painted yellow in the screen
	 * @param activeComponent Active component - That is the component that is painted yellow in the screen
	 */
	public void setActiveComponent(Component activeComponent) {
		this.activeComponent = activeComponent;
	}

	/**
	 * Get the currently selected component in the screen (i.e., the component that the user selected by a input button)
	 * @return Currently selected component in the screen (i.e., the component that the user selected by a input button)
	 */
	public Component getSelectedComponent() {
		return selectedComponent;
	}

	/**
	 * Set the Currently selected component in the screen (i.e., the component that the user selected by a input button)
	 * @param selectedComponent Currently selected component in the screen (i.e., the component that the user selected by a input button)
	 */
	public void setSelectedComponent(Component selectedComponent) {
		this.selectedComponent = selectedComponent;
	}

	/**
	 * This will give you a cycle thread instance. Each time a cycle thread start cyclin (and for that a thread is created),
	 * it cannot stat again, as JAVA only allows an object to turn into a thread once. So, if
	 * it is not alive or null, create a new one and return it. 
	 * @return Instance of the Cycling thread
	 */
	public static CycleThread getInstance() {
		if (cycleThread == null || !cycleThread.isAlive()) {
			cycleThread = new CycleThread();
		}
		return cycleThread;
	}

	/**
	 * Adds a component to the first level (command groups)
	 * @param component Component mapping the command group
	 */
	public void addRootComponent(Component component) {
		level1ComponentList.add(component);
	}

	/**
	 * Adds a second level component - That would be a command inside a command group
	 * @param rComponent Component mapped to the command group
	 * @param component Component mapped to a command
	 */
	public void addSecondLevelComponent(Component rComponent, Component component) {
		List<Component> cList = level2ComponentMap.get(rComponent);
		if (cList == null) {
			level2ComponentMap.put(rComponent, new ArrayList<Component>());
			cList = level2ComponentMap.get(rComponent);
		}
		cList.add(component);
	}

	/**
	 * Start cycling process. This will start the thread in this instance, which will
	 * issue a command to switch the TV set to the selection screen and then start highlighting
	 * command groups and command so the user can select those.
	 */
	public void startCycling() {
		this.start();
	}

	/**
	 * This method is called when a selection was requested by CASAActuator.
	 * It will check if a cycling process is running and then try to perform the selecion.
	 * If the selection is at the group level (level 1 component), a new phase of the
	 * cycling process starts, through the commands. If cycling is already at the
	 * second level, it will try to issue the selected command to CASAActuator
	 */
	public void selectionRequested() {
		Component c = getActiveComponent();
		if (c != null) {
			if (c instanceof MyGUIButton) {
				GUI.addMessage("Selecionado comando -> " + c);
				stopCycling();
				this.interrupt();
				sendCommand(((MyGUIButton)c).toCommand());
			} else {
				GUI.addMessage("Grupo selecionado");
				setSelectedComponent(getActiveComponent());
			}
		}
	}

	/**
	 * Main thread method. This will  actually handle the cycling process
	 */
	@Override
	public void run() {
		try {
			Robot robot = new Robot();
			robot.mouseMove((int)MouseInfo.getPointerInfo().getLocation().getX()+1,
					(int)MouseInfo.getPointerInfo().getLocation().getY()+1);
		} catch (Exception e) {
			//TODO
		}
		// Change TV set to menu and start a thread.
		// TODO: Move the command macro and TTS text to a configurable section
		sendCommand(new Command().setCommandTp(Command.TP_STRING).setData("TVIOS;Olá"));

		// Wait 7s to start, so there is time to switch the TV set source.
		try {
			Thread.sleep(Long.parseLong(Config.getGeneralConfigEntry("sourceTVDelay","8000")));
		} catch (Exception e) {
			// Nothing to do, just print the error
			e.printStackTrace();
		}
		for (Component comp : level1ComponentList) {
			setActiveComponent(comp);

			// Backup previous color
			Color cl = comp.getBackground();
			
			// Set to yellow
			comp.setBackground(YELLOW);
			comp.getParent().setBackground(YELLOW);
			comp.getParent().getParent().setBackground(YELLOW);
			
			// Wait for action
			waitForAction();
			
			// Set back to previous color
			comp.setBackground(cl);
			comp.getParent().setBackground(cl);
			comp.getParent().getParent().setBackground(cl);
			
			// Check if it was selected
			if (getSelectedComponent() != null) {
				
				// Yes, it was. Start cycling inside this group
				Component rootComponent = getSelectedComponent();
				setSelectedComponent(null);
				for (Component level2Comp : level2ComponentMap.get(rootComponent)) {
					setActiveComponent(level2Comp);
					Color cl2 = level2Comp.getBackground();
					level2Comp.setBackground(YELLOW);
					waitForAction();
					level2Comp.setBackground(cl2);
					if (stopCycling) {
						break;
					}
				}
			}
			if (stopCycling) {
				break;
			}
		}
		
		// Switch back to previous source
		// TODO: Move command macro to a configurable place
		sendCommand(new Command().setCommandTp(Command.TP_STRING).setData("TVIOD"));
	}

	
	/**
	 * Standard waiting time between groups/actions cycling
	 */
	private void waitForAction() {
		try {
			Thread.sleep(Long.parseLong(Config.getGeneralConfigEntry("cyclingInterval","2500")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Flags this thread to stop cycling actions.
	 */
	private void stopCycling() {
		this.stopCycling = true;
	}
}
