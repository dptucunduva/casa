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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Text synthesizer. This class currently only supports Microsoft Windows.
 * 
 * This helper class will create a VBS script and start it in a separate process that
 * will speak the sentence.
 */
public class VoiceSynth {
	
	// Text to be synthesized
	private String sentence;
	
	/**
	 * VoiceSynth Constructor.
	 * 
	 * This constructor receives the sentence that should be synthesized.
	 * 
	 * @param sentence Sentence to be synthesized
	 */
	public VoiceSynth(String sentence) {
		this.sentence = sentence;
	}
	
	/**
	 * Say it through a VBS script
	 */
	public void sayit() {
		FileWriter fw = null;
		try {
			String filename = System.getProperty("java.io.tmpdir") + File.separatorChar + UUID.randomUUID().toString() + ".vbs";
			fw = new FileWriter(filename);
			fw.write("\'\r\n");
			fw.write("set speech = Wscript.CreateObject(\"SAPI.spVoice\")\r\n");
			fw.write("speech.speak \"" + this.sentence + "\"");
			fw.close();
			
			runit(filename);
		} catch (IOException ioE) {
			// Nothing to do, just print the error.
			ioE.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// Nothing to do, just print the error.
					e.printStackTrace();
				};
			}
		}
	}
	
	/**
	 * Run VBS script that will use windows TTS functionality to synthesize the text.
	 * @param filename VBS script filename - full path.
	 * @throws IOException Error executing VBS script
	 */
	private void runit(String filename) throws IOException {
		 Runtime.getRuntime().exec("cscript " + filename);
	}
}
