package actions;

public class GenerateAllSetterNoDefaultValue extends BaseGenerateAllSetter {

    @Override
    protected boolean hasDefaultValue() {
        return false;
    }
}
