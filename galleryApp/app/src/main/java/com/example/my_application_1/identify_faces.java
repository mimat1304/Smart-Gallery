package com.example.my_application_1;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class identify_faces extends AppCompatActivity {
    List<Integer> userIds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_faces);
        Intent intent= getIntent();
        int []userIds=intent.getIntArrayExtra("userIds");

    }
}
