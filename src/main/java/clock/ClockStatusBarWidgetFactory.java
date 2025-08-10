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

package clock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * The ClockStatusBarWidgetFactory class represents a factory for creating instances of the ClockStatusBarWidget.
 * It implements the StatusBarWidgetFactory interface.
 *
 * @author GEILLER Valentin & GUEZI Yanis
 * @see StatusBarWidgetFactory
 */
public class ClockStatusBarWidgetFactory implements StatusBarWidgetFactory {

    /**
     * Returns the clock widget ID
     *
     * @return the clock widget ID
     */
    @Override
    public @NotNull @NonNls String getId() {
        return "Caseine-Clock";
    }

    /**
     * Returns the clock widget display name
     *
     * @return the clock widget display name
     */
    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Caseine Clock";
    }


    /**
     * Creates a clock widget
     *
     * @param project the project
     * @return the clock widget
     */
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new ClockStatusBarWidget(project);
    }

}
