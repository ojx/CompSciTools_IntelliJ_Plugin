/*
 * CompSci Tools Plugin for IntelliJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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

package module;

import service.PersistentStorage;
import service.WebserviceClientFactory;
import ui.icons.CompSciToolsColors;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.exception.MoodleWebServiceException;
import vplwsclient.exception.VplConnectionException;

import javax.json.JsonObject;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static service.PersistentStorage.DEFAULT_NUMBER;

/**
 * This class represents the "New Project" window for the Casein plugin.
 * It allows to make a new project with the user's token and the ID of the exercise.
 * Used a .form file to create the UI.
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsModuleWizardStep extends com.intellij.ide.util.projectWizard.ModuleWizardStep {

    @SuppressWarnings("unused")
    private JPanel MainPanel;
    @SuppressWarnings("unused")
    private JCheckBox initCheck;
    @SuppressWarnings("unused")
    private JTextPane initialProjectTextPane;
    @SuppressWarnings("unused")
    private JPanel InitialPanel;
    @SuppressWarnings("unused")
    private JTextPane errorTextField;
    @SuppressWarnings("unused")
    private JLabel errorIcon;
    @SuppressWarnings("unused")
    private JScrollPane errorTextScrollPane;
    @SuppressWarnings("unused")
    private JTextField wsURL;
    @SuppressWarnings("unused")
    private JLabel okIcon;
    private JTextPane wsText;


    @Override
    public JComponent getComponent() {
        //Retrieve the token of the user in the storage
        PersistentStorage state = PersistentStorage.getInstance();
        state.vplID = DEFAULT_NUMBER;

        //Set the state of the button to false at the beginning
        state.initCheckButtonSelected = false;
        initCheck.addActionListener(e -> state.initCheckButtonSelected = initCheck.isSelected());

        wsText.setBackground(CompSciToolsColors.NONE);
        initialProjectTextPane.setBackground(CompSciToolsColors.NONE);
        errorTextField.setBackground(CompSciToolsColors.NONE);
        wsURL.setColumns(1);

        // A KeyListener for when the user typed
        wsURL.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                //System.out.println(wsURL.getText());
                if (wsURL.getText().trim().isEmpty()) {
                    clearValidationError();
                } else {
                    try {
                        URI uri = URI.create(wsURL.getText());
                        URL url = uri.toURL(); // Convert only after validation
                        if (url.getHost().toLowerCase().equals("compsci.tools")) {
                            String queryString = url.getQuery();
                            Map<String, String> params = new HashMap<>();
                            if (queryString != null && !queryString.isEmpty()) {
                                String[] pairs = queryString.split("&");
                                for (String pair : pairs) {
                                    int idx = pair.indexOf("=");
                                    if (idx > 0) {
                                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                                        params.put(key, value);
                                    }
                                }
                            }
                            if (params.containsKey("id") && params.containsKey("wstoken")) {
                                try {
                                    int vplId = Integer.parseInt(params.get("id"));
                                    String wsToken = params.get("wstoken");

                                    Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
                                    Matcher matcher = pattern.matcher(wsToken);

                                    if (wsToken.length() > 10 && matcher.matches()) {
                                        clearValidationError();
                                        okIcon.setVisible(true);
                                        state.userToken = wsToken;
                                        state.vplID = vplId + "";
                                        checkService();
                                    } else {
                                        showValidationError("Invalid service token");
                                    }
                                } catch (NumberFormatException e) {
                                    showValidationError("Invalid service ID");
                                }
                            }
                        } else {
                            showValidationError("Invalid Webservice URL server");
                        }
                    } catch (MalformedURLException e) {
                        showValidationError("Invalid Webservice URL");
                    }
                }
            }
        });


        //Configure the error message that appears when the user typed a wrong value for the token or the ID
        errorTextScrollPane.setBorder(BorderFactory.createEmptyBorder());
        clearValidationError();

        return MainPanel;
    }

    private void showValidationError(String message) {
        okIcon.setVisible(false);
        errorTextField.setText(message);
        errorIcon.setVisible(true);
        errorTextScrollPane.setVisible(true);
    }

    private void clearValidationError() {
        okIcon.setVisible(false);
        errorIcon.setVisible(false);
        errorTextScrollPane.setVisible(false);
        errorTextField.setText("");
    }

    private void checkService() {
        try {
            JsonObject jo = WebserviceClientFactory.createFromStorageState().callService(VPLService.VPL_INFO);
            if (jo.containsKey("name")) {
                okIcon.setVisible(true);
                errorTextField.setText(jo.getString("name"));
                errorIcon.setVisible(false);
                errorTextScrollPane.setVisible(true);
            } else {
                showValidationError("Invalid service response. Please " +
                        "make a new copy of the URL on https://compsci.tools");
            }
        } catch (VplConnectionException | MoodleWebServiceException e) {
            //   if (!"".equals(VPL_IDTextField.getText()) && !"".equals(tokenPassword.getPassword().toString())) {
            showValidationError("Web service connection rejected. Please " +
                    "make a new copy of the URL on https://compsci.tools");
            //   }
        }
    }


    @Override
    public void updateDataModel() {
        // Nothing to do.
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
