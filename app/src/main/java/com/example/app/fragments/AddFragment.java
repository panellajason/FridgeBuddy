package com.example.app.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.app.R;
import com.example.app.activities.AddByPicActivity;
import com.example.app.activities.AddManuallyActivity;


public class AddFragment extends Fragment {

    private Button picBtn;
    private Button manualBtn;


    public AddFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add, container, false);

        picBtn = view.findViewById(R.id.addByPicBtn);
        manualBtn = view.findViewById(R.id.addManuallyBTN);

        getActivity().setTitle("Choose a method");

        picBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddByPicActivity.class));
            }
        });


        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddManuallyActivity.class));
            }
        });


        return view;
    }

}
