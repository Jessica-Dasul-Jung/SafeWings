package com.travelous.socialcomponents;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.travelous.socialcomponents.views.CallPolice;

public class CallActivity extends AppCompatActivity {

    private ImageView redbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        redbutton = (ImageView) findViewById(R.id.redbutton);
        redbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CallActivity.this, CallPolice.class);
                startActivity(intent);
            }
        });
    }
}
