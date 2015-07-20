package com.pgizka.simplecallrecorder.com.pgizka.simplecallrecorder.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Pawe³ on 2015-07-10.
 */
public class RecorderContract {

    public static final String CONTENT_AUTHORITY = "com.pgizka.simplecallrecorder";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CONTACT = "contact";
    public static final String PATH_RECORD = "record";
    public static final String PATH_RECORD_WITH_CONTACT = "recordWithContact";
    //public static final String PATH_RECORD_WITH_CONTACT_WITH_SEPARATORS = "recordWithContactWithSeparators";

    public static final class ContactEntry implements BaseColumns {

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;

        // Table name
        public static final String TABLE_NAME = "contact";

        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_RECORDED = "recorded";
        public static final String COLUMN_IGNORED = "ignored";

    }

    public static final class RecordEntry implements BaseColumns{

        public static int TYPE_OUTGOING = 0;
        public static int TYPE_INCOMING = 1;

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECORD;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RECORD;


        public static final String TABLE_NAME = "record";

        public static final String COLUMN_CONTACT_KEY = "contact_id_fk";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LENGTH = "length";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_TYPE = "type";

    }

    public static final Uri getContentUri(String path){
        return BASE_CONTENT_URI.buildUpon().appendPath(path).build();
    }

    public static Uri buildRecordItem(Uri uri, long id) {
        return ContentUris.withAppendedId(uri, id);
    }



}
