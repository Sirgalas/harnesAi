package ru.sergalas.harnes;

import org.springframework.boot.CommandLineRunner;
import ru.sergalas.harnes.agent.Agent;

public class DemoRunner implements CommandLineRunner {
    private final Agent agent;

    public DemoRunner(Agent agent) {
        this.agent = agent;
    }

    @Override
    public void run(String... args) {
        System.out.println(agent.run("Кто ты? Ответь одним предложением."));
    }
}
