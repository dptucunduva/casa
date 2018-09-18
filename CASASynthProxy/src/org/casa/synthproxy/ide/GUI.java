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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.casa.synthproxy.config.Command;
import org.casa.synthproxy.config.CommandGroup;
import org.casa.synthproxy.config.Config;

/**
 * This is the class that models the application's GUI.
 * 
 * @author Daniel Parra Tucunduva
 */
public class GUI {
	
	/** Date format mask **/
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
	/** LOG/Message textarea **/
	private static JTextArea textArea;
	/** Scrollpane in which the textarea will be inserted **/
	private static JScrollPane sp;
	/** Main windows frame **/
	private static JFrame frame;
	
	/** Upper left pannel - this is the first command group and will host commands **/
	private static JPanel leftUpperPanel = new JPanel();
	/** Lower left pannel - this is the second command group and will host commands **/
	private static JPanel leftLowerPanel = new JPanel();
	/** Upper right pannel - this is the third command group and will host commands **/
	private static JPanel rightUpperPanel = new JPanel();
	/** Lower right pannel - this is the forth command group and will host commands **/
	private static JPanel rightLowerPanel = new JPanel();
	/** Array that hosts all panels **/
	private static JPanel panelList[] = {leftUpperPanel, rightUpperPanel, leftLowerPanel, rightLowerPanel};

	/**
	 * Init GUI.
	 * @param config Previously loaded configuration
	 */
	public static void initGUI(Config config) {
		// Default decoration
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		// Window creation
		frame = new JFrame("CASA Synthetizer Proxy");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Up to 4 command groups - that would be 4 different panes
		JSplitPane leftVSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftUpperPanel, leftLowerPanel);
		leftVSplitPane.setResizeWeight(.5d);
		leftVSplitPane.setEnabled( false );
		JSplitPane rightVSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightUpperPanel, rightLowerPanel);
		rightVSplitPane.setResizeWeight(.5d);
		rightVSplitPane.setEnabled( false );
		JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftVSplitPane, rightVSplitPane);
		hSplitPane.setResizeWeight(.5d);
		hSplitPane.setEnabled( false );
		frame.getContentPane().add(hSplitPane);
		
		// LOG and user messages area
		textArea = new JTextArea();
		sp = new JScrollPane(textArea); 
		sp.setPreferredSize(new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()
				,100));
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		frame.getContentPane().add(sp, BorderLayout.SOUTH,-1);
		
		// Create visual entities from configuration
		handleConfig(config);
 
		// Show the window
		frame.setLocation(40, 40);
		frame.setSize((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()-80, 
				(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()-80);
		frame.setVisible(true);
	}

	/**
	 * Add a message to the log/message area.
	 * This method takes this message and prefix it with a formatted timestamp.
	 * @param message Message to be added.
	 */
	public static void addMessage(String message) {
		textArea.append("[" + sdf.format(new Date()) + "] " + message + "\n");
		
		JScrollBar sb = sp.getVerticalScrollBar();
		sb.setValue( sb.getMaximum() );
	}
	
	/**
	 * Handle configuration. This method will take the configuration and create all visual entities.
	 * @param config Configuration that was previously loaded by CASASynthProxy
	 */
	private static void handleConfig(Config config) {
		try {
			// For each group (up to 4)
			for (int i = 0; i < 4 && i < config.getGroups().size(); i++) {
				CommandGroup commandGroup = config.getGroups().get(i);
				CycleThread ct = CycleThread.getInstance();
				
				JPanel outer = new JPanel();
				outer.setBorder(BorderFactory.createTitledBorder(commandGroup.getName()));
				JPanel inner = new JPanel();
				outer.add(inner);
				inner.setLayout(new GridLayout(4,4));
				panelList[i].add(outer);
				
				ct.addRootComponent(inner);

				for (int j = 0; j < commandGroup.getCommands().size(); j++) {
					Command command = commandGroup.getCommands().get(j);
					
					MyGUIButton jCommand = new MyGUIButton(command.getLabel(), command.getData(),
							command.getTts());
					Font font = new Font("Arial",Font.PLAIN,(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/55);
					jCommand.setPreferredSize(new Dimension(JFrame.getFrames()[0].getFontMetrics(font).stringWidth(jCommand.getText()) + 50,70));
					jCommand.setFont(font);
					jCommand.addActionListener(new GUICommandHandler(jCommand));
					inner.add(jCommand);
					ct.addSecondLevelComponent(inner, jCommand);
				}
			}
		} catch (Exception e) {
			GUI.addMessage("Erro lendo configuração!");
			e.printStackTrace();
		}
	}
}
