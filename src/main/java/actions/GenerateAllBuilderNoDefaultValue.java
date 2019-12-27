package actions;

public class GenerateAllBuilderNoDefaultValue extends BaseGenerateAllBuilder {

    @Override
    protected boolean hasDefaultValue() {
        return false;
    }
}
