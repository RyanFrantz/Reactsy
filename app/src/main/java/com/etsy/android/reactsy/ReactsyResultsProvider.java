package com.etsy.android.reactsy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by mhorowitz on 8/26/14.
 */
public class ReactsyResultsProvider extends ContentProvider {
    public static final String AUTHORITY =
        ReactsyResultsProvider.class.getPackage() + ReactsyResultsProvider.class.getName();

    protected static final int MATCH_TEST_INSTANCES = 0;
    protected static final int MATCH_TRIAL_RESULTS = 1;

    private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sMatcher.addURI(AUTHORITY, ReactsyTables.TestInstance.NAME, MATCH_TEST_INSTANCES);
        sMatcher.addURI(AUTHORITY, ReactsyTables.TrialResults.NAME, MATCH_TRIAL_RESULTS);
    }

    public static class TypeHelper {
        public static final String TYPE_PREFIX = "vnd.android.cursor.";
        public static final String TYPE_TEST_INSTANCES = TYPE_PREFIX + "test_instances";
        public static final String TYPE_TRIAL_RESULTS = TYPE_PREFIX + "trial_results";

        public static final String SUBTYPE_PREFIX = "vnd.";

        public static String buildType(String type, String subtypeSuffix) {
            return type + '/' + SUBTYPE_PREFIX + subtypeSuffix;
        }

        public static String buildType(String type) {
            return buildType(type, AUTHORITY);
        }
    }

    public static class UriHelper {
        public static final Uri URI_TEST_INSTANCE_BASE =
            Uri.parse("content://" + AUTHORITY + "/" + ReactsyTables.TestInstance.NAME);
        public static final Uri URI_TRIAL_RESULTS_BASE =
            Uri.parse("content://" + AUTHORITY + "/" + ReactsyTables.TrialResults.NAME);

        public static long getTestInstanceId(Uri uri) {
            return Long.parseLong(uri.getLastPathSegment());
        }

        public static Uri buildTrialResultsUri(String testInstanceId) {
            return Uri.withAppendedPath(URI_TRIAL_RESULTS_BASE, testInstanceId);
        }

        public static Uri buildTrialResultsUri(long testInstanceId) {
            return buildTrialResultsUri(Long.toString(testInstanceId));
        }
    }

    protected ReactsyDatabase mDb;

    @Override
    public boolean onCreate() {
        this.mDb = new ReactsyDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sMatcher.match(uri)) {
            case MATCH_TEST_INSTANCES:
                return this.mDb.getReadableDatabase().query(ReactsyTables.TestInstance.NAME,
                                                            projection, selection, selectionArgs,
                                                            null, null, sortOrder);
            case MATCH_TRIAL_RESULTS:
                return this.mDb.getReadableDatabase().query(ReactsyTables.TrialResults.NAME,
                                                            projection, selection, selectionArgs,
                                                            null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sMatcher.match(uri)) {
            case MATCH_TEST_INSTANCES:
                return TypeHelper.buildType(TypeHelper.TYPE_TEST_INSTANCES);
            case MATCH_TRIAL_RESULTS:
                return TypeHelper.buildType(TypeHelper.TYPE_TRIAL_RESULTS);
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sMatcher.match(uri)) {
            case MATCH_TEST_INSTANCES:
                long rowId = this.mDb.getWritableDatabase().insert(ReactsyTables.TestInstance.NAME,
                                                                   ReactsyTables.TestInstance.COL_LDAP,
                                                                   values);
                return UriHelper.buildTrialResultsUri(rowId);
            case MATCH_TRIAL_RESULTS:
                this.mDb.getWritableDatabase().insert(ReactsyTables.TrialResults.NAME,
                                                      ReactsyTables.TrialResults.COL_RESULT_STATE,
                                                      values);
                String testInstanceId =
                    values.getAsString(ReactsyTables.TrialResults.COL_TEST_INSTANCE_ID);
                return UriHelper.buildTrialResultsUri(testInstanceId);
            default:
                return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (sMatcher.match(uri)) {
            case MATCH_TEST_INSTANCES:
                return this.mDb.getWritableDatabase().delete(ReactsyTables.TestInstance.NAME,
                                                             selection,
                                                             selectionArgs);
            case MATCH_TRIAL_RESULTS:
                return this.mDb.getWritableDatabase().delete(ReactsyTables.TrialResults.NAME,
                                                             selection,
                                                             selectionArgs);
            default:
                return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Updates are not allowed, but we'll implement anyway
        switch (sMatcher.match(uri)) {
            case MATCH_TEST_INSTANCES:
                return this.mDb.getWritableDatabase().update(ReactsyTables.TestInstance.NAME,
                                                             values, selection, selectionArgs);
            case MATCH_TRIAL_RESULTS:
                return this.mDb.getWritableDatabase().update(ReactsyTables.TrialResults.NAME,
                                                             values, selection, selectionArgs);
            default:
                return 0;
        }
    }
}
