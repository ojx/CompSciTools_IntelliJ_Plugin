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

package sideWindow;

/**
 * This class serves to represent a result of a test.
 * Is composed of: the name of the test ; a node if it exists (the node served here to regroup some tests based on the response of the API ;
 * the message of the test if there is one ; a string which represents the success or not of the test
 *
 * @author Joshua Monteiller
 */
public class TestResult {

    public static final String SUCCESS = "Pass";
    public static final String ERROR = "Err";
    public static final String FAILURE = "Fail";

    /**
     * Name of the test result
     */
    private String name;

    /**
     * "Node" of the test result. The node is not in every test of every exercise, but it is useful to assemble different commons tests
     */
    private String nodeName;

    /**
     * Message accompanying the test result. It's often an error message
     */
    private String message;

    /**
     * A string that represent if the test was successful or not. "Pass" for a successful test, "Fail" for a failure and "Err" for an error
     */
    private String success;

    public TestResult() {
    }

    public TestResult(String name) {
        this.name = name;
        this.nodeName = "";
        this.message = "";
        this.success = SUCCESS;
    }

    public TestResult(String name, String nodeName, String message, String success) {
        this.name = name;
        this.nodeName = nodeName;
        this.message = message;
        this.success = success;
    }

    /**
     * Return the name of the test result
     */
    public String getName() {
        return name;
    }

    /**
     * Return the node of the test result
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Return the message of the test result
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the string representing the success of the test result
     */
    public String getSuccess() {
        return success;
    }

    /**
     * Allow to set the success of the test result.
     * Useful only in the case of the node in the TestResultTreeCellRenderer.
     *
     * @param success the string which define the success
     */
    public void setSuccess(String success) {
        this.success = success;
    }


    /**
     * Delete from the name given in argument, the numbering in it.
     * Example: "Test succeed (1/8)"  -> "Test succeed"
     *
     * @param name the raw name of the test result
     * @return the name without the numbering
     */
    private String deleteNumbering(String name) {
        while (name.contains("(") && name.contains(")")) {
            String numbering = name.substring(name.indexOf('('), name.indexOf(')') + 1);
            name = name.replace(numbering, "");
        }
        return name.trim();
    }

    /**
     * A long method who serves to parse a String.
     * The string is the result found in the JSON, found in the response of the VPL service.
     *
     * @param resultText the string found in the JSON response file
     */
    public void parseText(String resultText) {
        //If the text has a point, it means it may have a node
        int endLine = resultText.indexOf('\n');
        if (resultText.contains(".")) {
            int parLine = resultText.indexOf('(');
            //If there is no '\n', it means that all the text is contained on one line
            if (endLine == -1) {
                int endNode;
                //We try to found if the point is before or between the parenthesis
                if (parLine != -1) {
                    endNode = resultText.lastIndexOf('.', parLine);
                } else {
                    endNode = resultText.lastIndexOf('.');
                }
                if (endNode == -1) {
                    //Very particular case where there is no node, only one line and the point are in the grade in the name
                    name = deleteNumbering(resultText);
                } else {
                    //It searches for the last point who separate the node and the name of the result
                    nodeName = resultText.substring(0, endNode);
                    name = deleteNumbering(resultText.substring(endNode + 1));
                }
            } else {
                int endNode = 0;
                //We try to found if the point is in the first line and if is before the parenthesis
                if (parLine == -1 || parLine > endLine) {
                    endNode = resultText.lastIndexOf('.', endLine);
                } else if (parLine < endLine) {
                    endNode = resultText.lastIndexOf('.', parLine);
                }

                //If there are multiple lines, maybe the point is not in the first line and so maybe there is no node
                if (endNode != -1) {
                    //The point is where we expected to it to be
                    this.nodeName = resultText.substring(0, endNode);
                    name = resultText.substring(endNode + 1, endLine);
                } else {
                    //The point is in the error message and not in the name so there is no node
                    name = resultText.substring(0, endLine);
                }
                name = deleteNumbering(name);
            }
        } else {
            //If there is no point, the name is only on the first line, which may be the only one
            name = endLine == -1 ? resultText : resultText.substring(0, endLine);
            name = deleteNumbering(name);
        }
        //If there is a message, all the text of the message is at the end
        message = endLine == -1 ? "" : resultText.substring(endLine + 1);

        //The success or not of a result is based only on if there is these particular texts
        if (resultText.contains("Incorrect") || resultText.contains("AssertionError")) {
            success = TestResult.FAILURE;
        } else if (resultText.contains("-The compilation") || resultText.contains("error")) {
            success = TestResult.ERROR;
        } else {
            success = TestResult.SUCCESS;
        }
    }
}
