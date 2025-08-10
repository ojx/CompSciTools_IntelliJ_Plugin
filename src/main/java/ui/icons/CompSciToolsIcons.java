/*
 * Copyright (C) 2022 Joshua Monteiller, Astor Bizard, Christophe Saint-Marcel
 *
 * CompSci Tools Plugin for IntelliJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CCompSci Tools Plugin for IntelliJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ui.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * This class stocks all the icons needed for the plugin
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsIcons {
    public static final Icon CompSciTools = IconLoader.getIcon("/ui/icons/cstools.png", CompSciToolsIcons.class);
    public static final Icon Pull = IconLoader.getIcon("/ui/icons/pull.png", CompSciToolsIcons.class);
    public static final Icon Push = IconLoader.getIcon("/ui/icons/push.png", CompSciToolsIcons.class);
    public static final Icon Reset = IconLoader.getIcon("/ui/icons/reset.png", CompSciToolsIcons.class);
    public static final Icon Evaluate = IconLoader.getIcon("/ui/icons/evaluate.png", CompSciToolsIcons.class);

    public static final Icon CaseError = IconLoader.getIcon("/ui/icons/JUnit_CaseError.png", CompSciToolsIcons.class);
    public static final Icon CaseFail = IconLoader.getIcon("/ui/icons/JUnit_CaseFail.png", CompSciToolsIcons.class);
    public static final Icon CaseOk = IconLoader.getIcon("/ui/icons/JUnit_CaseOk.png", CompSciToolsIcons.class);
    public static final Icon SuiteError = IconLoader.getIcon("/ui/icons/JUnit_SuiteError.png", CompSciToolsIcons.class);
    public static final Icon SuiteFail = IconLoader.getIcon("/ui/icons/JUnit_SuiteFail.png", CompSciToolsIcons.class);
    public static final Icon SuiteOk = IconLoader.getIcon("/ui/icons/JUnit_SuiteOk.png", CompSciToolsIcons.class);
    public static final Icon RedTimer = IconLoader.getIcon("/ui/icons/red_timer.svg", CompSciToolsIcons.class);
    public static final Icon GreenTimer = IconLoader.getIcon("/ui/icons/green_timer.svg", CompSciToolsIcons.class);
    public static final Icon OrangeTimer = IconLoader.getIcon("/ui/icons/orange_timer.svg", CompSciToolsIcons.class);
    public static final Icon Eval = IconLoader.getIcon("/ui/icons/eval.svg", CompSciToolsIcons.class);
}
