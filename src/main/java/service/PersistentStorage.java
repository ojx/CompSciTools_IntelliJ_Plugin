/*
 * CompSci Tools Plugin for IntelliJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License or
 * (at your option) any later version.
 *
 * CompSci Tools Plugin for IntelliJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/* Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * Some functions are under this different licence. Here the list: getInstance(), getState() & loadState()
 */

package service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static module.CompSciToolsModuleBuilder.SPECIAL_FILE_NAME;

/**
 * This class allows to store the application settings in a persistent way. Also stores others permanent things.
 * The {@link State} and {@link com.intellij.openapi.components.Storage} annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 *
 * @author Joshua Monteiller
 */
@State(
        name = "storage.plugin.settings",
        storages = @com.intellij.openapi.components.Storage("StoragePluginSettings.xml") //The name of the xml file which is where the data is stored
)

public class PersistentStorage implements PersistentStateComponent<PersistentStorage> {

    public static final String DEFAULT_NUMBER = "0"; //The default number use in case of an error with the given ID

    public String vplID = DEFAULT_NUMBER; //We need to store the ID between the moment when the user write the ID in the text field of a New Project wizard and the moment when the file .moodleVPL is written
    public final String csToolsURL = "https://compsci.tools/mod/vpl/webservice.php"; //The default url to use the VPL service
    public String userToken = ""; //The token of the user

    /**
     * If the initial project button is selected or not
     */
    public boolean initCheckButtonSelected = true;

    /**
     * In the class PersistentStorage, there is: the vplID, the csToolsURL, the customURL, the user token and the status of every button.
     *
     * @return the instance of the class PersistentStorage
     */
    public static PersistentStorage getInstance() {
        return ApplicationManager.getApplication().getService(PersistentStorage.class);
    }

    @Nullable
    @Override
    public PersistentStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PersistentStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    /**
     * Return the VPL ID thanks to the path given in argument.
     *
     * @param path the path to the special file which contains the VPL ID
     * @return the ID of the VPL under the form of a String
     */
    public String getProjectVplID(String path) {
        File f = new File(path + SPECIAL_FILE_NAME);
        try {
            //Read the file
            byte[] b;
            try (FileInputStream fos = new FileInputStream(f)) {
                b = fos.readAllBytes();
            }
            String number = new String(b);
            //Verify if it's an int
            Integer.parseInt(number);
            return number;
        } catch (FileNotFoundException | NumberFormatException e) {
            return DEFAULT_NUMBER; //return a default number

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSelectedUrl() {
        return this.csToolsURL;
    }
}
