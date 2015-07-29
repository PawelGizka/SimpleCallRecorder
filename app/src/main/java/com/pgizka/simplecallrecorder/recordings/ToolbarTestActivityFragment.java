package com.pgizka.simplecallrecorder.recordings;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pgizka.simplecallrecorder.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToolbarTestActivityFragment extends Fragment {

    public ToolbarTestActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toolbar_test, container, false);

     

        return view;
    }
}
