package io.github.sheikah45.fx2j.processor;

public class ProcessorException extends RuntimeException {

    private final String node;

    public ProcessorException(Throwable cause, String node) {
        super(cause);
        this.node = node;
    }

    public String getNode() {
        return node;
    }
}
