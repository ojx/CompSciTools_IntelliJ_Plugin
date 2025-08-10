/*
 * Copyright (C) 2022 Joshua Monteiller, Astor Bizard, Christophe Saint-Marcel
 *
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

// Copyright 2000-2025 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package module;

import org.jetbrains.annotations.NotNull;
import ui.icons.CompSciToolsIcons;

import javax.swing.Icon;

/**
 * This class is used to define the type of the project who will be created in the "New Project" wizard
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsModuleType extends com.intellij.openapi.module.ModuleType<CompSciToolsModuleBuilder> {
    /**
     * An important ID which is used in the plugin.xml file to reference this class
     */
    private static final String ID = "COMPSCI_TOOL_TYPE";

    public CompSciToolsModuleType() {
        super(ID);
    }

    @NotNull
    @Override
    public CompSciToolsModuleBuilder createModuleBuilder() {
        return new CompSciToolsModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "CompSci Tools Project";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Connect to and edit a CompSci Tools programming lab assignment";
    }

    @NotNull
    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
        return CompSciToolsIcons.CompSciTools;
    }

    /**
     * Return true if the given project is a vpl one.
     * This is detected with the presence of the special file (containing vplID) at the root of the project.
     *
     * @param project current project.
     * @return {@code true} if the project is a CompSci Tools project, {@code false} otherwise.

    public static boolean isCompSciToolsProject(@NotNull Project project) {
        File specialFile = new File(project.getBasePath() + File.separator + SPECIAL_FILE_NAME);
        if (specialFile.exists()) {
            // If there is no .vplignore file, warn the user about potential threat.
            if (!alreadyWarned && !new File(project.getBasePath() + File.separator + ".vplignore").exists()) {
                alreadyWarned = true;
                CompSciToolsNotifier.notifyWarning(project, "No .vplignore file found", "Pull and reset actions may delete files that are not stored on the vpl.",
                        new NotificationAction[]{
                                new NotificationAction("Create .vplignore") {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                        createVplignoreFile(project);
                                    }
                                }
                        });
            }
            return true;
        }
        return false;
    }

    private static boolean alreadyWarned = false;
     */
}
