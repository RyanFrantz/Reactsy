package com.etsy.android.reactsy;

import android.os.Handler;
import android.util.Log;

import java.util.Random;

/**
 * Created by mhorowitz on 8/19/14.
 */
public class ReactsyTestState {
    public static final int STATE_BEFORE_TEST = 0;
    public static final int STATE_USER_WAITING = 1;
    public static final int STATE_TRIAL_STARTED = 2;
    public static final int STATE_TRIAL_VALID = 3;
    public static final int STATE_TOO_SOON = 4;
    public static final int STATE_TOO_LATE = 5;
    public static final int STATE_AFTER_TEST = 6;

    public static final String[] STATES =
        {
            "before_test",
            "user_waiting",
            "trial_started",
            "trial_valid",
            "too_soon",
            "too_late",
            "after_test"
        };

    private static final String TAG = ReactsyTestState.class.getName();

    // TODO: Change ReactsyTestState to be Observable and use Observer if more than one is ever needed
    public interface StateChangeObserver {
        public void handleStateChange();
    }

    protected static class DelayedStateTransition implements Runnable {
        public static ReactsyTestState testState = null;

        protected boolean stillActive = true;
        protected int mToState;

        public DelayedStateTransition(int toState) {
            this.mToState = toState;
        }

        public void deactivate() {
            this.stillActive = false;
        }

        public void activate() {
            this.stillActive = true;
        }

        @Override
        public void run() {
            if (this.stillActive) {
                testState.setCurrentState(this.mToState);
            }
            else {
                this.stillActive = true;
            }
        }
    }

    private int mCurrentState = STATE_BEFORE_TEST;
    protected Handler mHandler = new Handler();

    protected ReactsyTestParameters mTestParameters;
    protected StateChangeObserver mObserver;
    protected ReactsyTestResults mTestResults;

    protected long mTrialDelayMs;
    protected long mWaitStartMs;
    protected long mTrialStartMs;

    private final DelayedStateTransition TO_USER_WAITING =
        new DelayedStateTransition(STATE_USER_WAITING);
    private final DelayedStateTransition TO_TRIAL_STARTED =
        new DelayedStateTransition(STATE_TRIAL_STARTED);
    private final DelayedStateTransition TO_TRIAL_VALID =
        new DelayedStateTransition(STATE_TRIAL_VALID);
    private final DelayedStateTransition TO_TOO_LATE =
        new DelayedStateTransition(STATE_TOO_LATE);
    private final DelayedStateTransition TO_AFTER_TEST =
        new DelayedStateTransition(STATE_AFTER_TEST);

    public ReactsyTestState(StateChangeObserver observer,
                            ReactsyTestParameters testParameters,
                            ReactsyTestResults testResults) {
        this.mObserver = observer;
        this.mTestParameters = testParameters;
        this.mTestResults = testResults;

        // ReactsyTestState is essentially a singleton
        DelayedStateTransition.testState = this;
    }

    // Factory method
    public DelayedStateTransition newDelayedStateTransition(int toState) {
        return new DelayedStateTransition(toState);
    }

    protected void postDelayedStateTransition(DelayedStateTransition transition, long delayMs) {
        transition.activate();
        this.mHandler.postDelayed(transition, delayMs);
    }

    protected void postDelayedStateTransition(int toState, long delayMs) {
        this.postDelayedStateTransition(newDelayedStateTransition(toState), delayMs);
    }

    protected void cancelDelayedStateTransition(DelayedStateTransition transition) {
        transition.deactivate();
        this.mHandler.removeCallbacks(transition);
    }

    protected void cancelAllDelayedTransitions() {
        cancelDelayedStateTransition(TO_USER_WAITING);
        cancelDelayedStateTransition(TO_TRIAL_STARTED);
        cancelDelayedStateTransition(TO_TRIAL_VALID);
        cancelDelayedStateTransition(TO_TOO_LATE);
    }

    public int getCurrentState() {
        synchronized (this) {
            return this.mCurrentState;
        }
    }

    public void setCurrentState(int newState) {
        setCurrentState(newState, true);
    }

    protected void setCurrentState(int newState, boolean notifyObservers) {
        synchronized (this) {
            if (this.mCurrentState != STATE_AFTER_TEST) {
                cancelAllDelayedTransitions();
                switch (newState) {
                    case STATE_USER_WAITING:
                        // Set up random test trial
                        this.mWaitStartMs = System.currentTimeMillis();
                        this.mTrialDelayMs = getNextTestDelay(this.mTestParameters.minDelayMs,
                                                              this.mTestParameters.maxDelayMs);
                        postDelayedStateTransition(TO_TRIAL_STARTED, this.mTrialDelayMs);
                        break;

                    case STATE_TRIAL_STARTED:
                        this.mTrialStartMs = System.currentTimeMillis();
                        postDelayedStateTransition(TO_TRIAL_VALID, this.mTestParameters.tooSoonMs);
                        break;

                    case STATE_TRIAL_VALID:
                        postDelayedStateTransition(TO_TOO_LATE,
                                                   this.mTestParameters.tooLateMs - this.mTestParameters.tooSoonMs);
                        break;

                    case STATE_TOO_SOON:
                        postDelayedStateTransition(TO_USER_WAITING,
                                                   this.mTestParameters.recoveryDelayMs);
                        break;

                    case STATE_TOO_LATE:
                        // Record too late time
                        recordTrial(STATE_TOO_LATE, this.mCurrentState,
                                    this.mTrialDelayMs, this.mTestParameters.tooLateMs);
                        postDelayedStateTransition(TO_USER_WAITING,
                                                   this.mTestParameters.recoveryDelayMs);
                        break;
                }

                this.mCurrentState = newState;
            }
        }
        if (notifyObservers) {
            this.mObserver.handleStateChange();
        }
    }

    public void resetTest() {
        synchronized (this) {
            this.mCurrentState = STATE_BEFORE_TEST;
            cancelDelayedStateTransition(TO_AFTER_TEST);
            postDelayedStateTransition(TO_AFTER_TEST, this.mTestParameters.testDurationSec * 1000);

            resetTrialData();
        }
    }

    public void restoreTestState(int toState) {
        synchronized (this) {
            this.mCurrentState = toState;
        }
        this.mObserver.handleStateChange();
    }

    public int getNextTestDelay(int minDelayMs, int maxDelayMs) {
        Random r = new Random();
        return r.nextInt(maxDelayMs - minDelayMs) + minDelayMs;
    }

    public void handleTrialEvent() {
        boolean stateChanged = false;
        synchronized (this) {
            long nowMs = System.currentTimeMillis();
            long reactionMs = nowMs - this.mTrialStartMs;
            switch (this.mCurrentState) {
                case STATE_USER_WAITING:
                    // Record too soon time and current state
                    recordTrial(STATE_TOO_SOON, this.mCurrentState, this.mTrialDelayMs,
                                nowMs - this.mWaitStartMs - this.mTrialDelayMs);
                    setCurrentState(STATE_TOO_SOON, false);
                    stateChanged = true;
                    break;
                case STATE_TRIAL_STARTED:
                    // Record too soon time and current state
                    recordTrial(STATE_TOO_SOON, this.mCurrentState, this.mTrialDelayMs, reactionMs);
                    setCurrentState(STATE_TOO_SOON, false);
                    stateChanged = true;
                    break;
                case STATE_TRIAL_VALID:
                    // Record trial time
                    recordTrial(STATE_TRIAL_VALID, this.mCurrentState, this.mTrialDelayMs, reactionMs);
                    setCurrentState(STATE_USER_WAITING, false);
                    stateChanged = true;
                    break;
                default:
                    // ignore in all other states
                    break;
            }
        }
        if (stateChanged) {
            this.mObserver.handleStateChange();
        }
    }

    protected void resetTrialData() {
        Log.i(TAG, "resetTrialData");
        this.mTestResults.clear();
    }

    protected void recordTrial(int resultState, int fromState, long waitMs, long reactionMs) {
        Log.i(TAG, "recordTrial [RESULT: " + STATES[resultState]
                               + " FROM: " + STATES[fromState]
                               + " WAIT: " + waitMs + "ms"
                               + " REACTION: " + reactionMs + "ms");
        this.mTestResults.addTrial(resultState, fromState, waitMs, reactionMs);
    }
}
