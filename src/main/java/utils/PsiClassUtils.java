/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */

package utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author bruce.ge
 * @Date 2017/1/30
 * @Description
 */
public class PsiClassUtils {

    public static boolean isNotSystemClass(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null || qualifiedName.startsWith("java.")) {
            return false;
        }
        return true;
    }

    public static boolean isValidSetMethod(PsiMethod m) {
        return m.hasModifierProperty(PsiModifier.PUBLIC) && !m.hasModifierProperty(PsiModifier.STATIC) && m.getName().startsWith("set");
    }

    public static boolean isValidGetMethod(PsiMethod m) {
        return m.hasModifierProperty(PsiModifier.PUBLIC) && !m.hasModifierProperty(PsiModifier.STATIC) &&
                (m.getName().startsWith("get") || m.getName().startsWith("is"));
    }

    public static boolean isValidBuilderMethod(PsiMethod m) {
        return m.hasModifierProperty(PsiModifier.PUBLIC) && m.hasModifierProperty(PsiModifier.STATIC) &&
                Objects.equals("builder", m.getName());
    }

    public static List<PsiMethod> addSetMethodToList(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .filter(PsiClassUtils::isValidSetMethod)
                .collect(Collectors.toList());
    }

    public static List<PsiMethod> addGettMethodToList(PsiClass psiClass) {
        return Arrays.stream(psiClass.getMethods())
                .filter(PsiClassUtils::isValidGetMethod)
                .collect(Collectors.toList());
    }

    @NotNull
    public static List<PsiMethod> extractSetMethods(PsiClass psiClass) {
        List<PsiMethod> methodList = new ArrayList<>();
        while (isNotSystemClass(psiClass)) {
            methodList.addAll(addSetMethodToList(psiClass));
            psiClass = psiClass.getSuperClass();
        }
        return methodList;
    }

    public static List<PsiMethod> extractGetMethod(PsiClass psiClass) {
        List<PsiMethod> methodList = new ArrayList<>();
        while (isNotSystemClass(psiClass)) {
            addGettMethodToList(psiClass);
            psiClass = psiClass.getSuperClass();
        }
        return methodList;
    }

    public static boolean checkClassHasValidSetMethod(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        while (isNotSystemClass(psiClass)) {
            boolean hasValidSetMethod = Arrays.stream(psiClass.getMethods())
                    .anyMatch(PsiClassUtils::isValidSetMethod);
            if (hasValidSetMethod) {
                return true;
            }
            psiClass = psiClass.getSuperClass();
        }
        return false;
    }


    public static boolean checkClasHasValidGetMethod(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        while (isNotSystemClass(psiClass)) {
            for (PsiMethod m : psiClass.getMethods()) {
                if (isValidGetMethod(m)) {
                    return true;
                }
            }
            psiClass = psiClass.getSuperClass();
        }
        return false;
    }
}
