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

import ui.icons.CompSciToolsColors;
import ui.icons.CompSciToolsIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

import static sideWindow.TestResult.*;


/**
 * This class is used to show on the result test tree of CompSciToolsSideWindows the name of the test and the linked icon of its results
 * This class is used to show on the result test tree of CompSciToolsSideWindows the name of the test and the linked icon of its results
 */
public class TestResultTreeCellRenderer implements TreeCellRenderer {
    /**
     * The JLabel who serves to show the text and the icon
     */
    private final JLabel label;

    TestResultTreeCellRenderer() {
        label = new JLabel();
    }

    /**
     * Can make a label with the appropriate text and icon depending on if it's a leaf or a node, on the result of the test or the results of each child.
     *
     * @param tree     the receiver is being configured for
     * @param value    the value to render
     * @param selected whether node is selected
     * @param expanded whether node is expanded
     * @param leaf     whether node is a lead node
     * @param row      row index
     * @param hasFocus whether node has focus
     * @return the finished component
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {


        int childCount = ((DefaultMutableTreeNode) value).getChildCount();

        //If the actual node is a leaf
        if (childCount == 0) {
            //Retrieve the test result from the node
            TestResult testResult = (TestResult) ((DefaultMutableTreeNode) value).getUserObject();
            //Set the name
            String name = testResult.getName();
            label.setText(name);
            //Depends on the success, set a different icon
            switch (testResult.getSuccess()) {
                case SUCCESS:
                    label.setIcon(CompSciToolsIcons.CaseOk);
                    label.setForeground(CompSciToolsColors.GREEN);
                    break;
                case FAILURE:
                    label.setIcon(CompSciToolsIcons.CaseFail);
                    label.setForeground(CompSciToolsColors.OUTSTANDING_GRAY);
                    break;
                case ERROR:
                    label.setIcon(CompSciToolsIcons.CaseError);
                    label.setForeground(CompSciToolsColors.RED);
                    break;
            }
        } else {
            //Else, the actual node is a node with multiples leafs and maybe others nodes
            int s = 0, f = 0, e = 0; //The number of success, failures and errors in the children
            for (int i = 0; i < childCount; i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) value).getChildAt(i);
                TestResult childTestResult = (TestResult) childNode.getUserObject();
                //Depends on the success, increment a different int
                switch (childTestResult.getSuccess()) {
                    case SUCCESS:
                        s++;
                        break;
                    case FAILURE:
                        f++;
                        break;
                    case ERROR:
                        e++;
                        break;
                }
                //If there is any errors, we are 100% sure that the node will be in an Error state
                if (e != 0) {
                    break;
                }
            }
            //Depends on the numbers, set a different icon and the success of the node
            TestResult testResult = (TestResult) ((DefaultMutableTreeNode) value).getUserObject();
            if (e != 0) {
                label.setIcon(CompSciToolsIcons.SuiteError);
                label.setForeground(CompSciToolsColors.RED);
                testResult.setSuccess(ERROR);
            } else if (f != 0) {
                label.setIcon(CompSciToolsIcons.SuiteFail);
                label.setForeground(CompSciToolsColors.OUTSTANDING_GRAY);
                testResult.setSuccess(FAILURE);
            } else if (s != 0) {
                label.setIcon(CompSciToolsIcons.SuiteOk);
                label.setForeground(CompSciToolsColors.GREEN);
                testResult.setSuccess(SUCCESS);
            } else {
                label.setIcon(null);
            }
            ((DefaultMutableTreeNode) value).setUserObject(testResult);
            //Set the name
            label.setText(testResult.getName());
        }

        return label;
    }
}
