package ru.sergalas.harnes.hud;

import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.sergalas.harnes.agent.Agent;
import ru.sergalas.harnes.context.TokenEstimator;
import ru.sergalas.harnes.event.AgentEvent;
import ru.sergalas.harnes.tools.Tool;

import java.util.*;
import java.util.regex.Pattern;

import static ru.sergalas.harnes.hud.BodyPart.*;
import static ru.sergalas.harnes.hud.BootStatus.ONLINE;
import static ru.sergalas.harnes.hud.LanguageUtils.plural;

@Component
public class Hud {

    private final MachineViewPrinter machineViewPrinter;
    private final Agent agent;
    private final List<Tool<?>> tools;

    public Hud(MachineViewPrinter printer, Agent agent, List<Tool<?>> tools) {
        this.machineViewPrinter = printer;
        this.agent = agent;
        this.tools = tools;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent event) {
        machineViewPrinter.showBootLog(collectBootLines(event.getApplicationContext().getBeanDefinitionCount()));
    }

    private List<BootLine> collectBootLines(int beanDefinitionCount) {
        Map<BodyPart,BootLine> bootLines = new EnumMap<>(BodyPart.class);
        bootLines.put(
                POWER_CELL,
                BootLine.of(POWER_CELL, ONLINE, "Спринг контекст " + plural(beanDefinitionCount,"бин","бина", "бинов"))
        );
        bootLines.put(
                NEUROPROCESSOR,
                BootLine.of(NEUROPROCESSOR,ONLINE,agent.getChatModel())
        );
        bootLines.put(
                MEMORY_BANK,
                BootLine.of(MEMORY_BANK,ONLINE,agent.getConversation().getClass().getSimpleName())
        );
        bootLines.put(
                DIAGNOSTIC_BUS,
                BootLine.of(DIAGNOSTIC_BUS,ONLINE, "Agent Event: " + AgentEvent.class.getPermittedSubclasses().length)
        );
        for (Tool<?> tool : tools) {
            BodyPart bodyPart = forToolName(tool.name());
            BootLine bootLine = bootLines.computeIfAbsent(bodyPart, BootLine::empty);
            bootLine.components().add(tool.name());
        }
        if(!tools.isEmpty()) {
            bootLines.put(
                TOOL_BUS,
                BootLine.of(TOOL_BUS,ONLINE,plural(tools.size(),"инструмент", "инструмента", "инструментов")));
        }

        return  List.copyOf(bootLines.values());
    }

    @EventListener
    public void onTurnStarted(AgentEvent.TurnStarted event) {
        machineViewPrinter.showOrder(event.userMessage());
        List<Message> promptMessages = agent.getConversation().toPromptMessages();
        machineViewPrinter.showContext(promptMessages, TokenEstimator.estimate(promptMessages));
    }

    @EventListener
    public void onTurnEnded(AgentEvent.TurnEnded event) {
        machineViewPrinter.showTurnFinished(event.duration(), event.iteration());
    }

    @EventListener
    public void onToolCallStarted(AgentEvent.TollCallStarted event) {
        machineViewPrinter.showToolCallStarted(event.description().isBlank() ?  event.toolName() : event.description());
    }
    @EventListener
    public void onToolCoolFinished(AgentEvent.TollCallFinished event) {
        machineViewPrinter.showToolCallFinished(event.error(),event.preview(),event.duration());
    }

    @EventListener
    public void onTurnLimitReached(AgentEvent.TurnLimitReached event) {
        machineViewPrinter.showTurnLimitReached(event.maxTurns());
    }


    //@EventListener
    public void onAgentSpokeEvent(AgentEvent.AgentSpoke event) {
        String assistantMessage = event.assistantMessage();
        boolean notRussian = Pattern.compile("A-Za-z")
                .matcher(assistantMessage)
                .find();
        if(notRussian) {
            System.err.println("Говори по русски");
            throw new RuntimeException(assistantMessage);
        }
    }
}
