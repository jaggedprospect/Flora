package com.jagged.flora.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.jagged.flora.FlowerPickerActivity;
import com.jagged.flora.R;


public class FlowerPickerFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1="param1";
    private static final String ARG_PARAM2="param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FlowerPickerFragment(){
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FlowerPickerFragment newInstance(){
        FlowerPickerFragment fragment=new FlowerPickerFragment();
        Bundle args=new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(getArguments()!=null){
            mParam1=getArguments().getString(ARG_PARAM1);
            mParam2=getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState){
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flower_picker,container,false);

        // Set OnClickListener() for Flower Picker Button (to start FlowerPickerActivity)
        Button fpButton = (Button)view.findViewById(R.id.flowerPickerButton);
        fpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent myIntent = new Intent(getActivity(), FlowerPickerActivity.class);
                startActivity(myIntent);
            }
        });

        return view;
    }
}