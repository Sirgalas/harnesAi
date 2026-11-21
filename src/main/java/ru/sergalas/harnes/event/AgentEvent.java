package ru.sergalas.harnes.event;

import org.springframework.context.ApplicationEvent;

import java.time.Duration;

public sealed interface AgentEvent {
    record TurnStarted(String userMessage) implements AgentEvent {}
    record AgentSpoke(String assistantMessage) implements AgentEvent {}
    record TurnEnded(Duration duration, int iteration) implements AgentEvent {}

    public record TollCallStarted(String toolName, String description) implements AgentEvent {}
    public record TollCallFinished(String toolName, boolean error, String preview, Duration  duration) implements AgentEvent {}
    record TurnLimitReached(int maxTurn) implements AgentEvent {}
}
