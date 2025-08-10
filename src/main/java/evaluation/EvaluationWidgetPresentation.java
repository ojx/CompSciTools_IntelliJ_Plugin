package evaluation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import module.CompSciToolsModuleBuilder;
import org.jetbrains.annotations.Nullable;
import service.ServiceGetter;
import ui.icons.CompSciToolsIcons;
import vplwsclient.RestJsonMoodleClient.VPLService;
import vplwsclient.exception.MoodleWebServiceException;
import vplwsclient.exception.VplConnectionException;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.swing.*;

/**
 * The EvaluationWidgetPresentation class represents a presentation for the evaluation widget.
 * It implements the StatusBarWidget.MultipleTextValuesPresentation interface.
 *
 * @see StatusBarWidget.MultipleTextValuesPresentation
 */
public class EvaluationWidgetPresentation implements StatusBarWidget.MultipleTextValuesPresentation {

    private static Integer freeEvaluations;
    private static Integer nbEvaluations;
    private static String reductionByEvaluation;

    private final Project project;

    /**
     * Constructor of the EvaluationWidgetPresentation class
     *
     * @param project the project
     */
    public EvaluationWidgetPresentation(Project project) {
        System.out.println("Project: " + project.getName());
        this.project = project;
        updateEvaluationCounter(project);
    }

    /**
     * Call {@code mod_vpl_subrestrictions} to update the Evaluation Counter.
     *
     * @param project The current project.
     */
    public static void updateEvaluationCounter(Project project) {
        ServiceGetter serv = new ServiceGetter(project.getBasePath() + "/");
        JsonObject values = null;
       /* try {
            values = serv.getRJMC().callService(VPLService.VPL_GET_SETTING);
        } catch (VplConnectionException e) {
            throw new RuntimeException(e);
        } catch (MoodleWebServiceException e) {
            throw new RuntimeException(e);
        }
        System.out.println(values.toString());*/
        try {
            values = serv.getRJMC().callService(VPLService.VPL_GET_SUBRESTRICTIONS);
        } catch (VplConnectionException | MoodleWebServiceException e) {
            System.out.println("Foook!");
            e.printStackTrace();
            freeEvaluations = null;
            nbEvaluations = null;
            reductionByEvaluation = null;
            return;
        }
      //  System.out.println(values.toString());
      //  System.out.println("Free evaluations: " + freeEvaluations);

        JsonValue freeEvaluationsValue = values.get("freeevaluations");
        if (freeEvaluationsValue.getValueType() == JsonValue.ValueType.NUMBER) {
            freeEvaluations = Integer.parseInt(freeEvaluationsValue.toString());
        } else {
            freeEvaluations = null;
        }

        JsonValue nEvaluationsValue = values.get("nevaluations");
        if (nEvaluationsValue.getValueType() == JsonValue.ValueType.NUMBER) {
            nbEvaluations = Integer.parseInt(nEvaluationsValue.toString());
        } else {
            nbEvaluations = null;
        }

        JsonValue reductionByEvaluationValue = values.get("reductionbyevaluation");
        if (reductionByEvaluationValue.getValueType() == JsonValue.ValueType.STRING) {
            reductionByEvaluation = reductionByEvaluationValue.toString().substring(1, reductionByEvaluationValue.toString().length() - 1);
            if (!reductionByEvaluation.endsWith("%")) {
                reductionByEvaluation += "pts";
            }
        } else {
            reductionByEvaluation = null;
        }
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
        if (freeEvaluations == null || nbEvaluations == null || reductionByEvaluation == null) {
            return "No evaluation limitation";
        }
        return nbEvaluations + "/" + freeEvaluations + " (-" + reductionByEvaluation + ")";
        /*"1/5 (-5pts)"*/
    }

    /**
     * Returns the text to display in the tooltip
     *
     * @return the text to display in the tooltip
     */
    @Nullable
    @Override
    public String getTooltipText() {
        if (!CompSciToolsModuleBuilder.isCompSciToolsProject(project))
            return "";
        if (freeEvaluations == null) {
            return "No evaluation limitation";
        }
        return Math.max(0, freeEvaluations - nbEvaluations) + " free evaluation(s) remaining,<br> then -" + reductionByEvaluation + " per evaluation"
                /*"5 free evaluation(s) remaining,<br> then -5pts per evaluation"*/;
    }

    /**
     * Returns the icon to display
     *
     * @return the icon to display
     */
    @Nullable
    @Override
    public Icon getIcon() {
        if (freeEvaluations != null)
            return CompSciToolsIcons.Eval;
        else
            return null;
    }

    public static int getFreeEvaluations() {
        return freeEvaluations == null ? -1 : freeEvaluations;
    }

    public static int getNbEvaluations() {
        return nbEvaluations == null ? -1 : nbEvaluations;
    }
}
