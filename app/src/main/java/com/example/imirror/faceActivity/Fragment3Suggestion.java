package com.example.imirror.faceActivity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.imirror.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment3Suggestion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment3Suggestion extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String text;

    public Fragment3Suggestion() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param str1 Parameter 1.
     * @param str2 Parameter 2.
     * @return A new instance of fragment Fragment3Suggestion.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment3Suggestion newInstance(String str1, String str2) {
        Fragment3Suggestion fragment = new Fragment3Suggestion();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, str1);
        args.putString(ARG_PARAM2, str2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            Log.d("cindy","mParam1: "+mParam1);
        }
        //Log.d("cindy",mParam1+mParam2);
        //
        //mytext.setText(mParam1+mParam2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment3_suggestion, container, false);
        TextView mytext = view.findViewById(R.id.mytext);
        if (getArguments() != null){
            text = getArguments().getString("name");
        }

        Log.d("cindy","userName: "+text);
        mytext.setText(text);

        return view;
    }
}