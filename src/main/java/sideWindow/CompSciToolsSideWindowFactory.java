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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import module.CompSciToolsModuleBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import service.PersistentStorage;

import java.util.HashMap;
import java.util.Map;

import static service.PersistentStorage.DEFAULT_NUMBER;


/**
 * This class is used to construct the CompSciToolsSideWindow class and used it for the plugin to show this window.
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsSideWindowFactory implements ToolWindowFactory, DumbAware, ProjectActivity {

    /**
     * The CompSciToolsSideWindow initialized and used only in the class CompSciToolsSideWindowFactory
     */
    private static CompSciToolsSideWindow csw;

    /**
     * A Map who stores the CompSciToolsSideWindow with the path of the project linked to it
     */
    private static final Map<String, CompSciToolsSideWindow> cswMap = new HashMap<>();

    /**
     * The tool window which serves use this class to add the CompSciToolsSideWindow in IntelliJ
     */
    private static ToolWindow tw;

    /**
     * Create the tool window content.
     *
     * @param project    The current project
     * @param toolWindow The current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        //Path of the project
        String path = project.getBasePath() + "/";
        PersistentStorage state = PersistentStorage.getInstance();

        //Look if the CompSciToolsSideWindow is not initialized yet
        String vplID = state.getProjectVplID(path);
        csw = cswMap.get(path);

        //If not, create a new one and add it to the Map
        if (csw == null) {
            csw = new CompSciToolsSideWindow(path);
            if (!vplID.equals(DEFAULT_NUMBER)) {
                cswMap.put(path, csw);
            }
        }

        //Add the CompSciToolsSideWindow to the IntelliJ window
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(csw.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);

        //Store the toolWindow for the changeCsw method
        tw = toolWindow;
    }

    /**
     * The SideWindow will not be available if the project is not a CompSci Tools Project.
     * To know if the project is a valid project, we simply check if the special file is present.
     *
     * @param project The current project
     * @return {@code true} if the SideWindow should appear. {@code false} otherwise.
     */
    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return CompSciToolsModuleBuilder.isCompSciToolsProject(project);
    }

    /**
     * This method implements the extension point ProjectActivity, and will be executed when a project is opened.
     * This is a complement to the {@code shouldBeAvailable} method of this class.
     * @param project The project being opened
     * @param continuation
     * @return {@code null}
     */
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("CompSci Tools");
        if (toolWindow != null) {
            ApplicationManager.getApplication().invokeLater(() -> toolWindow.setAvailable(this.shouldBeAvailable(project)));
        }
        return null;
    }

    /**
     * Return the CompSciToolsSideWindow used by the plugin.
     * It's useful only when we need to show the result after an evaluation and when we need to build the tree of tests.
     * Calls in CompSciToolsAction class.
     *
     * @return csw the CompSciToolsSideWindow used by the plugin
     */
    public static CompSciToolsSideWindow getCsw(String path) {
        //Look if there is a CompSciToolsSideWindow with this VPL ID
        csw = cswMap.get(path);

        //If there isn't a CompSciToolsSideWindow with this ID, make a new one
        if (csw == null) {
            csw = new CompSciToolsSideWindow(path);
            cswMap.put(path, csw);
        }
        return csw;
    }

    /**
     * Allow changing the CompSciToolsSideWindow when the plugin is already running.
     * Only calls by the reload button of CompSciToolsSideWindow.
     *
     * @param newPath the path that leads to the special file which contains the VPL ID
     * @return true if the CompSciToolsSideWindow is changed, false otherwise
     */
    public static boolean changeCsw(String newPath) {
        CompSciToolsSideWindow newCsw = cswMap.get(newPath);
        if (!csw.equals(newCsw)) {
            // If there isn't a CompSciToolsSideWindow with this ID, make a new one
            if (newCsw == null) {
                csw = new CompSciToolsSideWindow(newPath);
                cswMap.put(newPath, csw);
            } else {
                csw = newCsw;
            }

            //Remove the previously used window thanks to the Content Manager
            Content c = tw.getContentManager().getContents()[0];
            tw.getContentManager().removeContent(c, true);

            //Add the new CompSciToolsSideWindow
            ContentFactory contentFactory = ContentFactory.getInstance();
            Content content = contentFactory.createContent(csw.getContent(), "", false);
            tw.getContentManager().addContent(content);
            return true;
        } else {
            return false;
        }
    }
}
