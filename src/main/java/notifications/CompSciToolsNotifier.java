package notifications;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settings.SettingsConfigurable;
import ui.icons.CompSciToolsIcons;

import javax.swing.*;
import java.io.File;

import static module.CompSciToolsModuleBuilder.SPECIAL_FILE_NAME;
import static module.CompSciToolsModuleBuilder.createSpecialFile;

public abstract class CompSciToolsNotifier {

    /**
     * Create a notification with the given parameters.
     * Instead of using this method directly, use {@code notifyError}, {@code notifyWarning} or {@code notifyInfo} to assure a better readability in the code.
     *
     * @param project The current project.
     * @param title   The title of the notification.
     * @param body    The body of the notification.
     * @param type    The type of the notification. See {@code NotificationType}.
     * @param icon    The icon you want to display on the notification.
     * @param actions The actions offered by the notification (open settings for example).
     */
    private static void notify(@Nullable Project project, @Nullable String title, @Nullable String body, NotificationType type, @Nullable Icon icon, @Nullable NotificationAction[] actions) {
        new Thread(() -> {
            Notification notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("CompSci Tools Notification Group")
                    .createNotification("<html>" + ((title == null) ? "" : title.replace("\n", "<br>")),
                            (body == null) ? "" : body.replace("\n", "<br>") + "</html>",
                            type);
            if (actions != null) {
                for (AnAction action : actions) {
                    if (action != null)
                        notification.addAction(action);
                }
            }
            if (icon != null)
                notification.setIcon(icon);
            notification.notify(project);
        }).start();
    }

    /* -- Error notification -- */

    /**
     * Create an error notification.
     *
     * @param project The current project.
     * @param title   Title of the notification.
     * @param body    Body of the notification.
     */
    public static void notifyError(@Nullable Project project, @Nullable String title, @Nullable String body) {
        notify(project, title, body, NotificationType.ERROR, null, null);
    }

    /**
     * Create an error notification.
     *
     * @param project The current project.
     * @param title   Title of the notification.
     * @param body    Body of the notification.
     * @param actions List of offered actions.
     */
    public static void notifyError(@Nullable Project project, @Nullable String title, @Nullable String body, NotificationAction[] actions) {
        notify(project, title, body, NotificationType.ERROR, null, actions);
    }

    /* -- Warning notification -- */

    /**
     * Create a warning notification.
     *
     * @param project The current project.
     * @param title   The title of the notification.
     * @param body    The body of the notification.
     */
    public static void notifyWarning(@Nullable Project project, @Nullable String title, @Nullable String body) {
        notify(project, title, body, NotificationType.WARNING, null, null);
    }

    /**
     * Create a warning notification.
     *
     * @param project The current project.
     * @param title   The title of the notification.
     * @param body    The body of the notification.
     * @param actions The actions offered by the notification (open settings for example).
     */
    public static void notifyWarning(@Nullable Project project, @Nullable String title, @Nullable String body, @Nullable NotificationAction[] actions) {
        notify(project, title, body, NotificationType.WARNING, null, actions);
    }

    /* -- Information Notifications -- */

    /**
     * Create an information notification.
     *
     * @param project The current project.
     * @param title   The title of the notification.
     * @param body    The body of the notification.
     * @param icon    The icon you want to display on the notification.
     */
    public static void notifyInfo(@Nullable Project project, @Nullable String title, @Nullable String body, Icon icon) {
        notify(project, title, body, NotificationType.INFORMATION, icon, null);
    }

    /**
     * Notify that an error connection happened.
     *
     * @param project Current project.
     */
    public static void notifyConnectionError(@Nullable Project project, boolean error) {
        NotificationAction[] actions;
        if (project != null) {
            actions = new NotificationAction[]{
                    new NotificationAction("Open " + SPECIAL_FILE_NAME) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            File specialFile = new File(project.getBasePath() + File.separator + SPECIAL_FILE_NAME);
                            if (!specialFile.exists())
                                createSpecialFile(project);
                            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(specialFile.getPath());
                            if (vFile != null) {
                                fileEditorManager.openFile(vFile, true);
                            }
                        }
                    },
                    new NotificationAction("Open CompSci Tools settings") {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable.class);
                        }
                    }};
        } else {
            actions = new NotificationAction[0];
        }
        notify(project, "Connection With Moodle Web Service Failed.",
                "Please make sure you have:\n" +
                        " - a working internet connection\n" +
                        " - the file " + SPECIAL_FILE_NAME + " in the project's directory\n" +
                        " - a correct ID of the VPL exercise\n" +
                        " - a valid user token\n" +
                        " - a good URL to the web service",
                error ? NotificationType.ERROR : NotificationType.WARNING, error ? null : CompSciToolsIcons.CompSciTools, actions);
    }
}
