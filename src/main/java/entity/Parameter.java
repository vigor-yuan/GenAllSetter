package entity;

import java.util.List;

/**
 * 方法参数的拆解
 */
public class Parameter {

    private String packagePath;

    private String className;

    private List<GenericParameter> genericParameters;

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<GenericParameter> getGenericParameters() {
        return genericParameters;
    }

    public void setGenericParameters(List<GenericParameter> genericParameters) {
        this.genericParameters = genericParameters;
    }
}
