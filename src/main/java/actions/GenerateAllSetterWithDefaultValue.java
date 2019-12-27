package actions;

public class GenerateAllSetterWithDefaultValue extends BaseGenerateAllSetter {

    @Override
    protected boolean hasDefaultValue() {
        return true;
    }
}
