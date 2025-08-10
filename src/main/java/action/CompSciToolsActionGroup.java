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

package action;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import module.CompSciToolsModuleBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Define a custom group of action to insert in different menu from the plugin.xml file.
 */
public class CompSciToolsActionGroup extends ActionGroup {
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e != null && e.getProject() != null && CompSciToolsModuleBuilder.isCompSciToolsProject(e.getProject())) {
            return new AnAction[]{
                    ActionManager.getInstance().getAction("cstools.reset"),
                    ActionManager.getInstance().getAction("cstools.pull"),
                    ActionManager.getInstance().getAction("cstools.push"),
                    ActionManager.getInstance().getAction("cstools.evaluate"),
            };
        }
        return new AnAction[]{};
    }
}
