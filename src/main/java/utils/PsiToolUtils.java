package utils;

import com.google.common.base.Splitter;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackageStatement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import entity.GenericParameter;
import entity.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author bruce.ge
 * @Date 2017/1/28
 * @Description
 */
public class PsiToolUtils {

    public static boolean checkGuavaExist(Project project, @NotNull PsiElement element) {
        //获取元素所在的module com.xingren.lombok.lomboktest.main
        Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
        if (moduleForPsiElement == null) {
            return false;
        }
        //搜索 当前module 有没有guava Lists类
        PsiClass[] listss = PsiShortNamesCache.getInstance(project).getClassesByName("Lists", GlobalSearchScope.moduleRuntimeScope(moduleForPsiElement, false));
        for (PsiClass psiClass : listss) {
            if (psiClass.getQualifiedName().equals("com.google.common.collect.Lists")) ;
            return true;
        }

        return false;
    }

    /**
     * 通过参数的全限定名称来分解出需要的路径和类名
     *
     * @param fullyQualifiedName
     * @return
     */
    @NotNull
    public static Parameter extraParmaterFromFullyQualifiedName(String fullyQualifiedName) {
        Parameter parameter = new Parameter();
        int u = fullyQualifiedName.indexOf("<");
        if (u == -1) {
            parameter.setClassName(extractShortName(fullyQualifiedName));
            parameter.setPackagePath(fullyQualifiedName);
        } else {
            String packagePath = fullyQualifiedName.substring(0, u);
            parameter.setClassName(extractShortName(packagePath));
            parameter.setPackagePath(packagePath);
            String realClassPart = fullyQualifiedName.substring(u + 1, fullyQualifiedName.length() - 1);
            List<String> genericParamaters = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(realClassPart);
            List<GenericParameter> genericParameters = genericParamaters.stream().map(s -> {
                GenericParameter param = new GenericParameter();
                param.setRealPackage(extractShortName(s));
                param.setRealName(s);
                return param;
            }).collect(Collectors.toList());
            parameter.setGenericParameters(genericParameters);
        }
        return parameter;
    }

    /**
     * 把imports 插入文件头中
     *
     * @param file
     * @param document
     * @param newImportList
     */
    public static void addImportToFile(Project project, PsiJavaFile file, Document document, Set<String> newImportList) {
        if (newImportList.isEmpty()) {
            return;
        }
        StringBuilder newImportText = new StringBuilder();
        for (String newImport : newImportList) {
            newImportText.append("\nimport ").append(newImport).append(";");
        }
        PsiPackageStatement packageStatement = file.getPackageStatement();
        int start = packageStatement != null ? packageStatement.getTextLength() + packageStatement.getTextOffset() : 0;
        String insertText = newImportText.toString();
        if (StringUtils.isNotBlank(insertText)) {
            WriteCommandAction.runWriteCommandAction(project,
                    () -> document.insertString(start, newImportText));
        }
    }

    @NotNull
    public static String lowerStart(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private static String extractShortName(String fullName) {
        return fullName.substring(fullName.lastIndexOf(".") + 1);
    }
}
