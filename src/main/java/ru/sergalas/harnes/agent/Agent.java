package ru.sergalas.harnes.agent;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Agent {

    private static final String SYSTEM_PROMPT = """
            Ты — Harness, агент для написания кода, работающий в локальном репозитории.
                Отвечай кратко.
        """;

    private Conversation conversation = new Conversation(SYSTEM_PROMPT);

    @Autowired
    private ChatModel chatModel;

    public String run(String userMessage) {
        return null;
    }
}
