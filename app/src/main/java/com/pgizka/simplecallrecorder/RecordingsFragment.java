package com.pgizka.simplecallrecorder;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pgizka.simplecallrecorder.com.pgizka.simplecallrecorder.data.RecorderContract;


public class RecordingsFragment extends Fragment {
    static  final String TAG = RecordingsFragment.class.getSimpleName();

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recordings, container, false);

        listView = (ListView) view.findViewById(R.id.recordings_list_viev);

        Cursor cursor = getActivity().getContentResolver().query(
                RecorderContract.getContentUri(RecorderContract.PATH_RECORD_WITH_CONTACT),
                null, null, null, null);
        RecordingsAdapter recordingsAdapter = new RecordingsAdapter(getActivity(), cursor);
        listView.setAdapter(recordingsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClicked(id);
            }
        });

        return view;
    }

    private void onListItemClicked(long id){
        Intent intent = new Intent(getActivity(), RecordingDetailActivity.class);
        intent.setData(RecorderContract.buildRecordItem(RecorderContract.getContentUri(
                RecorderContract.PATH_RECORD_WITH_CONTACT),id));
        startActivity(intent);
    }


}
