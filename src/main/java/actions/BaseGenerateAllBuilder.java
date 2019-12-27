package actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;
import utils.CodeUtils;
import utils.PsiClassUtils;
import utils.PsiDocumentUtils;
import utils.PsiElementUtils;

import java.util.Arrays;

public abstract class BaseGenerateAllBuilder extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (!isAvaliable(e)) {
            return;
        }
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        PsiElement element = PsiElementUtils.getElement(editor, file);
        PsiMethodCallExpression expression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        PsiMethod psiMethod = expression.resolveMethod();
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());
        PsiField[] fields = psiClass.getFields();
        Document document = editor.getDocument();
        //split text
        String splitText = PsiDocumentUtils.calculateSplitText(document, expression.getTextOffset()) + "\t\t";
        //Generate code
        String code = generateCode(fields, splitText);
        WriteCommandAction.runWriteCommandAction(project,
                () -> document.insertString(expression.getTextOffset() + expression.getTextLength(), code));
    }

    @NotNull
    private String generateCode(PsiField[] fields, String splitText) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(fields).forEach(psiField -> {
            sb.append(splitText);
            sb.append('.');
            sb.append(psiField.getName());
            sb.append("(");
            if (hasDefaultValue()) {
                //TODO @Mars 需要添加导包
                String parmaterClassName = psiField.getType().getCanonicalText();
                sb.append(CodeUtils.getDefaultValue(parmaterClassName).orElse("new " + parmaterClassName + "()"));
            }
            sb.append(")");
        });
        sb.append(splitText).append(".build()");
        return sb.toString();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(isAvaliable(e));
    }

    private Boolean isAvaliable(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        if (project == null || editor == null || file == null) {
            return false;
        }
        PsiElement element = PsiElementUtils.getElement(editor, file);
        PsiMethodCallExpression expression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        if (expression == null) {
            return false;
        }
        PsiMethod psiMethod = expression.resolveMethod();
        if (psiMethod == null) {
            return false;
        }
        if (!PsiClassUtils.isValidBuilderMethod(psiMethod)) {
            return false;
        }
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());
        if (psiClass == null) {
            return false;
        }

        if (!psiClass.getName().endsWith("Builder")) {
            return false;
        }
        return true;
    }

    abstract protected boolean hasDefaultValue();
}
