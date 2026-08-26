package de.knollfrank.extensionsformaps.feature.addstop;

class StateHandler {

    public enum State {
        IDLE,
        WAITING_FOR_STOP_COUNT_CLICK,
        WAITING_FOR_LAST_STOP_CLICK,
        WAITING_FOR_CLEAR_CLICK
    }

    public State state = State.IDLE;
}
