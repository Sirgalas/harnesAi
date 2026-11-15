package ru.sergalas.harnes.hud;

import org.springframework.ai.chat.messages.Message;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.sergalas.harnes.agent.Agent;
import ru.sergalas.harnes.context.TokenEstimator;
import ru.sergalas.harnes.event.AgentEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static ru.sergalas.harnes.hud.BodyPart.*;
import static ru.sergalas.harnes.hud.BootStatus.ONLINE;
import static ru.sergalas.harnes.hud.LanguageUtils.plural;

@Component
public class Hud {

    private final MachineViewPrinter machineViewPrinter;
    private final Agent agent;

    public Hud(MachineViewPrinter printer, Agent agent) {
        this.machineViewPrinter = printer;
        this.agent = agent;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent event) {
        machineViewPrinter.showBootLog(collectBootLines(event.getApplicationContext().getBeanDefinitionCount()));
    }

    private List<BootLine> collectBootLines(int beanDefinitionCount) {
        List<BootLine> bootLines = new ArrayList<>();
        bootLines.add(BootLine.of(POWER_CELL, ONLINE, "Спринг контекст " + plural(beanDefinitionCount,"бин","бина", "бинов")));
        bootLines.add(BootLine.of(NEUROPROCESSOR,ONLINE,agent.getChatModel()));
        bootLines.add(BootLine.of(MEMORY_BANK,ONLINE,agent.getConversation().getClass().getSimpleName()));
        bootLines.add(BootLine.of(DIAGNOSTIC_BUS,ONLINE, "Agent Event: " + AgentEvent.class.getPermittedSubclasses().length));
        return  bootLines;
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
