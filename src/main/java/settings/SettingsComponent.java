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

package settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * This class supports creating and managing a {@link JPanel} for the settings window.
 * It provides all the UI needed for the settings
 *
 * @author Joshua Monteiller
 */
public class SettingsComponent {

    private final JPanel myMainPanel;
   // private final JBTextField customURLText = new JBTextField();
   // private final JRadioButton csToolsURLButton = new JRadioButton("CompSci Tools");
   // private final JRadioButton customURLButton = new JRadioButton("Custom:");
    private final JPasswordField tokenPassword = new JPasswordField();
    /*private final JRadioButton recommendedButton = new JRadioButton("Listen to execution server via websocket (recommended)");
    private final JRadioButton compatibilityButton = new JRadioButton("Wait for a few seconds (compatibility)");
*/
    public SettingsComponent() {
        //The buttons group of URL RadioButtons
     /*   ButtonGroup URLRadioButtons = new ButtonGroup();
        URLRadioButtons.add(csToolsURLButton);
        URLRadioButtons.add(customURLButton);
        csToolsURLButton.setSelected(true);
        customURLText.setEnabled(false);*/

        //The buttons group of way of evaluate RadioButtons
        /*ButtonGroup evaluationRadioButtons = new ButtonGroup();
        evaluationRadioButtons.add(recommendedButton);
        evaluationRadioButtons.add(compatibilityButton);
        recommendedButton.setSelected(true);*/

        myMainPanel = FormBuilder.createFormBuilder()
                /*   .addComponent(new JBLabel("VPL Webservice URL: "), 1)
               .addComponent(csToolsURLButton, 1)
                 .addComponent(customURLButton, 1)
                 .addComponent(customURLText, 1)*/
                .addComponent(new JBLabel("User Security (Token): "), 1)
                .addComponent(tokenPassword)
               /* .addComponent(new JBLabel("Evaluation method: "), 1)
                .addComponent(recommendedButton)
                .addComponent(compatibilityButton)*/
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    //Each method that follows here is used in the file SettingsConfigurable.java when a setting is modified or when the settings are applied or reset.
    //Most of these methods work in a pair of two methods who set and get something

    /*public String getCustomURL() {
        return customURLText.getText();
    }*/

  /*  public void setCustomURL(@NotNull String newText) {
        customURLText.setText(newText);
    }

    public void setCustomURLText(boolean newStatus) {
        customURLText.setEnabled(newStatus);
    }

    public boolean getCsToolsRadioStatus() {
        return csToolsURLButton.isSelected();
    }

    public void setCsToolsRadioStatus(boolean newStatus) {
        csToolsURLButton.setSelected(newStatus);
    }

    public boolean getCustomRadioStatus() {
        return customURLButton.isSelected();
    }

    public void setCustomRadioStatus(boolean newStatus) {
        customURLButton.setSelected(newStatus);
    }*/

    public String getToken() { //Because it's a password field, we need a little more to get the token in a string form
        StringBuilder pass = new StringBuilder();
        char[] word = tokenPassword.getPassword();
        for (char c : word) {
            pass.append(c);
        }
        return pass.toString();
    }

    public void setToken(@NotNull String newText) {
        tokenPassword.setText(newText);
    }

   /* public boolean getRecommendedRadioStatus() {
        return recommendedButton.isSelected();
    }

    public void setRecommendedRadioStatus(boolean newStatus) {
        recommendedButton.setSelected(newStatus);
    }

    public boolean getCompatibilityRadioStatus() {
        return compatibilityButton.isSelected();
    }

    public void setCompatibilityRadioStatus(boolean newStatus) {
        compatibilityButton.setSelected(newStatus);
    }*/
}
