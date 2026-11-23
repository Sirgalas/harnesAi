package ru.sergalas.harnes.tools;

import org.springframework.ai.util.JsonHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.sergalas.harnes.event.AgentEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolRegistry {

    private final Map<String, Tool<?>>  toolsByName = new LinkedHashMap<>();
    private final ApplicationEventPublisher publisher;
    private final JsonHelper jsonHelper = new JsonHelper();

    public ToolRegistry(List<Tool<?>> tools, ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        for (Tool<?> tool : tools) {
            Tool<?> clash = toolsByName.put(tool.name(), tool);
            if (clash != null) {
                throw new IllegalStateException(
                        "два инструмента с именем '%s': %s и %s"
                                .formatted(tool.name(), clash.getClass(), tool.getClass()));

            }
        }
    }

    public List<Tool<?>> tools() {
        return List.copyOf(toolsByName.values());
    }

    public ToolResult execute(String toolName, String arguments) {
        Tool<?> tool = toolsByName.get(toolName);
        if(tool == null){
            return ToolResult.error("нет инструмента %s. Доступны: %s".formatted(toolName, toolsByName.keySet()));
        }
        publisher.publishEvent(new AgentEvent.TollCallStarted(toolName, describeCall(tool, arguments)));
        Instant toolStartedAt = Instant.now();
        ToolResult toolResult = invoke(tool, arguments);
        publisher.publishEvent(
            new AgentEvent.TollCallFinished(
                toolName,
                toolResult.error(),
                toolResult.preview(100),
                Duration.between(
                    toolStartedAt,
                    Instant.now()
                )
            )
        );
        return toolResult;
    }

    private <I> String describeCall(Tool<I> tool, String argumentsJson) {
        try {
            String described = tool.renderToolUseMessage(jsonHelper.fromJson(argumentsJson, tool.inputType()));
            return described == null ? "" : described;
        } catch (Exception argumentsAreBroken) {
            return "";
        }
    }

    private <I> ToolResult invoke(Tool<I> tool, String argumentsJson) {
        I input;
        try {
            input = jsonHelper.fromJson(argumentsJson, tool.inputType());
        } catch (Exception e) {
            return ToolResult.error("не разобрать аргументы %s: %s".formatted(tool.name(), e.getMessage()));
        }
        try {
            return tool.call(input);
        } catch (Exception e) {
            return ToolResult.error("%s упал: %s".formatted(tool.name(), e));
        }
    }
}
