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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import module.CompSciToolsModuleBuilder;
import notifications.CompSciToolsNotifier;
import org.jetbrains.annotations.NotNull;
import service.ServiceGetter;
import sideWindow.CompSciToolsSideWindow;
import sideWindow.CompSciToolsSideWindowFactory;
import ui.icons.CompSciToolsIcons;
import vplwsclient.FileUtils;
import vplwsclient.RestJsonMoodleClient;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.VplFile;
import vplwsclient.exception.*;

import javax.json.JsonObject;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static module.CompSciToolsModuleBuilder.createVplignoreFile;
import static service.ServiceGetter.writeFilesToDisk;

/**
 * Exception used to exit control flow but not supposed to be handled (usually thrown after handling an exception prematurely).
 */
class AlreadyTreatedException extends Exception {}

/**
 * This class served to add actions to different buttons who appeared on the top of the side window.
 * The position of these buttons is defined in the CompSciToolsActionGroup class.
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsAction extends AnAction {

    private ServiceGetter servGet;

    private Project currentProject;

    private String basePath;

    /**
     * Perform an action when any button is used.
     * This method is called when push, pull, reset or evaluate action
     * is triggered by the user.
     *
     * @param event AnActionEvent, which served to retrieve the project
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        currentProject = event.getProject(); //Retrieve the project from where the button was pressed
        String command = event.getPresentation().getText(); //The command represents the pressed button
        basePath = currentProject.getBasePath() + File.separator; //The path of where to found the project and the files associated with

        //If the ServiceGetter is not initialized, initialize it or if the path to the special file have changed, recreate the ServiceGetter
        if (servGet == null || servGet.isPathModified(basePath) || command.equals("Reset")) {
            servGet = new ServiceGetter(basePath);
        }
        
        servGet.updateIfModified(); // Make sure everything is correct and has not been changed

        FileDocumentManager.getInstance().saveAllDocuments();

        if (servGet.hasFailed()) {
            //If the connection has failed, show an error message to the user
            CompSciToolsNotifier.notifyConnectionError(currentProject, true);
            return;
        }

        // Successful Connection
        try {
            switch (command) {
                case "Reset":
                    if (pull(true)) {
                        CompSciToolsNotifier.notifyInfo(currentProject, "CompSci Tools Project successfully reset", "", CompSciToolsIcons.Reset); // Notification should depend on the context instead of the action performed so that it can be called from different context.
                    }
                    break;
                case "Pull":
                    if (pull(false)) {
                        CompSciToolsNotifier.notifyInfo(currentProject, "CompSci Tools Project successfully imported", "", CompSciToolsIcons.Pull); // Notification should depend on the context instead of the action performed so that it can be called from different context.
                    }
                    break;
                case "Push":
                    push();
                    CompSciToolsNotifier.notifyInfo(currentProject, "CompSci Tools Project successfully exported", "", CompSciToolsIcons.Push); // Notification should depend on the context instead of the action performed so that it can be called from different context.
                    break;
                case "Evaluate":
                    // Notify that the evaluation has started.
                    CompSciToolsNotifier.notifyInfo(currentProject, "Evaluation is in progress...", "", CompSciToolsIcons.Evaluate); // Notification should depend on the context instead of the action performed so that it can be called from different context.
                    // Evaluation is a long action, it will run as a separate Thread that will inform the user about its progress.
                    launchEvaluation();
                    break;
                default:
                    CompSciToolsNotifier.notifyError(currentProject, "ERROR : Internal code error", "Selected button doesn't exist or its name is misspelled in the code");
            }
        } catch (IOException | VplException e) {
            handleActionException(e, command);
        }
    }

    /**
     * Handle the exceptions caused by actions (except a new project).
     * !! Make sure that this function is called at the end of the action handling !!
     * Otherwise, you may end up notifying the user some time for the same problem.
     *
     * @param e       Raised exception to handle. It can be of type {@link NoSuchFileException}, {@link MaxFilesException}, {@link IOException}, {@link VplException}, {@link InterruptedException} or {@link AlreadyTreatedException}.
     * @param command A string indicating the handled action that failed.
     */
    private void handleActionException(Exception e, String command) {
        if (e instanceof RequiredFileNotFoundException) {
            CompSciToolsNotifier.notifyError(currentProject, "Required Files are missing.", e.getMessage());
            return;
        }
        if (e instanceof NoSuchFileException) {
            CompSciToolsNotifier.notifyError(currentProject, "ERROR : file not found", "The files you want to push or evaluate are not in the project/have been deleted");
            return;
        }
        if (e instanceof MaxFilesException) {
            CompSciToolsNotifier.notifyError(currentProject, "Maximum Number Of Files Exceeded", e.getMessage(),
                    new NotificationAction[]{
                            new NotificationAction("Open .vplignore") {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                    createVplignoreFile(currentProject);
                                    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(currentProject.getBasePath() + File.separator + ".vplignore");
                                    if (vFile != null) {
                                        FileEditorManager.getInstance(currentProject).openFile(vFile, true);
                                    }
                                }
                            }
                    });
            return;
        }
        if (e instanceof IOException || e instanceof VplException || e instanceof InterruptedException) {
            CompSciToolsNotifier.notifyError(currentProject, "Error During Tool Action : " + command, "IOException, VplException or InterruptedException occurred\n" + e.getMessage());
            return;
        }
        if (e instanceof AlreadyTreatedException) {
            // The problem has already been dealt with. Just do nothing.
            return;
        }
        // If this line is reach, there is an error in the plugin implementation.
        throw new RuntimeException("Internal error. Please report this to devs@compsci.tools", e);
    }

    /**
     * Pull current project from the vpl.
     *
     * @param reset {@code true} If this method is called by a reset action and project should be pull from initial project state. Else, the project will be pulled from the last saved (pushed/evaluated) version of your project.
     * @return {@code true} if the pull was performed, {@code false} if it was cancelled by the user.
     * @throws MoodleWebServiceException In case an error happened in the moodle client.
     * @throws VplConnectionException    In case of a connexion issue.
     * @throws IOException               In case of an error with the local file system.
     */
    private boolean pull(boolean reset) throws MoodleWebServiceException, VplConnectionException, IOException {
        String warningMsg = "Warning! You are about to overwrite your project for the exercise: " + servGet.getExerciseName() + ".\nAre you sure you want to continue ?";
        //Ask the user if he really wants to erase the actual file
        if (Messages.showOkCancelDialog(currentProject, warningMsg, "Confirmation", "OK", "Cancel", Messages.getQuestionIcon()) == Messages.OK) {
            // In case of a reset, create .vplignore file
          //  System.out.println("reset: " + reset);
            if (reset) {
                new File(currentProject.getBasePath() + File.separator + ".vplignore").delete(); // Delete vplignore to assure it refresh
                CompSciToolsModuleBuilder.createVplignoreFile(currentProject);
            }
            //Proceed to reset the files
            writeFilesToDisk(reset ? servGet.getReqFiles() : servGet.getFiles(), basePath);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Call the client to push all the files of the project to save them on the platform.
     *
     * @throws VplException En error occurred within the interaction with the web service
     * @throws IOException  An error occurred within the interaction with local file system
     */
    public void push() throws VplException, IOException {
        List<VplFile> listVFile = new ArrayList<>();
        String path = currentProject.getBasePath() + File.separator + CompSciToolsModuleBuilder.SOURCE_DIRECTORY;
        List<String> excludedFiles = FileUtils.scanExcludedList(path);
        //Browse all the files and directory of the project
        List<File> listFiles = FileUtils.listIncludedFiles(path, path, excludedFiles);
        for (File file : listFiles) {
            String standardFilePath = file.getPath()
                    .replace(File.separator, "/");
            VplFile VFile = new VplFile(file, standardFilePath.replace(path + "/", ""));
            listVFile.add(VFile);
        }
        //Call the service to save all the file in the list given in arguments
        servGet.getRJMC().callServiceWithFiles(VPLService.VPL_SAVE, listVFile);
    }

    /**
     * Call the client to push all the files needed for the exercise, evaluate them and return the results to the user.
     * This action takes quite some time so it is ran in a separate thread and warn the user about itself and handle its own exceptions.
     */
    public void launchEvaluation() {

        new Thread(() -> {
         //   System.out.println("bp: "+ (basePath+ CompSciToolsModuleBuilder.SOURCE_DIRECTORY));
            CompSciToolsSideWindow csw = CompSciToolsSideWindowFactory.getCsw(basePath);  // leave as just basePath
            try {
                // Avert the user that the evaluation is running
                csw.setSelectedTab();
                csw.setResultsTree("", "");
                csw.setResultGrade("Evaluation is running...", true);

                //We need to push the file before the evaluation
               // System.out.println("Pushing!");
                push();
               // System.out.println("Pushed!");

           //     PersistentStorage state = PersistentStorage.getInstance();
                RestJsonMoodleClient RJMC = servGet.getRJMC();
                JsonObject jsonFile = compatibilityWait(RJMC);

               // System.out.println("Result: "+jsonFile.toString());

                //Set the grade and the result tree in the side window
                csw.setSelectedTab();

                if (!jsonFile.getString("compilation").trim().isEmpty()) { // Errors
                    csw.showErrors(jsonFile.getString("compilation").trim());
                } else {
                    csw.setResultsTree(jsonFile.getString("evaluation"), (new SimpleDateFormat()).format(new Date()));
                    csw.setResultGrade(jsonFile.getString("grade"));
                    ApplicationManager.getApplication().invokeLater(() -> csw.setNodeExpanded(csw.resultsTree, (DefaultMutableTreeNode) csw.resultsTree.getModel().getRoot()));
                }

                CompSciToolsNotifier.notifyInfo(currentProject, "Evaluation finished", "", CompSciToolsIcons.Evaluate);
            } catch (InterruptedException /*| AlreadyTreatedException*/ | VplException | IOException e) {
                //System.out.println("Foook!");
            //    System.out.println(e.getMessage());
                handleActionException(e, "Evaluate");
                csw.initResults(currentProject.getBasePath() + "/");
            }
        }).start();
    }

    /**
     * Wait for an evaluation result with repeatedly calling webservice.
     *
     * @param RJMC Moodle client uto make remote calls to Moodle server.
     * @return A {@code JsonObject} containing the results of the evaluation.
     * @throws VplConnectionException    En error occurred within the interaction with the web service
     * @throws MoodleWebServiceException In case, an error happened in the moodle client.
     * @throws InterruptedException      Another Thread interrupted the wait and query process.
     */
    @NotNull
    private JsonObject compatibilityWait(RestJsonMoodleClient RJMC) throws VplConnectionException, MoodleWebServiceException, InterruptedException {
        JsonObject jsonFile = null;
       // System.out.println("Free evals: " + EvaluationWidgetPresentation.getFreeEvaluations());
        final Object lock = new Object(); //The lock needed to wait
        int waitTime = 2000; //The number of milliseconds needed to wait
        int lockTry = 0; //The number of tries to succeed in a good response of the service
        int maxTry = 7; //The maximum number of allowed tries
        while (lockTry < maxTry) {
           // System.out.println("Calling EVALUATE");
            RJMC.callService(VPLService.VPL_EVALUATE);
            //System.out.println("resp: " + jsonTest);
            //We need to wait for the evaluation to finish
            synchronized (lock) {
                lock.wait(waitTime);
            }
            //Retrieve the results of the evaluation
           // System.out.println("Calling GET_RESULT");

            jsonFile = RJMC.callService(VPLService.VPL_GET_RESULT);
         //   jsonFile = RJMC.callService(VPLService.VPL_GET_LAST_EVALUATION);
         //   System.out.println("Resp: "+jsonFile.toString());

            //If the return object is a JSON with nothing in the evaluation or the grade, we try the process one more time and wait one more second
            if (!jsonFile.containsKey("compilation") || jsonFile.getString("compilation").trim().equals("The compilation process did not generate an executable nor error message.")) {
            //    System.out.println("Try " + ++lockTry);
                if (lockTry == maxTry) {
                    CompSciToolsNotifier.notifyWarning(currentProject, "The Evaluation Failed", "Too many tries to let the API evaluate the exercise");
                }
                waitTime += 500; //We wait a little more for the next time
            } else { //If not, everything is okay! We can exit the while loop
                lockTry = maxTry;
            }
        }
        return jsonFile;
    }
}