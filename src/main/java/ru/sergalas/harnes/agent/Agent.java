package ru.sergalas.harnes.agent;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.sergalas.harnes.event.AgentEvent;

import java.time.Duration;
import java.time.Instant;

@Service
public class Agent {

    private static final String SYSTEM_PROMPT = """
            Ты — Harness, агент для написания кода, работающий в локальном репозитории.
                Отвечай кратко.
        """;
    private final ApplicationEventPublisher publisher;
    private Conversation conversation = new Conversation(SYSTEM_PROMPT);
    private final ChatModel chatModel;

    public Agent(ChatModel chatModel, ApplicationEventPublisher publisher) {
        this.chatModel = chatModel;
        this.publisher = publisher;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public String getChatModel() {
        return chatModel.getOptions().getModel();
    }

    public String run(String userMessage) {
        Instant startTime = Instant.now();
        conversation.addUserMessage(userMessage);
        publisher.publishEvent(new AgentEvent.TurnStarted(userMessage));
        int iteration = 1;
        AssistantMessage assistantMessage = chatModel.call(new Prompt(conversation.toPromptMessages())).getResult().getOutput();
        conversation.addAssistantMessage(assistantMessage);
        publisher.publishEvent(new AgentEvent.AgentSpoke(assistantMessage.getText()));
        publisher.publishEvent(new AgentEvent.TurnEnded(Duration.between(startTime, Instant.now()), iteration));
        return assistantMessage.getText();
    }
}
