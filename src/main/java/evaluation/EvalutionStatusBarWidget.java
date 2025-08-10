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
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The EvaluationStatusBarWidget class represents a status bar widget that displays the number of evaluations.
 * It implements the StatusBarWidget interface.
 *
 * @see StatusBarWidget
 */
public class EvalutionStatusBarWidget implements StatusBarWidget {

    /**
     * Used to retrieve the project status
     */
    private final Project project;

    /**
     * Timer for refreshing the window
     */
    private Timer timer;

    /**
     * Constructor of the EvaluationStatusBarWidget class
     *
     * @param project the project
     */
    public EvalutionStatusBarWidget(Project project) {
        this.project = project;
        this.timer = null;
    }

    /**
     * Returns the evaluation widget ID
     *
     * @return the evaluation widget ID
     */
    @NotNull
    @Override
    public String ID() {
        return "CompSciTools-Evaluation";
    }

    /**
     * Returns the evaluation widget presentation
     *
     * @return the evaluation widget presentation
     */
    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return new EvaluationWidgetPresentation(this.project);
    }

    /**
     * Installs the evaluation widget
     *
     * @param statusBar the status bar
     * @see StatusBar
     */
    @Override
    public void install(@NotNull StatusBar statusBar) {
        // We make a timer for update the presentation every second
        // it will call the getSelectedValue method of the EvaluationWidgetPresentation class
        this.timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                statusBar.updateWidget("CompSciTools-Evaluation");
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
        StatusBarWidget.super.dispose();
    }
}
