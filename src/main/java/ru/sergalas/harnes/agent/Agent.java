package ru.sergalas.harnes.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.sergalas.harnes.event.AgentEvent;
import ru.sergalas.harnes.tools.ToolAdapter;
import ru.sergalas.harnes.tools.ToolRegistry;
import ru.sergalas.harnes.tools.ToolResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class Agent {

    private static final String SYSTEM_PROMPT = """
            Ты — Harness, агент для написания кода, работающий в локальном репозитории.
            — Прежде чем использовать инструмент, ознакомьтесь с предоставленным контекстом. Если вас спрашивают о рабочем каталоге, ветке, незафиксированных изменениях или идентификаторе проекта, отвечайте, опираясь на данные из среды, репозитория и заметок по проекту, если они есть. Не нужно вызывать инструменты для проверки или пересчёта информации, которая уже представлена.
                Используйте инструменты только в тех случаях, когда необходимой информации нет, пользователь просит провести новую проверку или задача требует выполнения действия, а не ответа.
            — Чтобы найти файлы по имени, используйте Glob, а для поиска по содержимому — Grep. — Перед редактированием файла прочтите его. Редактирование меняет конкретный текст, а запись — весь файл целиком. — Если нужно изучить код, который не представлен в контексте, примените Grep для поиска нужного места, а затем прочитайте только необходимую часть.
                Отвечай кратко.
        """;
    private final ApplicationEventPublisher publisher;
    private Conversation conversation = new Conversation(SYSTEM_PROMPT);
    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final ToolAdapter toolAdapter;
    private final AgentProperty agentProperty;



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
        for(int iteration = 1; iteration <= agentProperty.maxTurns(); iteration++ ) {
            Prompt prompt = Prompt
                    .builder()
                    .messages(conversation.toPromptMessages())
                    .chatOptions(toolOptions())
                    .build();
            AssistantMessage assistantMessage = chatModel.call(prompt).getResult().getOutput();
            conversation.addAssistantMessage(assistantMessage);
            publisher.publishEvent(new AgentEvent.AgentSpoke(assistantMessage.getText()));

            if(!assistantMessage.hasToolCalls()){
                publisher.publishEvent(new AgentEvent.TurnEnded(Duration.between(startTime, Instant.now()), iteration));
                return assistantMessage.getText();
            }
            conversation.addToolResponse(executeAll(assistantMessage));
        }
        publisher.publishEvent(new AgentEvent.TurnLimitReached(agentProperty.maxTurns()));
        publisher.publishEvent(
            new AgentEvent.TurnEnded(
                Duration.between(startTime,Instant.now()),
                agentProperty.maxTurns()
            )
        );
        return "Достигнут предел в %d итераций — задача не завершена.".formatted(agentProperty.maxTurns());
    }

    private ToolResponseMessage executeAll(AssistantMessage assistantMessage) {
        List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
        for(AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()){
            ToolResult toolResult = toolRegistry.execute(toolCall.name(), toolCall.arguments());
            toolResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(),toolCall.name(), toolResult.content()));

        }
        return ToolResponseMessage.builder().responses(toolResponses).build();
    }

    private ChatOptions toolOptions() {
        return chatModel
                .getOptions()
                .mutate()
                .combineWith(
                    ToolCallingChatOptions
                        .builder()
                        .toolCallbacks(toolAdapter.toolCallbacks())
                )
                .build();
    }
}
