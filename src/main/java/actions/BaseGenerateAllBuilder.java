package actions;

import com.google.common.collect.Sets;
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
import entity.Parameter;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import utils.*;

import java.util.Arrays;
import java.util.Set;

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
        Pair<String, Set<String>> codeAndImportStatements = generateCode(fields, splitText);
        WriteCommandAction.runWriteCommandAction(project,
                () -> document.insertString(expression.getTextOffset() + expression.getTextLength(),
                        codeAndImportStatements.getLeft()));
        PsiToolUtils.addImportToFile(project, (PsiJavaFile) file, document, codeAndImportStatements.getRight());
    }

    /**
     * @param fields
     * @param splitText
     * @return left是
     */
    @NotNull
    private Pair<String, Set<String>> generateCode(PsiField[] fields, String splitText) {
        StringBuilder code = new StringBuilder();
        Set<String> importPackages = Sets.newHashSet();
        Arrays.stream(fields).forEach(psiField -> {
            code.append(splitText);
            code.append('.');
            code.append(psiField.getName());
            code.append("(");
            if (hasDefaultValue()) {
                String parmaterClassName = psiField.getType().getCanonicalText();
                Parameter parameter = PsiToolUtils.extraParmaterFromFullyQualifiedName(parmaterClassName);
                String packagePath = parameter.getPackagePath();
                Pair<String, String> valueAndImport = CodeUtils.getDefaultValueAndDefaultImport(packagePath)
                        .orElse(Pair.of("new " + parameter.getClassName() + "()", packagePath));
                code.append(valueAndImport.getLeft());
                if (CodeUtils.isNeedToDeclareClasses(valueAndImport.getRight())) {
                    importPackages.add(valueAndImport.getRight());
                }
            }
            code.append(")");
        });
        code.append(splitText).append(".build()");
        return Pair.of(code.toString(), importPackages);
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
