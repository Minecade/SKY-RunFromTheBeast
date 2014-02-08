package com.minecade.rfb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public abstract class Callback<T> implements Runnable {

    private List<T> results;
    private State state = State.PENDING;
    private List<String> errorMessages;
    private Level level = Level.INFO;

    public synchronized void addResult(T toAdd) {
        if (null != toAdd) {
            switch (state) {
            case PENDING:
                if (null == this.results) {
                    this.results = new ArrayList<T>();
                }
                this.results.add(toAdd);
                return;
            default:
                throw new IllegalStateException(String.format("Cannot add results on this state: %s", state));
            }
        }
    }

    public synchronized void addResults(List<T> toAdd) {
        if (null != toAdd) {
            switch (state) {
            case PENDING:
                results.addAll(toAdd);
                return;
            default:
                throw new IllegalStateException(String.format("Cannot add results on this state: %s", state));
            }
        }
    }

    public synchronized T getResult() {
        switch (state) {
        case DONE:
            return null != results && results.size() > 0 ? results.get(0) : null;
        default:
            throw new IllegalStateException(String.format("Cannot get result on this state: %s", state));
        }
    }

    public synchronized List<T> getResults() {
        switch (state) {
        case DONE:
            return results;
        default:
            throw new IllegalStateException(String.format("Cannot get results on this state: %s", state));
        }
    }

    public synchronized List<String> getErrorMessages() {
        switch (state) {
        case ERROR:
            return errorMessages;
        default:
            throw new IllegalStateException(String.format("Cannot get results on this state: %s", state));
        }
    }

    public synchronized void done() {

        if (null == this.results) {
            this.results = new ArrayList<T>();
        }

        state = State.DONE;
    }

    public synchronized void error(String errorMessage) {
        error(errorMessage, Level.SEVERE);
    }

    public synchronized void error(String errorMessage, Level level) {
        if (null == errorMessage) {
            throw new IllegalArgumentException("errorMessage cannot be null");
        }

        if (null == level) {
            throw new IllegalArgumentException("level cannot be null");
        }

        if (null == this.errorMessages) {
            this.errorMessages = new ArrayList<String>();
        }

        this.errorMessages.add(errorMessage);
        this.level = level;
        state = State.ERROR;
    }

    public synchronized void error(List<String> errorMessages, Level level) {

        if (null == errorMessages) {
            throw new IllegalArgumentException("errorMessages cannot be null");
        }

        if (null == level) {
            throw new IllegalArgumentException("level cannot be null");
        }

        if (null == this.errorMessages) {
            this.errorMessages = new ArrayList<String>();
        }

        this.errorMessages.addAll(errorMessages);
        this.level = level;
        state = State.ERROR;
    }

    public State getState() {
        return state;
    }

    public Level getLevel() {
        return level;
    }

    public enum State {
        PENDING, DONE, ERROR;
    }
}
