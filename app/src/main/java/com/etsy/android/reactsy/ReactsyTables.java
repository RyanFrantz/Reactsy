package com.etsy.android.reactsy;

import android.provider.BaseColumns;

/**
 * Created by mhorowitz on 8/26/14.
 */
public class ReactsyTables {
    public static final String[] ALL_COLUMNS = null;
    public static final String ALL_ROWS = null;

    public static class TestInstance implements BaseColumns {
        public static final String NAME = "test_instance";

        public static final String COL_TEST_DURATION_SEC = "testDurationSec";
        public static final String COL_MIN_DELAY_MS = "minDelayMs";
        public static final String COL_MAX_DELAY_MS = "maxDelayMs";
        public static final String COL_TOO_SOON_MS = "tooSoonMs";
        public static final String COL_TOO_LATE_MS = "tooLateMs";
        public static final String COL_RECOVERY_DELAY_MS = "recoveryDelayMs";
        public static final String COL_MIN_VALID_TRIALS_PCT = "minValidTrialsPct";
        public static final String COL_TEST_MECHANISM = "testMechanism";
        public static final String COL_LDAP = "ldap";
        public static final String COL_TEST_IDENTITY = "testIdentity";
        public static final String COL_SUM_VALID_REACTIONS_MS = "sumValidTrialReactionsMs";
        public static final String COL_NUM_TRIALS_TOO_LATE = "numTrialsTooLate";
        public static final String COL_NUM_TRIALS_TOO_SOON = "numTrialsTooSoon";

        public static final String SQL_CREATE =
            "CREATE TABLE " + NAME
                + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                      + COL_TEST_DURATION_SEC + " INTEGER,"
                      + COL_MIN_DELAY_MS + " INTEGER,"
                      + COL_MAX_DELAY_MS + " INTEGER,"
                      + COL_TOO_SOON_MS + " INTEGER,"
                      + COL_TOO_LATE_MS + " INTEGER,"
                      + COL_RECOVERY_DELAY_MS + " INTEGER,"
                      + COL_MIN_VALID_TRIALS_PCT + " REAL,"
                      + COL_TEST_MECHANISM + " TEXT,"
                      + COL_LDAP + " TEXT,"
                      + COL_TEST_IDENTITY + " TEXT,"
                      + COL_SUM_VALID_REACTIONS_MS + " INTEGER,"
                      + COL_NUM_TRIALS_TOO_LATE + " INTEGER,"
                      + COL_NUM_TRIALS_TOO_SOON + " INTEGER)";
    }

    public static class TrialResults implements BaseColumns {
        public static final String NAME = "trial_results";

        public static final String COL_TEST_INSTANCE_ID = "id";
        public static final String COL_TRIAL = "trial";
        public static final String COL_RESULT_STATE = "resultState";
        public static final String COL_FROM_STATE = "fromState";
        public static final String COL_WAIT_MS = "waitMs";
        public static final String COL_REACTION_MS = "reactionMs";

        public static final String SQL_CREATE =
            "CREATE TABLE " + NAME
                + "(" + COL_TEST_INSTANCE_ID + " INTEGER,"
                + COL_TRIAL + " INTEGER,"
                + COL_RESULT_STATE + " INTEGER,"
                + COL_FROM_STATE + " INTEGER,"
                + COL_WAIT_MS + " INTEGER,"
                + COL_REACTION_MS + " INTEGER PRIMARY KEY ("
                + COL_TEST_INSTANCE_ID + ", " + COL_TRIAL + ") WITHOUT ROWID";
    }
}
