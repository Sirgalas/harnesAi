package ru.sergalas.harnes.agent;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("harness.agent")
public record AgentProperty(
        @DefaultValue("20") int maxTunes,
        @DefaultValue("." ) String workspaceRoot
) {
    public AgentProperty {
        if (maxTunes < 1) {
            throw new IllegalArgumentException("harnes.agent.max-turns должен быть  >= 1, получено:  " + maxTunes);
        }
        if(workspaceRoot == null || workspaceRoot.isBlank()) {
            throw new IllegalArgumentException("harnes.agents.workspace-root не может быть пустым");
        }
    }
}
