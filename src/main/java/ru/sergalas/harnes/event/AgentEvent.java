package ru.sergalas.harnes.event;

import java.time.Duration;

public sealed interface AgentEvent {
    record TurnStarted(String userMessage) implements AgentEvent {}
    record AgentSpoke(String assistantMessage) implements AgentEvent {}
    record TurnEnded(Duration duration, int iteration) implements AgentEvent {}
}
