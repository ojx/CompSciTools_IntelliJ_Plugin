/*
 CompSci Tools Plugin for IntelliJ is free software: you can redistribute it and/or modify
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

package service;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import module.CompSciToolsModuleBuilder;
import vplwsclient.FileUtils;
import vplwsclient.RestJsonMoodleClient;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.VplFile;
import vplwsclient.exception.MoodleWebServiceException;
import vplwsclient.exception.VplConnectionException;

import javax.json.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to bridge between the plugin and the RestJsonMoodleClient.
 * It allows the plugin to get the name, description and other things from the JSON file much easier.
 *
 * @author Joshua Monteiller
 */
public class ServiceGetter {

    private RestJsonMoodleClient RJMC;
    private JsonObject jsonInfo;
    private boolean fail;

    private String vplID;
    private String token;
    private String url;
    private final String path;

    /**
     * Construct a new ServiceGetter with the variable stored in the Storage
     *
     * @param path the path of the file which contains the VPL ID
     */
    public ServiceGetter(String path) {
        this.path = path;

        PersistentStorage state = PersistentStorage.getInstance();
        boolean existVpl = new File(path + CompSciToolsModuleBuilder.SPECIAL_FILE_NAME).exists();
        if (existVpl) {
            vplID = state.getProjectVplID(path);
        } else {
            vplID = state.vplID;
        }
        token = state.userToken;
        url = state.csToolsURL;

        update();
    }

    /**
     * Returns the client used in the class to be used manually by other methods
     *
     * @return RJMC the RestJsonMoodleClient used in the ServiceGetter class
     */
    public RestJsonMoodleClient getRJMC() {
        updateIfModified();
        return RJMC;
    }

    /**
     * This method calls the API to have the name of the exercise.
     *
     * @return the String which represents the name of the exercise
     */
    public String getExerciseName() {
        updateIfModified();
        return fail ? "" : jsonInfo.getString("name");
    }

    /**
     * This method calls the API to have the description of the exercise.
     *
     * @return the String which represents the description of the exercise
     */
    public String getDescription() {
        updateIfModified();
        if (fail) {
            return "<html>\n<head><style>p {font-family: sans-serif; font-size: small} body {padding: 3px 11px 2px 11px}</style></head><body><p>Security token has expired.</p><p>Please reconnect with the webservices URL.</p></body></html>";
        }

        String hexCode = EditorColorsManager.getInstance().isDarkEditor() ? "#444444" : "#F4F4F4";

        String intro = jsonInfo.getString("intro");
        return "<html>\n<head><style>pre {border: solid 1px #aaaaaa; border-radius: 5px; overflow-wrap: break-word; word-wrap: break-word; padding: 2px 5px; background-color: " + hexCode + "; white-space: pre-wrap;font-size: x-small;} body {padding: 3px 11px 2px 11px; font-family: sans-serif; font-size: small} div, p {font-family: sans-serif; font-size: small} </style></head>\n<body>\n" + intro + "\n</body>\n</html>";
    }

    /**
     * This method calls the API to retrieve all the initial files of the exercise.
     *
     * @return the array of the files needed for the exercise
     */
    public VplFile[] getReqFiles() {
        updateIfModified();
        return RestJsonMoodleClient.extractFiles(jsonInfo, "reqfiles");
    }

    /**
     * This method calls the API to retrieve all the files of the user for the exercise.
     *
     * @return the array of the files needed for the exercise
     */
    public VplFile[] getFiles() {
        updateIfModified();
        try {
            return RestJsonMoodleClient.extractFiles(RJMC.callService(VPLService.VPL_OPEN), "files");
        } catch (MoodleWebServiceException | VplConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a boolean which represents if the connection with the service has failed or not
     *
     * @return a boolean, true the connection has failed somehow, false everything is okay
     */
    public boolean hasFailed() {
        return fail;
    }

    /**
     * Verifies that the ID of the exercise, the user token and the url haven't changed.
     * If they have, reinitialize the variables, make a new RestJsonMoodleClient from the new variables and try to retrieve a new 'info' JSON file.
     */
    public void updateIfModified() {
        PersistentStorage state = PersistentStorage.getInstance();
        String vplID = state.getProjectVplID(path);
        String token = state.userToken;
        String url = state.csToolsURL;
        if (updateInputs(vplID, token, url)) {
            update();
        }
    }

    public void update() {
        RJMC = WebserviceClientFactory.createFromCustomProperties(vplID, token, url);
        try {
            jsonInfo = RJMC.callService(VPLService.VPL_INFO);
            fail = false;
        } catch (MoodleWebServiceException | VplConnectionException e) {
            fail = true;
        }
    }

    protected boolean updateInputs(String vplID, String token, String url) {
        boolean changed = false;
        if (!this.vplID.equals(vplID)) {
            this.vplID = vplID;
            changed = true;
        }
        if (!this.token.equals(token)) {
            this.token = token;
            changed = true;
        }
        if (!this.url.equals(url)) {
            this.url = url;
            changed = true;
        }
        return changed;
    }

    /**
     * Return if the path to the special file have changed
     *
     * @param newPath the "new" path of the special file
     * @return a boolean, if the path have changed, return true, if not return false
     */
    public boolean isPathModified(String newPath) {
        return !newPath.equals(path);
    }


    /**
     * Pull all the initial files or all the previously pushed files of the exercise.
     * It depends on the JsonArray given in argument.
     *
     * @param files the VplFile array, if it comes from {@link #getReqFiles()}, it represents all the initial files and if it comes from {@link #getFiles()}, it represents all the previously pushed files
     * @throws IOException If an error occurred during the interaction with local files.
     */
    public static void writeFilesToDisk(VplFile[] files, String path) throws IOException {
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        List<String> excludedFiles = FileUtils.scanExcludedList(path);
        excludedFiles.add(CompSciToolsModuleBuilder.SPECIAL_FILE_NAME);
     /*   excludedFiles.add(".idea/modules.xml");
        excludedFiles.add(".idea/*.xml");
        excludedFiles.add(".idea/");*/

     //   System.out.println("Excluded files: " + excludedFiles);

        // First delete all files
        for (File file : Objects.requireNonNull(new File(path).listFiles())) {
            if (!FileUtils.isExcluded(file.getName(), excludedFiles)) {
          //      System.out.println("Deleting file: " + file.getName() + "(" + file.getAbsolutePath() + ")");
                FileUtils.deleteIncludedFolder(file, path, excludedFiles);
            }
        }

        File dir = new File(path +  File.separator + CompSciToolsModuleBuilder.SOURCE_DIRECTORY);
        if (!dir.exists()) dir.mkdirs();


        // Then recover from remote
        for (VplFile vplFile : files) {
            // Do not import files registered in .vplignore
            if (FileUtils.isExcluded(vplFile.getFullName(), excludedFiles))
                continue;

            //Write the file in the folder specified by the path
            String fileName = vplFile.getFullName();
            File file = new File(path + CompSciToolsModuleBuilder.SOURCE_DIRECTORY + File.separator + fileName);

            //Retrieve the name of the directory of the file
            int index = fileName.lastIndexOf('/');
            String dirName = fileName.substring(0, index + 1);
            File directory = new File(path + dirName);

            //If the directories do not exist, we create it
            if (!directory.exists()) directory.mkdirs();

            FileOutputStream fos = new FileOutputStream(file);
            vplFile.write(fos);

            //Refresh the file's content
            VirtualFile virtualFile = fileSystem.findFileByIoFile(file);
            if (virtualFile != null) {
                virtualFile.refresh(false, false);
            }
        }

        //Refresh the project's root
        VirtualFile virtualFile = fileSystem.findFileByIoFile(new File(path));
        if (virtualFile != null) {
            virtualFile.refresh(false, false);
        }
    }
}
