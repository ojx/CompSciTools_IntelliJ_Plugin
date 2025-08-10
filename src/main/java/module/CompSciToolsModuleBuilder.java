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

/* Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * Some functions are under this different licence. Here the list: getModuleType() & getCustomOptionsStep()
 */

package module;

import com.intellij.ide.util.projectWizard.ModuleNameLocationSettings;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.SdkDetector;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import notifications.CompSciToolsNotifier;
import org.jetbrains.annotations.NotNull;
import service.PersistentStorage;
import service.ServiceGetter;
import service.WebserviceClientFactory;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.exception.MoodleWebServiceException;
import vplwsclient.exception.VplConnectionException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static service.ServiceGetter.writeFilesToDisk;

/**
 * This class serves to build the project and the files it will contain after the user validate the new project.
 *
 * @author Joshua Monteiller
 */
public class CompSciToolsModuleBuilder extends com.intellij.ide.util.projectWizard.ModuleBuilder {

    public static final String SPECIAL_FILE_NAME = ".moodlevpl";
    public static final String SOURCE_DIRECTORY = "src";

    private static final String[] forbiddenCharacters = {"<", ">", ":", "\"", "/", "\\", "|", "?", "*"}; //The list of forbidden characters in the name of a project


    /**
     * Set the VPL project. Add the file needed for the exercise and the special file which stored the VPL ID.
     *
     * @param model
     */
    @Override
    public void setupRootModel(ModifiableRootModel model) {
        Project project = model.getProject();

        PersistentStorage state = PersistentStorage.getInstance();
        //The path to the special file
        String path = project.getBasePath() + File.separator;

        assignJdk(model);

       // System.out.println("path: " + path);
     //   System.out.println("File.separator: "+ File.separator);

        //Create the special file
        createSpecialFile(project);

        // Create .vplignore file
        createVplignoreFile(project);

        //"Download" the required files for the exercise
        ServiceGetter servGet = new ServiceGetter(path);
        if (!servGet.hasFailed()) {
            try {
                writeFilesToDisk(state.initCheckButtonSelected ? servGet.getReqFiles() : servGet.getFiles(), path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        // Setup module to allow its execution as a java program.
        VirtualFile baseDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        if (baseDir != null) {
            model.addContentEntry(baseDir).addSourceFolder(baseDir + File.separator + SOURCE_DIRECTORY, false);
        }
       // model.setSdk();
       // model.inheritSdk();


    }


    /**
     * Creates the special file containing the vpl id
     *
     * @param project The project.
     */
    public static void createSpecialFile(Project project) {
        PersistentStorage state = PersistentStorage.getInstance();
        File f = new File(project.getBasePath() + File.separator + SPECIAL_FILE_NAME);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(state.vplID.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a .vplignore file at the root of the project.
     *
     * @param project The project.
     */
    public static void createVplignoreFile(Project project) {
        String vplIgnorePath = project.getBasePath() + File.separator + ".vplignore";
        if (new File(vplIgnorePath).exists()) {
            return;
        }
        try {
            InputStream templateStream = CompSciToolsModuleBuilder.class.getResourceAsStream("/vplignoreTemplate");
            if (templateStream != null) Files.copy(templateStream, Paths.get(vplIgnorePath));
        } catch (IOException e) {
            CompSciToolsNotifier.notifyError(project, "Failure While Creating .vplignore File.", "You can create one manually to solve this.");
        }
    }

    @Override
    public ModuleType<CompSciToolsModuleBuilder> getModuleType() {
        // return
        return new CompSciToolsModuleType();
    }


    /**
     * Retrieve the information given by the user and search the exercise name.
     * If found, replace the default name of the project by the name of the exercise
     */
    @Override
    public ModuleWizardStep modifySettingsStep(SettingsStep settingsStep) {
        ModuleNameLocationSettings moduleNameLocationSettings = settingsStep.getModuleNameLocationSettings();
        if (moduleNameLocationSettings != null) {
           // String path = moduleNameLocationSettings.getModuleContentRoot();
            String path = System.getProperty("user.home");
          //  System.out.println("p1:" + path);

            //Keep the path and erase from it the last folder that doesn't interest us
           // path = path.substring(0, path.lastIndexOf(File.separator) + 1);
           // System.out.println("p2:" + path);
            path += File.separator + "Documents" + File.separator + "CompSciTools";
          //  System.out.println("p3:" + path);

            File directory = new File(path);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            //Get the new name for the project
            String newName = createNameProject(path);
           // System.out.println(newName);
            path += File.separator + newName;

            if (newName.equals("")) {
                super.modifySettingsStep(settingsStep);
            }

            //Change the path and the name of the project
            moduleNameLocationSettings.setModuleContentRoot(path);
      //      System.out.println(moduleNameLocationSettings.getModuleContentRoot());

            //System.out.println("OJ=>"+Arrays.toString(SdkType.getAllTypes()));


            moduleNameLocationSettings.setModuleName(newName);
            settingsStep.getModuleNameLocationSettings().setModuleContentRoot(path);
            settingsStep.getModuleNameLocationSettings().setModuleName(newName);
        }

        return super.modifySettingsStep(settingsStep);
    }

    private void assignJdk(ModifiableRootModel model) {

        SdkDetector detector = new SdkDetector();

        ArrayList<Boolean> zulus = new ArrayList<>();
        ArrayList<String> jdkPaths = new ArrayList<>();
        ArrayList<Double> jdkVersions = new ArrayList<>();
        ArrayList<String> jdkNames = new ArrayList<>();
        detector.detectSdks(SdkType.findByName("JavaSDK"), new ProgressIndicatorBase(), (sdkType, jdkName, jdkPath) -> {
                /*System.out.println(sdkType);
                System.out.println(jdkName);
                System.out.println(jdkPath);*/
            String s = jdkName.trim();
            String versionNum = s.substring(s.lastIndexOf(" ")) + 1;
            double v = 0;
            if (versionNum.indexOf(".") > 0) {
                if (versionNum.indexOf(".") != versionNum.lastIndexOf(".")) {
                    v = Double.parseDouble(versionNum.substring(0,versionNum.lastIndexOf(".") ));
                } else {
                    v = Double.parseDouble(versionNum);
                }
            }
            jdkNames.add(jdkName);
            jdkVersions.add(v);
            jdkPaths.add(jdkPath);
            zulus.add(s.toLowerCase().contains("zulu"));
        });

        if (jdkPaths.size() > 0) {
            int best = -1;
            boolean zuluFound = false;

            for (int i = 0; i < jdkPaths.size(); i++) {
                double v = jdkVersions.get(i);
                if (v > 0) {
                    if (best < 0 || (zulus.get(i) && !zuluFound) || (v > jdkVersions.get(i) && ((!zulus.get(i) && !zuluFound) || (zulus.get(i) && zuluFound)))) {
                        best = i;
                        zuluFound = zuluFound || zulus.get(i);
                    }
                }
            }

         /*   System.out.println(jdkPaths.size());
            System.out.println("Best JDK Path: " + jdkPaths.get(best));
            System.out.println("Best JDK Name: " + jdkNames.get(best));*/
            //  ProjectJdkTable.getInstance().

            // Sdk jdk = JavaSdk.getInstance().createJdk("java 1.8", jdkHomeDirectory(), false);

            List<Sdk> SDKs = ProjectJdkTable.getInstance().getSdksOfType(Objects.requireNonNull(SdkType.findByName("JavaSDK")));
        //    System.out.println(SDKs.size());

            //does it contain the best JDK?
            boolean found = false;
            for (Sdk sdk : SDKs) {
                if (sdk.getName().equals(jdkNames.get(best))) {
                    found = true;
                   /* System.out.println("Found JDK: " + sdk.getName() );
                    System.out.println(sdk.getName());
                    System.out.println(sdk.getVersionString());
                    System.out.println(sdk.getHomeDirectory());*/

                    model.setSdk(sdk);
                    ProjectRootManager.getInstance(model.getProject()).setProjectSdk(sdk);
                    FileBasedIndex.getInstance().requestReindex(model.getProject().getBaseDir());
                  //  VirtualFileManager.getInstance().syncRefresh();
                    //or LocalFileSystem.getInstance().refreshAndFindFileByPath()
                    break;
                }
            }

            if (!found) {
                Sdk sdk = ProjectJdkTable.getInstance().createSdk(jdkNames.get(best), Objects.requireNonNull(SdkType.findByName("JavaSDK")));
              //  System.out.println("Adding JDK: " + sdk.getName() );
                ProjectJdkTable.getInstance().addJdk(sdk);
                model.setSdk(sdk);
                ProjectRootManager.getInstance(model.getProject()).setProjectSdk(sdk);

              //  System.out.println(model.getProject().getBaseDir());
                FileBasedIndex.getInstance().requestReindex(model.getProject().getBaseDir());

             //   VirtualFileManager.getInstance().syncRefresh(); // This worked I think
               //or  LocalFileSystem.getInstance().refreshAndFindFileByPath();

              //  ProjectRootManager.getInstance(model.getProject()).setProjectSdkName(sdk.getName());
/*
                PsiTestUtil.addContentRoot()
                PsiTestUtil.checkFileStructure();
                PsiFile psiFile = PsiManager.getInstance(model.getProject())..findFile(virtualFile);
                psiFile.clearCaches();


                FileContentUtil.reparseFiles(model.getProject(), model.getProject().).*/



                    //    CachedValuesManager.getProjectPsiDependentCache().
             //   BuildDataManager
              //  ProjectManager.getInstance().
              /*  EditorSettingsExternalizable editorSettings = EditorSettingsExternalizable.getInstance();
                editorSettings.setUseSoftWraps(true);
                editorSettings.setAllSoftwrapsShown(true);
                ProjectSettingsService.getInstance(model.getProject()).
                editorSettings.setAddUnambiguousImportsOnTheFly(enable);*/

              /*  CodeStyleSettings settings = CodeStyleSettingsManager.getInstance(model.getProject()).getCurrentSettings();
                CommonCodeStyleSettings cs = settings.getCommonSettings("Java");*/
               // CustomCodeStyleSettings javaSettings = settings.getCustomSettings(CustomCodeStyleSettings.class);

               // javaSettings..ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = true;
            }

        } else {
            /* todo: download
            // Check if the SdkDownload service is available
            if (SdkDownload.EP_NAME.findExtension(SdkDownload.class) != null) {
                // Trigger the "Download JDK" UI
                SdkDownload.EP_NAME.findExtension(SdkDownload.class).showDownloadUI(project, null, null);
            } else {
                Messages.showErrorDialog(project, "JDK download functionality is not available.", "Error");
            }
            */
        }
    }

    /**
     * Create a new name for the project thanks to the configuration of the project (with the user token and the VPL ID)
     * The new name will be used to preconfigure the name and path text field when the user will pass to the second step.
     *
     * @param path the actual path of the project
     * @return the new name of the project (the name of the exercise linked to the VPL ID)
     */
    public static String createNameProject(String path) {
        //Retrieve the name of the exercise
        String name;
        try {
            name = WebserviceClientFactory.createFromStorageState().callService(VPLService.VPL_INFO).getString("name");
        } catch (VplConnectionException | MoodleWebServiceException e) {
            return "";
        }

        //Replace all the forbidden characters which can't be in the name of a project
        for (String s : forbiddenCharacters) {
            name = name.replace(s, "");
        }
        name = name.replace("  ", " ");


        //Erase in the path the previous name
        //path = path.replace(name, "");

        //If the name already exists, try to add a number at the end to differentiate it
        File dir = new File(path + File.separator + name);
        int i = 1;
        String newName = name;
        while (dir.exists()) {
            newName = name + i;
            dir = new File(path + File.separator + newName);
            i++;
        }
        return newName;
    }

    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
        return new CompSciToolsModuleWizardStep();
    }

    /**
     * Return true if the given project is a vpl one.
     * This is detected with the presence of the special file (containing vplID) at the root of the project.
     *
     * @param project current project.
     * @return {@code true} if the project is a CompSci Tools project, {@code false} otherwise.
     */
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

}
