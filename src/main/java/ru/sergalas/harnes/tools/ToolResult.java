package ru.sergalas.harnes.tools;

public record ToolResult(String content, boolean error) {
    private static  ToolResult ok(String content) {
        return new ToolResult(content, false);
    }
    private static  ToolResult error(String content) {
        return new ToolResult(content, true);
    }

    private String preview(int maxChar) {
        String firstLine = content.lines().findFirst().orElse("");
        return firstLine.length() <= maxChar ? firstLine : firstLine.substring(0, maxChar) + "...";
    }
}
