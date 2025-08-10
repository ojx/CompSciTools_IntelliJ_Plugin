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

package clock;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;


/**
 * The ClockStatusBarWidget class represents a status bar widget that displays the time.
 * It implements the StatusBarWidget interface.
 *
 * @author GEILLER Valentin & GUEZI Yanis
 * @see StatusBarWidget
 */
public class ClockStatusBarWidget implements StatusBarWidget {

    /**
     * Used to retrieve the project status
     */
    private final Project project;

    /**
     * Timer for refreshing the window
     */
    private Timer timer;

    /**
     * The related presentation
     */
    private ClockWidgetPresentation presentation;

    /**
     * Constructor of the ClockStatusBarWidget class
     *
     * @param project the project
     */
    public ClockStatusBarWidget(Project project) {
        this.project = project;
        this.timer = null;
        this.presentation = null;
    }

    /**
     * Returns the clock widget ID
     *
     * @return the clock widget ID
     */
    @NotNull
    @Override
    public String ID() {
        return "Caseine-Clock";
    }

    /**
     * Returns the clock widget presentation
     *
     * @return the clock widget presentation
     */
    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        //return StatusBarWidget.super.getPresentation();
        if (this.presentation == null) {
            this.presentation = new ClockWidgetPresentation(this.project);
        }
        return this.presentation;
    }

    /**
     * Installs the clock widget
     *
     * @param statusBar the status bar
     * @see StatusBar
     */
    @Override
    public void install(@NotNull StatusBar statusBar) {
        // We make a timer for update the presentation every second
        // it will call the getSelectedValue method of the ClockWidgetPresentation class
        this.timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                statusBar.updateWidget("Caseine-Clock");
            }
        }, 0, 1000);
    }

    /**
     * Dispose the widget
     */
    @Override
    public void dispose() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        if (this.presentation != null) {
            this.presentation.dispose();
            this.presentation = null;
        }
        StatusBarWidget.super.dispose();
    }
}
