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

package evaluation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The EvaluationStatusBarWidgetFactory class represents a factory for creating instances of the EvaluationStatusBarWidget.
 * It implements the StatusBarWidgetFactory interface.
 *
 * @author GEILLER Valentin & GUEZI Yanis
 * @see StatusBarWidgetFactory
 */
public class EvaluationStatusBarWidgetFactory implements StatusBarWidgetFactory {

    /**
     * Returns the evaluation widget ID
     *
     * @return the evaluation widget ID
     */
    @Override
    public @NotNull @NonNls String getId() {
        return "CompSciTools-Evaluation";
    }

    /**
     * Returns the evaluation widget display name
     *
     * @return the evaluation widget display name
     */
    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "CompSci Tools Evaluation";
    }

    /**
     * Returns if the evaluation widget is available
     *
     * @param project the project
     * @return if the evaluation widget is available
     */
    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    /**
     * Creates an evaluation widget
     *
     * @param project the project
     * @param scope   the scope
     * @return the evaluation widget
     */
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project, @NotNull CoroutineScope scope) {
        return StatusBarWidgetFactory.super.createWidget(project, scope);
    }

    /**
     * Creates an evaluation widget
     *
     * @param project the project
     * @return the evaluation widget
     */
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new EvalutionStatusBarWidget(project);
    }

    /**
     * Disposes the evaluation widget
     *
     * @param widget the evaluation widget
     */
    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        StatusBarWidgetFactory.super.disposeWidget(widget);
    }

    /**
     * Returns if the evaluation widget can be enabled on the status bar
     *
     * @param statusBar the status bar
     * @return if the evaluation widget can be enabled on the status bar
     */
    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
