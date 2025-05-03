package environment;

public final class EnvironmentHolder {

    private static GridEnvironment environment;

    private EnvironmentHolder() { }

    public static void setEnvironment(GridEnvironment env) {
        EnvironmentHolder.environment = env;
    }

    public static GridEnvironment getEnvironment() {
        return environment;
    }
}
