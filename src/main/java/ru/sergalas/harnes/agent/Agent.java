package ru.sergalas.harnes.agent;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Agent {

    private static final String SYSTEM_PROMPT = """
            Ты — Harness, агент для написания кода, работающий в локальном репозитории.
                Отвечай кратко.
        """;

    private Conversation conversation = new Conversation(SYSTEM_PROMPT);

    private final ChatModel chatModel;

    public Agent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String run(String userMessage) {
        conversation.addUserMessage(userMessage);
        AssistantMessage assistantMessage = chatModel.call(new Prompt(conversation.toPromptMessage())).getResult().getOutput();
        conversation.addAssistantMessage(assistantMessage);
        return assistantMessage.getText();
    }
}
