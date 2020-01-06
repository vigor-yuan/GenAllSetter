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

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public abstract class BaseGenerateAllSetter extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        if (project == null || editor == null || file == null) {
            return;
        }
        //Get the element under the current cursor
        PsiElement element = PsiElementUtils.getElement(editor, file);
        //Confirm that the element's parent is a local variable declaration element
        //PsiTreeUtil can get the specified element in the psi tree
        PsiLocalVariable variable = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        if (variable == null) {
            return;
        }
        if (!(variable.getParent() instanceof PsiDeclarationStatement)) {
            return;
        }
        PsiElement parent = variable.getParent();
        //Get psi class
        PsiClass psiClass = PsiTypesUtil.getPsiClass(variable.getType());
        //Check if there is a set method
        List<PsiMethod> allSetMethods = PsiClassUtils.extractSetMethods(psiClass);
        String variableText = variable.getName();
        Document document = editor.getDocument();
        //split text
        String splitText = PsiDocumentUtils.calculateSplitText(document, parent.getTextOffset());
        //Generate insert code
        Pair<String, Set<String>> codeAndImports = generateCodeAndImports(allSetMethods, variableText, splitText);
        WriteCommandAction.runWriteCommandAction(project,
                () -> document.insertString(parent.getTextOffset() + parent.getTextLength(), codeAndImports.getLeft()));
        PsiToolUtils.addImportToFile(project, (PsiJavaFile) file, document, codeAndImports.getRight());

    }

    /**
     * Generate code and default imports
     *
     * @param allSetMethods
     * @param variableText
     * @param splitText
     * @return
     */
    @NotNull
    private Pair<String, Set<String>> generateCodeAndImports(List<PsiMethod> allSetMethods, String variableText, String splitText) {
        StringBuilder sb = new StringBuilder();
        Set<String> importPackages = Sets.newHashSet();
        allSetMethods.forEach(psiMethod -> {
            sb.append(splitText);
            sb.append(variableText);
            sb.append('.');
            sb.append(psiMethod.getName());
            sb.append("(");
            if (hasDefaultValue()) {
                PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
                IntStream.range(0, parameters.length)
                        .mapToObj(i -> generateParmaterCode(i, parameters.length, parameters[i].getType().getCanonicalText()))
                        .forEach(valueAndImport -> {
                            sb.append(valueAndImport.getLeft());
                            if (CodeUtils.isNeedToDeclareClasses(valueAndImport.getRight())) {
                                importPackages.add(valueAndImport.getRight());
                            }
                        });
            }
            sb.append(");");
        });
        return Pair.of(sb.toString(), importPackages);
    }

    public Pair<String, String> generateParmaterCode(Integer index, int size, String parmaterClassName) {
        StringBuilder code = new StringBuilder();
        Parameter parameter = PsiToolUtils.extraParmaterFromFullyQualifiedName(parmaterClassName);
        String packagePath = parameter.getPackagePath();
        Pair<String, String> valueAndImport = CodeUtils.getDefaultValueAndDefaultImport(packagePath)
                .orElse(Pair.of("new " + parameter.getClassName() + "()", packagePath));
        code.append(valueAndImport.getLeft());
        if (index != size - 1) {
            code.append(',');
        }
        return Pair.of(code.toString(), valueAndImport.getRight());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(isAvaliable(e));
    }

    private Boolean isAvaliable(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile file = e.getData(PlatformDataKeys.PSI_FILE);
        if (editor == null || file == null) {
            return false;
        }
        PsiElement element = PsiElementUtils.getElement(editor, file);
        PsiLocalVariable variable = PsiTreeUtil.getParentOfType(element, PsiLocalVariable.class);
        if (variable == null) {
            return false;
        }
        if (!(variable.getParent() instanceof PsiDeclarationStatement)) {
            return false;
        }
        PsiClass psiClass = PsiTypesUtil.getPsiClass(variable.getType());
        boolean hasValidSetMethod = PsiClassUtils.checkClassHasValidSetMethod(psiClass);
        if (!hasValidSetMethod) {
            return false;
        }
        return true;
    }

    abstract protected boolean hasDefaultValue();
}
