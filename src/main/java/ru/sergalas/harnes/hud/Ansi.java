package ru.sergalas.harnes.hud;

/**
 * Цвета зелёного интерфейса терминала.
 *
 * <p>Всё, что относится к внешнему виду, живёт в пакете {@code hud} и только здесь.
 */
final class Ansi {

    private static final String ESC = "\u001B";

    static final String RESET = ESC + "[0m";
    static final String GREEN = ESC + "[32m";
    static final String BRIGHT_GREEN = ESC + "[92m";
    static final String DIM_GREEN = ESC + "[2;32m";
    static final String BOLD = ESC + "[1m";
    static final String RED = ESC + "[31m";
    static final String BRIGHT_RED = ESC + "[91m";
    static final String WHITE = ESC + "[37m";

    private Ansi() {}

    static String green(String text) {
        return GREEN + text + RESET;
    }

    static String brightGreen(String text) {
        return BRIGHT_GREEN + text + RESET;
    }

    static String dim(String text) {
        return DIM_GREEN + text + RESET;
    }

    static String bold(String text) {
        return BOLD + text + RESET;
    }

    static String red(String text) {
        return RED + text + RESET;
    }

    static String brightRed(String text) {
        return BRIGHT_RED + text + RESET;
    }

    static String white(String text) {
        return WHITE + text + RESET;
    }
}
