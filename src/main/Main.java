package main;

import agents.manager.AgentManager;
import agents.manager.AgentSettings;
import environment.EnvironmentFactory;
import environment.EnvironmentHolder;
import environment.GridEnvironment;
import gui.SimulationView;
import javafx.application.Application;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        GridEnvironment env = EnvironmentFactory.buildOfficeEnvironment();
        EnvironmentHolder.setEnvironment(env);

        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(ProfileImpl.GUI, "true");
        AgentContainer container = Runtime.instance().createMainContainer(profile);

        AgentManager.init(container);

        AgentSettings settings = new AgentSettings();
        settings.calmCount = 110;
        settings.panickedCount = 20;
        settings.injuredCount = 10;
        settings.fireplaceCount = 3;
        settings.firefighterCount = 5;
        settings.fireSensorCount = 10;
        settings.rescuerCount = 2;
        settings.fireSeverity = 3;

        SimulationView.setAgentSettings(settings);
        SimulationView.setEnvironment(env);
        Application.launch(SimulationView.class, args);
    }
}
