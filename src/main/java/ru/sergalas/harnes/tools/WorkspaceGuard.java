package ru.sergalas.harnes.tools;

import org.springframework.stereotype.Component;
import ru.sergalas.harnes.agent.AgentProperty;
import ru.sergalas.harnes.exception.WorkspaceViolationException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@Component
public class WorkspaceGuard {
    private final Path root;

    public WorkspaceGuard(AgentProperty agentProperty) {
        this.root = Path.of(agentProperty.workspaceRoot()).toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    public Path resolve(String path) throws WorkspaceViolationException {
        if(path == null || path.isBlank()) {
            throw new WorkspaceViolationException("путь не указан");
        }

        Path candidate = Path.of(path);
        Path resolved = (candidate.isAbsolute() ? candidate : root.resolve(candidate)).normalize();
        if(!resolved.startsWith(root)) {
            throw new WorkspaceViolationException(" путь не принадлежит рабочей директории");
        }
        try{
            Path realPath = resolved.toRealPath();
            if(!realPath.startsWith(root.toRealPath())) {
                throw new WorkspaceViolationException(" путь ведет наружу рабочей директории :" + path);
            }
        } catch (NoSuchFileException e) {
        } catch (IOException e) {
            throw new WorkspaceViolationException(" не удалось проверить путь :" + path);
        }
        return resolved;
    }

    public String relative(Path path) {
        return root.relativize(path).toString().replace("\\", "/");
    }
}
