package main.command;

public interface Command {
    void execute();
    void undo();
}