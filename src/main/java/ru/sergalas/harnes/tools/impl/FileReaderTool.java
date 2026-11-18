package ru.sergalas.harnes.tools.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import ru.sergalas.harnes.exception.WorkspaceViolationException;
import ru.sergalas.harnes.tools.Tool;
import ru.sergalas.harnes.tools.ToolResult;
import ru.sergalas.harnes.tools.WorkspaceGuard;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileReaderTool<I> implements Tool<FileReaderTool.Input> {

    public static final int DEFAULT_LIMIT = 2000;
    private static final int MAX_LINE_LENGTH = 2000;

    private final WorkspaceGuard workspaceGuard;

    private String render(List<String> lines, int from, int to) {
        StringBuilder out = new StringBuilder();
        for (int i = from; i <= to; i++) {
            String line = lines.get(i - 1);
            if (line.length() > MAX_LINE_LENGTH) {
                line = line.substring(0, MAX_LINE_LENGTH) + "… (строка обрезана)";
            }
            out.append(i).append('\t').append(line).append('\n');
        }
        if (to < lines.size()) {
            out.append("… показаны строки %d-%d из %d. Используй offset, чтобы прочитать дальше."
                    .formatted(from, to, lines.size()));
        }
        return out.toString();
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String description() {
        return """
                Reads a file from the local filesystem. Use this instead of guessing file contents.
                
                Usage:
                - file_path is relative to the project root
                - Output is in `cat -n` format: line number, tab, then the line content
                - Reads up to %d lines; use offset and limit for larger files
                - Returns an error message if the file does not exist
                """.formatted(DEFAULT_LIMIT);
    }

    @Override
    public Class<Input> inputType() {
        return Input.class;
    }

    @Override
    public String renderToolUseMessage(Input input) {
        return input.filePath();
    }

    @Override
    public ToolResult call(Input input) {
        Path pathToFile = null;
        try {
            pathToFile = workspaceGuard.resolve(input.filePath());
        }catch (WorkspaceViolationException e) {
            ToolResult.error(e.getMessage());
        }
        if(!Files.exists(pathToFile)) {
            return ToolResult.error("File does not exist " + input.filePath());
        }
        if(Files.isDirectory(pathToFile)) {
            return ToolResult.error("File is a directory " + input.filePath());
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(pathToFile);
        } catch (MalformedInputException e) {
            return ToolResult.error("File " + input.filePath() + " is malformed");
        }catch (IOException e) {
          return ToolResult.error("File %s: %s".formatted(input.filePath(), e.getMessage()));
        }
        if (lines.isEmpty()) {
            return ToolResult.ok("File %s is empty".formatted(input.filePath()));
        }

        int from = Math.max(1, input.offset() == null ? 1 : input.offset());
        int limit = input.limit() == null ? DEFAULT_LIMIT : Math.max(1, input.limit());
        int to = Math.min(lines.size(), from + limit - 1);
        if(from > lines.size()) {
            return ToolResult.error("File %s is larger than %s lines".formatted(input.filePath(),  lines.size()));
        }
        return ToolResult.ok(render(lines,from, to));
    }

    public record Input(
            @ToolParam(description = "Path to the file, relative to the project root")
            String filePath,

            @Nullable @ToolParam(required = false, description = "1-based line number to start from")
            Integer offset,

            @Nullable @ToolParam(required = false, description = "How many lines to read, default 2000")
            Integer limit) {
    }
}
