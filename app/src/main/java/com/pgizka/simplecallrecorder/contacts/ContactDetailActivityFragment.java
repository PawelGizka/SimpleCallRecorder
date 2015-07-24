package com.pgizka.simplecallrecorder.contacts;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pgizka.simplecallrecorder.R;
import com.pgizka.simplecallrecorder.data.RecorderContract;
import com.pgizka.simplecallrecorder.main.MainActivity;
import com.pgizka.simplecallrecorder.recordings.RecordingDetailActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class ContactDetailActivityFragment extends Fragment {

    ListView listView;
    ImageView mainImage;
    TextView displayNameText, phoneText, statusText;

    Cursor contactCursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_detail, container, false);

        listView = (ListView) view.findViewById(R.id.contact_detail_list_view);
        mainImage = (ImageView) view.findViewById(R.id.contact_detail_main_image);
        displayNameText = (TextView) view.findViewById(R.id.contact_detail_display_name_text);
        phoneText = (TextView) view.findViewById(R.id.contact_detail_phone_text);
        statusText = (TextView) view.findViewById(R.id.contact_detail_status_text);

        Intent intent = getActivity().getIntent();
        Uri uri = intent.getData();
        contactCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        contactCursor.moveToFirst();
        int id = contactCursor.getInt(contactCursor.getColumnIndex(RecorderContract.ContactEntry._ID));
        String selection = RecorderContract.RecordEntry.COLUMN_CONTACT_KEY + " = ? ";
        String [] selectionArgs = {Integer.toString(id)};
        Cursor recordCursor = getActivity().getContentResolver().query(RecorderContract.getContentUri(RecorderContract.PATH_RECORD),
                null, selection, selectionArgs, null);
        ContactDetailAdapter adapter = new ContactDetailAdapter(getActivity(), recordCursor);
        listView.setAdapter(adapter);

        String displayName = contactCursor.getString(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_DISPLAY_NAME));
        String phoneNumber = contactCursor.getString(contactCursor.getColumnIndex(RecorderContract.ContactEntry.COLUMN_PHONE_NUMBER));

        displayNameText.setText(displayName);
        phoneText.setText(phoneNumber);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), RecordingDetailActivity.class);
                intent.setData(RecorderContract.buildRecordItem(RecorderContract.getContentUri(
                        RecorderContract.PATH_RECORD_WITH_CONTACT),id));
                startActivity(intent);
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent upIntent = new Intent(getActivity(), MainActivity.class);
                upIntent.putExtra("position", 2);
                NavUtils.navigateUpTo(getActivity(), upIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
