package ru.sergalas.harnes.agent;

import org.springframework.ai.chat.messages.*;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private final String systemPrompt;
    private List<Message> messages = new ArrayList<>();

    public Conversation(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public void addUserMessage(String message) {
        messages.add(new UserMessage(message));
    }

    public void addAssistantMessage(AssistantMessage assistantMessage) {
        messages.add(assistantMessage);
    }

    public List<Message> toPromptMessages() {
        ArrayList<Message> prompt = new ArrayList<>(messages.size() + 1);
        prompt.add(new SystemMessage(systemPrompt));
        prompt.addAll(messages);
        return prompt;
    }

    public void addToolResponse(ToolResponseMessage toolResponseMessage) {
        messages.add(toolResponseMessage);
    }
}
