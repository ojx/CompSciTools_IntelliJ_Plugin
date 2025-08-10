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
 * Some functions are under this different licence. Here the list: getDisplayName() & createComponent()
 */

package settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import service.PersistentStorage;

import javax.swing.*;

/**
 * This class provides controller functionality for application settings.
 *
 * @author Joshua Monteiller
 */
public class SettingsConfigurable implements Configurable {

    private SettingsComponent mySettingsComponent;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "CompSci Tools Settings";
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new SettingsComponent();
        return mySettingsComponent.getPanel();
    }

    /**
     * A method used to verify if anything is modified in the settings
     *
     * @return a boolean, true if anything was modified in the settings, false if not
     */
    @Override
    public boolean isModified() {
        PersistentStorage settings = PersistentStorage.getInstance();
        //Allow enabling and disabling of the custom text field
      // mySettingsComponent.setCustomURLText(mySettingsComponent.getCustomRadioStatus());
        //If anything is modified, the boolean will be at true
        boolean modified = /*!mySettingsComponent.getCustomURL().equals(settings.customURL);
        modified |= mySettingsComponent.getCsToolsRadioStatus() != settings.csToolsRadioButtonSelected;
        modified |= mySettingsComponent.getCustomRadioStatus() != settings.customRadioButtonSelected;
        modified |= */!mySettingsComponent.getToken().equals(settings.userToken);
        /*modified |= mySettingsComponent.getRecommendedRadioStatus() != settings.recommendedRadioButtonSelected;
        modified |= mySettingsComponent.getCompatibilityRadioStatus() != settings.compatibilityRadioButtonSelected;*/
        return modified;
    }

    /**
     * Apply the settings chosen by the user and store them in the Storage
     */
    @Override
    public void apply() {
        PersistentStorage settings = PersistentStorage.getInstance();
     /*   settings.customURL = mySettingsComponent.getCustomURL();
        settings.csToolsRadioButtonSelected = mySettingsComponent.getCsToolsRadioStatus();
        settings.customRadioButtonSelected = mySettingsComponent.getCustomRadioStatus();*/
        settings.userToken = mySettingsComponent.getToken();
      /*  settings.recommendedRadioButtonSelected = mySettingsComponent.getRecommendedRadioStatus();
        settings.compatibilityRadioButtonSelected = mySettingsComponent.getCompatibilityRadioStatus();*/
    }

    /**
     * Reset the settings at theirs previous state when the last "Apply" button was clicked
     */
    @Override
    public void reset() {
        PersistentStorage settings = PersistentStorage.getInstance();
       /* mySettingsComponent.setCustomURL(settings.customURL);
        mySettingsComponent.setCsToolsRadioStatus(settings.csToolsRadioButtonSelected);
        mySettingsComponent.setCustomRadioStatus(settings.customRadioButtonSelected);*/
        mySettingsComponent.setToken(settings.userToken);
      /*  mySettingsComponent.setRecommendedRadioStatus(settings.recommendedRadioButtonSelected);
        mySettingsComponent.setCompatibilityRadioStatus(settings.compatibilityRadioButtonSelected);*/
    }


}
