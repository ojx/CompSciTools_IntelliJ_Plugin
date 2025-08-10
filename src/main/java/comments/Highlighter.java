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

package comments;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * This class serves to highlight all the comments left by the teacher in the exercise.
 * The comments are preceded, in python file by '###' and in other files by '///'
 *
 * @author Joshua Monteiller
 */
public class Highlighter extends TypedHandlerDelegate {

    /**
     * Method called when the user typed any character
     *
     * @param c       the char typed by the user
     * @param project the project where the char is typed
     * @param editor  the editor of the file where the char is typed
     * @param file    the file where the char is typed
     * @return a result used in TypedHandlerDelegate
     */
    @Override
    public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        updateCommentsHighlight(editor, file);
        return Result.CONTINUE;
    }

    /**
     * This method serves to highlight all the comments of the opened file.
     *
     * @param ed the editor of the file
     */
    private void updateCommentsHighlight(Editor ed, PsiFile file) {
        //Detect the extension of the file, change the type of comment in case it's a Python file
        String comment = "///";
        String name = file.getName();
        int indexPoint = name.lastIndexOf(".");
        if (indexPoint != -1) {
            String ext = name.substring(indexPoint);
            if (ext.equals(".py")) {
                comment = "###";
            }

            //Erase all previous highlights
            for (RangeHighlighter highlighter : ed.getMarkupModel().getAllHighlighters()) {
                highlighter.dispose();
            }

            //Look after every possible comment in the file. Store the beginning and ending indices of the comment in an array which will be stored in another array
            String fileText = ed.getDocument().getText();
            int endLineIndex;
            int commentIndex = fileText.indexOf(comment);
            while (commentIndex != -1) {
                endLineIndex = fileText.indexOf("\n", commentIndex);
                //If the comments is at the end of the file, maybe there is no back to the line
                if (endLineIndex == -1) {
                    endLineIndex = fileText.length();
                }

                //Highlight the comments
                TextAttributes color = ed.getColorsScheme().getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);
                ed.getMarkupModel().addRangeHighlighter(commentIndex, endLineIndex, HighlighterLayer.ADDITIONAL_SYNTAX + 1, color, HighlighterTargetArea.EXACT_RANGE);

                commentIndex = fileText.indexOf(comment, endLineIndex);
            }
        }

    }
}

