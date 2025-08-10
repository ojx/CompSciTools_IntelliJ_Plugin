/*
 * Copyright 2018, 2019: Christophe Saint-Marcel, Astor Bizard, Nicolas Catusse, Lee Yee
 *
 * This file was originally part of the Caseine Tools Plugin for Eclipse.
 * CompSci Tools Plugin for Eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Caseine Plugin for Eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with CompSci Tools Plugin for Eclipse.  If not, see <https://www.gnu.org/licenses/>.
 */

package service;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import module.CompSciToolsModuleBuilder;
import vplwsclient.RestJsonMoodleClient;

import java.io.File;

/**
 * A factory to build web-service clients.
 * @author Astor Bizard
 */
public final class WebserviceClientFactory {
	
	private WebserviceClientFactory() {
		throw new IllegalStateException("Factory class");
	}
	
	private static final IdeaPluginDescriptor PLUGIN = PluginManagerCore.getPlugin(PluginId.getId("cstools.moodle.plugin"));
	public static final String SOURCE = ""; // IntelliJ-" + PLUGIN.  () + ".v" + PLUGIN.getVersion();
	
	/**
	 * Builds a {@link RestJsonMoodleClient} from current PersistentStorage.
	 */
	public static RestJsonMoodleClient createFromStorageState() {
		PersistentStorage state = PersistentStorage.getInstance();
		return createFromCustomProperties(state.vplID, state.userToken, state.getSelectedUrl());
	}

	/**
	 * Builds a {@link RestJsonMoodleClient} from current PersistentStorage and .moodlevpl file.
	 */
	public static RestJsonMoodleClient createFromProject(String projectPath) {
		File specialFile = new File(projectPath + CompSciToolsModuleBuilder.SPECIAL_FILE_NAME);
        if (specialFile.exists()) {
        	// There is a .moodlevpl file, retrieve VPL ID from it.
        	PersistentStorage state = PersistentStorage.getInstance();
        	return createFromCustomProperties(state.getProjectVplID(projectPath), state.userToken, state.getSelectedUrl());
        } else {
        	// No .moodlevpl file, use PersistentStorage.
            return createFromStorageState();
        }
	}
	
	public static RestJsonMoodleClient createFromCustomProperties(String vplID, String token, String url) {
		/*System.out.println("VPL ID: " + vplID);
		System.out.println("Token: " + token);
		System.out.println("URL: " + url);
		System.out.println("SOURCE: " + SOURCE);*/
		vplID += "&wstoken=" + token;
		RestJsonMoodleClient wsclient = new RestJsonMoodleClient(vplID, token, url);
		wsclient.setSource(SOURCE);
		return wsclient;
	}
}