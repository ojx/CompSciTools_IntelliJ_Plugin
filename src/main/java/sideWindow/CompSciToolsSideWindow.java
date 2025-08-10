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

package sideWindow;

import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.Gray;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import service.PersistentStorage;
import service.ServiceGetter;
import ui.icons.CompSciToolsColors;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.exception.MoodleWebServiceException;
import vplwsclient.exception.VplConnectionException;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static sideWindow.TestResult.*;

/**
 * The class CompSciToolsSideWindow represent the window that appeared normally on the right side of the IntelliJ window.
 * The plugin will use this class to show to the user the description of the exercise and the result of it once it had been evaluated.
 * Use a .form file to create the UI.
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsSideWindow {

    /////////User Interface////////
    @SuppressWarnings("unused")
    private JPanel myToolWindowContent;
    @SuppressWarnings("unused")
    private JTextPane exerciseNameTextPane;
    @SuppressWarnings("unused")
    private JTextPane descriptionExercise;
    @SuppressWarnings("unused")
    public JTree resultsTree;
    @SuppressWarnings("unused")
    private JProgressBar gradeProgressBar;
    @SuppressWarnings("unused")
    private JTextField gradeResult;
    @SuppressWarnings("unused")
    private JTextField nbFailures;
    @SuppressWarnings("unused")
    private JTextField nbTests;
    @SuppressWarnings("unused")
    private JButton reloadButton;
    @SuppressWarnings("unused")
    private JTextPane messageResult;
    @SuppressWarnings("unused")
    private JTabbedPane TabbedPane;
    @SuppressWarnings("unused")
    private JPanel descriptionPanel;
    @SuppressWarnings("unused")
    private JPanel resultPanel;
    @SuppressWarnings("unused")
    private JPanel layout2;
    @SuppressWarnings("unused")
    private JScrollPane treeScrollPane;
    @SuppressWarnings("unused")
    private JScrollPane messageScrollPane;
    @SuppressWarnings("unused")
    private JScrollPane descriptionScrollPane;
    @SuppressWarnings("unused")
    private JToolBar csToolBar;
    private JTextField wsURL;
    private JTextPane wsText;



    /////////Variables////////
    /**
     * The array of the different tests, represented by the class TestResult
     */
    private List<TestResult> listTestResult;

    /**
     * The tree model of the JTree which is used to show all the tests and their result
     */
    private DefaultTreeModel treeModel;

    /**
     * A class which is used to get all the important stuff from the web API
     */
    private final ServiceGetter servGet;

    private final String DARK_HEX = "#444444";
    private final String LIGHT_HEX = "#F4F4F4";

    /**
     * The constructor which is used to set multiple texts and an action listener.
     * However, the JTree is initialized in a different method {@link #createUIComponents() createUIComponents}
     *
     * @param path the path to the special file which contains the VPL ID
     */
    public CompSciToolsSideWindow(String path) {
        //Initialize the ServiceGetter
        this.servGet = new ServiceGetter(path);

     /*   System.out.println("Side Window started");
        System.out.println(this.servGet.getDescription());
        System.out.println(this.servGet.hasFailed());
*/
        wsText.setBackground(CompSciToolsColors.NONE);
        exerciseNameTextPane.setBackground(CompSciToolsColors.NONE);
        exerciseNameTextPane.setDisabledTextColor(CompSciToolsColors.BW);
        exerciseNameTextPane.setForeground(CompSciToolsColors.BW);

        StyledDocument doc = exerciseNameTextPane.getStyledDocument();
        SimpleAttributeSet centerAttribute = new SimpleAttributeSet();
        StyleConstants.setAlignment(centerAttribute, StyleConstants.ALIGN_CENTER);

        ApplicationManager.getApplication().getMessageBus().connect().subscribe(UISettingsListener.TOPIC, new UISettingsListener() {
            @Override
            public void uiSettingsChanged(@NotNull UISettings uiSettings) {
         //       System.out.println("uiSettingsChanged");
                descriptionExercise.setText(servGet.getDescription());
                String currentResult = messageResult.getText();

                final String style1 = "background-color: " + LIGHT_HEX;
                final String style2 = "background-color: " + DARK_HEX;

                int style1Index = currentResult.indexOf(style1);
                int style2Index = currentResult.indexOf(style2);

                String style;

                if (style1Index != -1 && style2Index != -1 && style1Index < style2Index) {
                    style = style1;
                } else if (style1Index != -1 && style2Index != -1) {
                    style = style2;
                } else if (style1Index != -1) {
                    style = style1;
                } else if (style2Index != -1) {
                    style = style2;
                } else {
                    return;
                }

                int styleIndex = currentResult.indexOf(style);

                if (styleIndex >= 0) {
                    String newResult = currentResult.substring(0, styleIndex + style.length() - DARK_HEX.length());
                    newResult += getHexBackgroundCode();
                    newResult += currentResult.substring(styleIndex + style.length());

                    messageResult.setText(newResult);
                }
            }
        });

        try {
            doc.setParagraphAttributes(0, doc.getLength(), centerAttribute, false);
        } catch (Exception e) {
           // e.printStackTrace();
        }

        if (servGet.hasFailed()) {

            wsURL.setVisible(true);
            PersistentStorage state = PersistentStorage.getInstance();
            String vplID = state.getProjectVplID(path);
            exerciseNameTextPane.setText("Unknown Project: " + vplID);

            wsText.setVisible(true);

            wsURL.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent keyEvent) {
                    //System.out.println(wsURL.getText());
                    if (wsURL.getText().trim().isEmpty()) {
                        clearValidationError();
                    } else {
                        try {
                            URL url = new URL(wsURL.getText());
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
                                            state.userToken = wsToken;
                                            state.vplID = vplId + "";
                                            servGet.update();
                                            if (servGet.hasFailed()) {
                                                showValidationError("Unknown project: " + servGet.getExerciseName());
                                            } else {
                                                wsURL.setVisible(false);
                                                wsText.setVisible(false);
                                                descriptionExercise.setText(servGet.getDescription());
                                                exerciseNameTextPane.setText(servGet.getExerciseName());
                                                initResults(path);
                                            }
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
        } else {
            wsURL.setVisible(false);
            wsText.setVisible(false);
        }

       //Show the exercise name
        exerciseNameTextPane.setText(servGet.getExerciseName());

        // Add action bar
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup("COMPSCI_TOOLS_ACTION", true);
        actionGroup.add(ActionManager.getInstance().getAction("cstools.reset"));
        actionGroup.add(ActionManager.getInstance().getAction("cstools.pull"));
        actionGroup.add(ActionManager.getInstance().getAction("cstools.push"));
        actionGroup.add(ActionManager.getInstance().getAction("cstools.evaluate"));
        ActionToolbar actionToolbar = actionManager.createActionToolbar("COMPSCI_TOOLBAR_TOOLBAR", actionGroup, false);
        actionToolbar.setOrientation(SwingConstants.HORIZONTAL);
        actionToolbar.setTargetComponent(csToolBar);
actionToolbar.getComponent().setBackground(new Color(54, 86, 116));
        csToolBar.add(actionToolbar.getComponent());

        //The description of the exercise
        descriptionExercise.setText(servGet.getDescription());

        //Add an action listener of the reload button
        reloadButton.addActionListener(e -> {
            servGet.update();
            if (!CompSciToolsSideWindowFactory.changeCsw(path)) {
                //If the button was pressed without a change of VPL ID, reload the description and name of the exercise
                descriptionExercise.setText(servGet.getDescription());
                exerciseNameTextPane.setText(servGet.getExerciseName());
                initResults(path);
            }
        });

        initResults(path);
    }

    public void showValidationError(String message) {
        descriptionExercise.setText(message);
    }

    public void clearValidationError() {
        descriptionExercise.setText("Loading project . . .");
    }

    /**
     * Set the result panel of the SideWindow.
     *
     * @param path Path to the project root.
     */
    public void initResults(String path) {
       // System.out.println("initResults");
        //Set a different text depending on if the connection with the API has failed or not
        if (servGet.hasFailed()) {
            gradeResult.setText("Expired security token.");
            setResultGrade("Expired security token.");
            setResultsTree("", "");
            //Hide the different texts from the user before the evaluation
            setTextsVisible(false);
            return;
        }

        // Retrieve last evaluation
        JsonObject lastEvaluation;
        try {
            lastEvaluation = servGet.getRJMC().callService(VPLService.VPL_GET_LAST_EVALUATION);
        } catch (VplConnectionException | MoodleWebServiceException e) {
            gradeResult.setText("Not evaluated yet");
            setTextsVisible(false);
            setResultGrade("Not evaluated yet");
            setResultsTree("", "");
            return;
        }

        if (lastEvaluation.get("grade").getValueType() == JsonValue.ValueType.NULL) {
            gradeResult.setText("Not evaluated");
            setTextsVisible(false);
            return;
        }

        //The project has already been evaluated, display the results of the last evaluation.
        JsonValue jsonDate = lastEvaluation.get("timesubmitted");
        String date;
        if (jsonDate.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber jsonNumberDate = lastEvaluation.getJsonNumber("timesubmitted");
            if (jsonNumberDate == null) {
                date = "Unknown date";
            } else if (jsonNumberDate.longValue() == 0) {
                date = "Ancient evaluation";
            } else {
                date = (new SimpleDateFormat()).format(new Date(jsonNumberDate.longValue() * 1000));
            }
        } else
            date = "Unknown date";

        setResultsTree(lastEvaluation.getString("evaluation"), date);
        setResultGrade(lastEvaluation.getString("grade"));
        //Expand the tree later at the end of the execution of this method
        setNodeExpanded(resultsTree, (DefaultMutableTreeNode) resultsTree.getModel().getRoot());
        setTextsVisible(true);
    }

    /**
     * Create the JTree which will be used to show to the user the different tests and their result.
     */
    private void createUIComponents() {
        //Make a tree with only a root. It will be expanded when an evaluation happens
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("");
        resultsTree = new Tree(top);
        treeModel = new DefaultTreeModel(top);

        //Add a listener for the tree when a node is selected
        resultsTree.addTreeSelectionListener(e -> nodeSelected());
    }


    //////______________SETTERS______________//////

    /**
     * See {@link #setResultGrade(String, boolean)} with determinate progress bar on default.
     *
     * @param resultGrade The string which represents the grade message given by the API.
     */
    public void setResultGrade(String resultGrade) {
        setResultGrade(resultGrade, false);
    }

    /**
     * A parser that parses the grade found in the JSON response after the call to get_result of the API.
     *
     * @param resultGrade The string which represents the grade message given by the API
     */
    public void setResultGrade(String resultGrade, boolean indeterminateProgressBar) {
        gradeProgressBar.setIndeterminate(indeterminateProgressBar);
        if (indeterminateProgressBar) {
            gradeProgressBar.setForeground(null);
            gradeProgressBar.setUI(new DarculaProgressBarUI());
        }
        gradeResult.setForeground(CompSciToolsColors.OUTSTANDING_GRAY);
        messageResult.setBackground(CompSciToolsColors.NONE);

        //If the grade doesn't contain a ':', we will treat it as plain text.
        if (!resultGrade.contains(":") || !resultGrade.contains("/")) {
            String finalResultGrade = resultGrade;
            ApplicationManager.getApplication().invokeAndWait(() -> {
                gradeResult.setText(finalResultGrade);
                gradeProgressBar.setValue(0);
                gradeProgressBar.setString("");
                if (!indeterminateProgressBar) {
                    gradeProgressBar.setBackground(CompSciToolsColors.NONE);
                }
            });
            return;
        }

        //Now we need to parse this message to have the exact grade and so, to set the progress bar
        //We search for the first ':', which is often found before the grade
        int index = resultGrade.indexOf(':');
        String text = (resultGrade.substring(index + 1)).trim();
        //We now search for the '/' which separate the user grade and the maximum grade
        index = text.indexOf('/');

        String grade = (text.substring(0, index))
                .trim()
                .replace(",", "."); //The grade
        String maxGrade = (text.substring(index + 1))
                .trim()
                .replace(",", "."); //The maximum grade

        //In case the grade is superior to the maximum grade
        if (Float.parseFloat(grade) > Float.parseFloat(maxGrade)) {
            resultGrade = resultGrade.replace(grade, maxGrade);
            grade = maxGrade;
        }

        String finalGrade = grade;
        String finalResultGrade1 = resultGrade;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            // Set the progress bar to illustrate given grade.

            gradeResult.setText(finalResultGrade1);
            gradeProgressBar.setBackground(Gray._162);
            gradeProgressBar.setForeground(CompSciToolsColors.GREEN);
            gradeProgressBar.setUI(new BasicProgressBarUI());
            gradeProgressBar.setMinimum(0);
            gradeProgressBar.setMaximum(maxGrade.equals("") ? 100 : (int) Float.parseFloat(maxGrade));
            gradeProgressBar.setValue(finalGrade.equals("") ? 0 : Math.round(Float.parseFloat(finalGrade)));
            gradeProgressBar.setString(text);
        });
    }


    /**
     * Create the tree for the JTree, which will be used to show to the user the different tests and their result.
     *
     * @param eval String got with the JSON response of get_result
     */
    public void setResultsTree(String eval, String date) {

        if (eval == null || eval.equals("")) {
            ApplicationManager.getApplication().invokeAndWait(() -> setTextsVisible(false));
            messageResult.setText("<html><head></head><body></body></html>");
            return;
        }

       /* System.out.println("#################EVAL####################");
        System.out.println(eval);
        System.out.println("#########################################");*/

        //Initialize the tree
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new TestResult("<html>Failed Tests <font color=\"#3e86a0\">[" + date + "]</font></html>"));
        treeModel = new DefaultTreeModel(top);

        //Add a listener for the tree when a node is selected
        resultsTree.addTreeSelectionListener(e -> nodeSelected());

        //Call a parser to retrieve the results of the test in the form of an array
        listTestResult = getTestResults(eval);
      //  System.out.println("SIZE:"+listTestResult.size());

        int nbT = listTestResult.size(); //Number of tests, which is also the size of the array
        int nbF = 0; //Number of failures

        //Count the number of faults and errors
     /*   for (TestResult tr : listTestResult) {

            String result = tr.getSuccess();
            if (!result.equals(SUCCESS)) {
                if (result.equals(FAILURE)) {
                    nbF++;
                } else {
                    nbE++;
                }
            }
        }*/

        //Set the texts for the number of tests, errors and failures and set it visible
     //   nbTests.setText(nbT + " Tests");
      //  nbFailures.setText("Failures: " + nbF);
        setTextsVisible(true);
    //    System.out.println(nbT + " Tests Failures: " + nbF);

        //Make a new array which represents all the primary nodes of the JTree
        List<DefaultMutableTreeNode> listNodes = new ArrayList<>();

        //For all the test results
        for (TestResult result : listTestResult) {

            //We look if it belongs to a specific node other than the root
            //If not, we add it directly in the list
           // System.out.println(result.getNodeName());
      //      System.out.println("result.getName())" + result.getName());

          /*  if (result.getNodeName() == null) {
                listNodes.add(new DefaultMutableTreeNode(result));
                continue;
            }*/

            // We search if the node is already in the array or not
            boolean added = false;
            for (int i = 0; i < listNodes.size(); i++) {
                DefaultMutableTreeNode nodeParsed = listNodes.get(i);
                TestResult resultParsed = (TestResult) nodeParsed.getUserObject();
            /*    System.out.println(i+"----------------------result.getName():");
                System.out.println(result.getName());
                System.out.println(i+"----------------resultParsed.getName():");
                System.out.println(resultParsed.getName());*/

              //WAS:  if (!result.getNodeName().equals(resultParsed.getName()))
              /*  if (!result.getName().equals(resultParsed.getName()))
                    continue;*/

               // System.out.println("add to tree . . . ");

                // We found the node. Let's add it to our tree.
                nodeParsed.add(new DefaultMutableTreeNode(result));

                //If the actual node is not already in an Error state we need to change it's state.
                if (!resultParsed.getSuccess().equals(ERROR)) {
                    //If the node is in a success state, its next state will be the same as the test result
                    if (!result.getSuccess().equals(SUCCESS)) {
                        resultParsed = new TestResult(resultParsed.getName(), resultParsed.getNodeName(), resultParsed.getMessage(), result.getSuccess());
                    }

                    //Update the node depends on the result of the new test result and the previous ones
                    nodeParsed.setUserObject(resultParsed);
                }

                //Add the node to the list so for the next loop, this node can be used
                listNodes.set(i, nodeParsed);
                added = true;
                break;
            }
            if (!added) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new TestResult(result.getNodeName()));
                //if it wasn't added (for multiple reasons), we had the child to the actual node
                newNode.add(new DefaultMutableTreeNode(result));
                listNodes.add(newNode);
            }
        }

        //We add everything in the root
        for (DefaultMutableTreeNode node : listNodes) {
            top.add(node);
        }

        //Set the tree model based on the root we have just created
        treeModel.setRoot(top);
        //Set the JTree on the model
        resultsTree.setModel(treeModel);
        //Initialize the message result
        messageResult.setText("<html><head></head><body></body></html>");
        //Set the cell renderer of the tree, it is useful to show the real name of the test result, the linked icon of the result and to expand the tree
        resultsTree.setCellRenderer(new TestResultTreeCellRenderer());
    }


    /**
     * Set the node expansion to true to allow the tree to be completely expanded.
     * Recursive method.
     *
     * @param tree The JTree which needed to be expanded
     * @param node The actual node. In the first call of the method, it is the root of the tree. After that is all the children of the root
     */
    public void setNodeExpanded(JTree tree, DefaultMutableTreeNode node) {
        //Recursive call for every Tree Node
        for (TreeNode treeNode : Collections.list(node.children())) {
            setNodeExpanded(tree, (DefaultMutableTreeNode) treeNode);
        }
        //If it is the root or if the node has no children, we don't need to use expandPath
        if (node.isRoot() || (node.getChildCount() == 0)) return;
        //If the node is a success, don't expand it
        if (((TestResult) node.getUserObject()).getSuccess().equals(SUCCESS)) return;

        //Expand the node
        tree.expandPath(new TreePath(node.getPath()));
    }

    /**
     * Set the different texts visible or not, depends on the boolean.
     * Here the texts are the texts in the side window and represent the number of tests, of faults and of errors
     *
     * @param b true the texts are visible, false the texts are not
     */
    private void setTextsVisible(boolean b) {
        nbTests.setVisible(b);
        nbFailures.setVisible(b);
        resultsTree.setVisible(b);
    }

    /**
     * Set the panel selected on the result panel.
     * This method is only called after a successful call in the Evaluate method of the class CompSciToolsAction.
     */
    public void setSelectedTab() {
        ApplicationManager.getApplication().invokeAndWait(() -> TabbedPane.setSelectedIndex(1));
    }


    /////______________GETTERS______________//////

    /**
     * Returns the global panel which contained all the UI of the side window
     *
     * @return JPanel
     */
    public JPanel getContent() {
        return myToolWindowContent;
    }

    /**
     * A parser that parses the string in argument.
     * Will return an array of TestResult which are composed of a name, a node if it exists, a message if it exists, a String which indicated if the test was successful or not.
     * A TestResult represents the result of one test
     *
     * @param evaluation this string represents the string obtained in the JSON response of the API
     * @return List<TestResult> an array which contained all the test results
     */
    private List<TestResult> getTestResults(String evaluation) {
        List<TestResult> listTestResult = new ArrayList<>();

        int testsTotal = 0;
        int passedTests = 0;

        final String summaryTitle = "-Summary of tests\n";
        final String summaryStart = "-+\n>|";
        final String summaryEnd = "|\n>+";

        int summaryIndex = evaluation.lastIndexOf("\n-Summary of tests\n");
        int testNum = 1;

        if (summaryIndex >= 0) {
            String summary = evaluation.substring(summaryIndex + summaryTitle.length()).trim();
            int sumStart = summary.indexOf(summaryStart);
            int sumEnd = summary.indexOf(summaryEnd);

            if (sumStart > 0 && sumEnd > 0) {
                summary = summary.substring(sumStart + summaryStart.length(), sumEnd).trim();

                int spcIdx = summary.indexOf(" ");
                boolean success = true;

                if (spcIdx > 0) {
                    try {
                        testsTotal = Integer.parseInt(summary.substring(0,spcIdx).trim());
                        int slashIdx = summary.indexOf("/");
                        summary = summary.substring(slashIdx + 1).trim();
                        spcIdx = summary.indexOf(" ");
                        passedTests = Integer.parseInt(summary.substring(0,spcIdx).trim());

                    } catch (NumberFormatException e) {
                       // throw new RuntimeException(e);
                        success = false;
                    }
                }

                if (success) {
                    /*System.out.println("testsTotal: " + testsTotal);
                    System.out.println("passedTests: " + passedTests);*/
                    nbTests.setText(testsTotal + " Tests");
                    nbFailures.setText("Failures: " + (testsTotal - passedTests));
                    setTextsVisible(true);
                    layout2.setVisible(true);

                    String tests = evaluation.substring(0, summaryIndex).trim();

                    final String inputTitle = "\n --- Input ---\n>";
                    final String outputTitle = "\n --- Program output ---\n>";
                    final String expectedTitle = "\n --- Expected output (exact text)---\n>";

                    String testTitle = "-Test " + testNum + ": ";
                    int testIndex = tests.indexOf(testTitle);

                    while (testIndex >= 0) {
                        tests = tests.substring(testIndex + testTitle.length()).trim();
                        int nlIndex = tests.indexOf('\n');
                        String testName = tests.substring(0, nlIndex).trim();
                        tests = tests.substring(nlIndex + 1).trim();
                        nlIndex = tests.indexOf('\n');
                        String rawMessage = tests.substring(0, nlIndex).trim();
                        String testMessage = "<p style=\"text-align: center;\">"+ rawMessage.replaceAll("\n", "<br/>")+"</p>";
                        tests = tests.substring(nlIndex);
                        int inputIndex = tests.indexOf(inputTitle);
                        int outputIndex = tests.indexOf(outputTitle);
                        int expectedIndex = tests.indexOf(expectedTitle);

                        if (inputIndex < 0 && outputIndex < 0 || expectedIndex < outputIndex) break;

                        String input = "";
                        String output = "";
                        String expected = "";

                        //  System.out.println("TESTS--------------------\n"+tests+"\n------------------------");
                        //System.out.println("i:" + inputIndex + " o:" + outputIndex + " e:" + expectedIndex);

                        if (inputIndex >= 0 && outputIndex >= 0 && inputIndex < outputIndex) {
                            // input and output
                            input = tests.substring(inputIndex + inputTitle.length(), outputIndex).trim();
                        }

                        output = tests.substring(outputIndex + outputTitle.length(), expectedIndex).trim();
                        tests = tests.substring(expectedIndex + expectedTitle.length());

                        testNum++;
                        testTitle = "-Test " + testNum + ": ";
                        testIndex = tests.indexOf(testTitle);

                        if (testIndex > 0) {
                            expected = tests.substring(0, testIndex).trim();
                        } else {
                            expected = tests.trim();
                        }

                        String testSuccess = output.equals(expected) ? SUCCESS : FAILURE;
                     //   System.out.println(" in: " + input + "\nout: " + output + "\nexp: " + expected + "\n" + success);

                        if (!input.isEmpty()) {
                            testMessage += "<p>Input:</p>\n<pre>" + input + "</pre>";
                        }
                        testMessage += "<p>Expected Output:</p>\n<pre>" + expected + "</pre>";
                        testMessage += "<p>Execution Output:</p>\n<pre>" + output + "</pre>";

                        TestResult testResult = new TestResult(testName, rawMessage , testMessage, testSuccess);
                        listTestResult.add(testResult);
                    }
                }
            }
        }
/*
        //We process the text to cut it into an array
        List<String> listString = new ArrayList<>();

        //If the first char of the string is '-', we can skip it directly
        int begin = evaluation.startsWith("-") ? 1 : 0;
        int end = evaluation.indexOf("\n-", begin);
        //The way of parsing the test results is very simple,
        //Each new test result begins directly after the previous one and end with a line break and a hyphen
        while (end != -1) {
            String sub = evaluation.substring(begin, end);
            listString.add(sub);
            begin = end + 2;
            end = evaluation.indexOf("\n-", begin);
        }
        //We may have not found any more line breaks, but it doesn't mean we have all the test results
        String sub = evaluation.substring(begin);
        while (sub.endsWith("\n")) {
            sub = sub.substring(0, sub.length() - 1);
        }
        listString.add(sub);
        //Now we have an array with all the text of each result

        //An array of TestResult, which is each composed of a name, a node, a message and a boolean if it succeeds or not

        for (String resultText : listString) {
            //We skip the case of "Tests results" because it's not interesting to show to the user
            if (!resultText.equals("Tests results")) {
                TestResult tr = new TestResult();
                //Parse the text to create the TestResult based on it
                tr.parseText(resultText);

                //Everything is added to the list
                listTestResult.add(tr);
            }
        }*/

        return listTestResult;
    }


    //////______________LISTENERS______________//////


    /**
     * A listener for when a node of the JTree is selected.
     * If the node is a result text, we set the messageResult TextField with the appropriate message.
     */
    private void nodeSelected() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.resultsTree.getLastSelectedPathComponent();
        if (node == null) return;

        boolean found = false;
        if (this.listTestResult != null) {
            for (TestResult tr : this.listTestResult) {
                Object userObject = node.getUserObject();
                if (userObject instanceof TestResult) {
                    if (((TestResult) userObject).getName().equals(tr.getName())) {
                        //If found the good node, show the message with a little indication if the test has succeeded or not

                        messageResult.setText("<html><head>" +
                                "<style>pre {border: solid 1px #aaaaaa; border-radius: 5px; overflow-wrap: break-word; word-wrap: break-word; padding: 2px 5px; background-color: " + getHexBackgroundCode() + "; white-space: pre-wrap;font-size: x-small; margin-bottom: 5px; margin-top: 2px} body {padding: 3px 11px 2px 11px; font-family: sans-serif; font-size: small} div, p {font-family: sans-serif; font-size: small; margin-bottom: 4px; margin-top: 2px} </style>" +
                                "</head><body>"+tr.getMessage()+"</body></html>");
                        found = true;
                        break;
                    }
                }
            }
        }
        //If the node selected is not a child, don't show any message
        if (!found) {
            messageResult.setText("<html><head></head><body></body></html>");
        }
    }



    private String getHexBackgroundCode() {
        if (UIUtil.isUnderDarcula()) {
            return DARK_HEX;
        }

        return LIGHT_HEX;
    }

    public void showErrors(String compilation) {
        setTextsVisible(false);
        gradeProgressBar.setIndeterminate(false);
        gradeProgressBar.setValue(0);
        if (compilation.endsWith(" error\r\nNot compiled") || compilation.endsWith(" errors\r\nNot compiled")) {
            String[] lines = compilation.split("\r\n");
            gradeResult.setText("Compilation Failed (" + lines[lines.length - 2] +")");
        } else {
            gradeResult.setText("Compilation Failed");
        }
        gradeResult.setForeground(CompSciToolsColors.RED);
        messageResult.setBackground(CompSciToolsColors.RED);
     //   gradeProgressBar.setBackground(CompSciToolsColors.NONE);
        messageResult.setText("<html><head><style>pre.error {overflow-wrap: break-word; word-wrap: break-word; white-space: pre-wrap; font-size: x-small; border-style: none; padding: 0px; background-color: #C75450; color: #FFFFFF;} body {padding: 3px 11px 2px 11px;}</style></head><body>" +
                "<pre class=\"error\">" + compilation + "</pre>" +
                "</body></html>");
    }
}
