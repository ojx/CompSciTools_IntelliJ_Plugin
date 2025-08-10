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
import com.intellij.openapi.wm.StatusBarWidget;
import module.CompSciToolsModuleBuilder;
import org.jetbrains.annotations.Nullable;
import service.ServiceGetter;
import ui.icons.CompSciToolsIcons;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.exception.MoodleWebServiceException;
import vplwsclient.exception.VplConnectionException;

import javax.json.JsonNumber;
import javax.json.JsonValue;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The ClockWidgetPresentation class represents a presentation for the clock widget.
 * It implements the StatusBarWidget.MultipleTextValuesPresentation interface.
 *
 * @author GEILLER Valentin & GUEZI Yanis
 * @see StatusBarWidget.MultipleTextValuesPresentation
 */
public class ClockWidgetPresentation implements StatusBarWidget.MultipleTextValuesPresentation {

    private long timeEnd;
    private final Project project;
    private Timer timer;

    /**
     * Constructor of the ClockWidgetPresentation class
     * It will call the api every 5 minutes.
     *
     * @param project the project
     */
    public ClockWidgetPresentation(Project project) {
        this.project = project;
        this.timeEnd = -1;

        if (!CompSciToolsModuleBuilder.isCompSciToolsProject(project)) {
            this.timer = null;
            return;
        }

        this.timer = new Timer();

        // Call api every 5 minutes.
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                call();
            }
        }, 0, 5 * 60 * 1000);
    }

    /**
     * Returns the text to display
     *
     * @return the text to display
     */
    @Nullable
    @Override
    public String getSelectedValue() {
        if (!CompSciToolsModuleBuilder.isCompSciToolsProject(project))
            return "";
        if (timeEnd == -1) {
            return "There is no time limitation with this exercise";
        }
        long remainingTime = timeEnd - System.currentTimeMillis();
        if (remainingTime <= 0) {
            return "Time is up!";
        } else {
            return "Time left: " + getFormattedTime(remainingTime / 1000);
        }
    }

    /**
     * Returns the text to display in the tooltip
     *
     * @return the text to display in the tooltip
     */
    @Nullable
    @Override
    public String getTooltipText() {
        return this.getSelectedValue();
    }

    /**
     * Returns the icon to display
     *
     * @return the icon to display
     */
    @Nullable
    @Override
    public Icon getIcon() {
        if (timeEnd == -1) {
            return null;
        }
        if (timeEnd > 600 * 1000) {
            return CompSciToolsIcons.GreenTimer;
        }
        if (timeEnd > 60 * 1000) {
            return CompSciToolsIcons.OrangeTimer;
        }
        return CompSciToolsIcons.RedTimer;
    }

    /**
     * Returns the text to display in the status bar for the given time
     * Example : 01:02:03
     *
     * @param time the time
     * @return the text to display in the status bar
     */
    private String getFormattedTime(long time) {
        long hour = time / 3600;
        long minute = (time % 3600) / 60;
        long second = time % 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    /**
     * Calls the api to get the time left
     *
     * @return the time left in seconds or null if there is no time limitation
     */
    private void call() {
        try {
            ServiceGetter serv = new ServiceGetter(project.getBasePath() + "/");
            JsonValue value = serv.getRJMC().callService(VPLService.VPL_GET_SUBRESTRICTIONS).get("timeleft");
            if (value.getValueType() == JsonValue.ValueType.NUMBER) {
                JsonNumber jsonNumber = (JsonNumber) value;
                timeEnd = System.currentTimeMillis() + ((jsonNumber.longValueExact() - 1) * 1000);
            } else {
                timeEnd = -1;
            }
        } catch (VplConnectionException | MoodleWebServiceException e) {
            timeEnd = -1;
        }
    }

    public void dispose() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }
}
