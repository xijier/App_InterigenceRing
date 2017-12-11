package com.example.a15096.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button confirm_button = (Button) findViewById(R.id.btn_confirm_register);
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerInformation(view);
            }
        });
    }

    /**
     * Device Set page
     */
    private void registerInformation(View view)
    {
        TextView usename = (TextView) findViewById(R.id.register_username);
        TextView phtone = (TextView) findViewById(R.id.register_phoneNumber);
        TextView email = (TextView) findViewById(R.id.register_EmailAddress);
        TextView password = (TextView) findViewById(R.id.register_textPassword);
        TextView register_area = (TextView) findViewById(R.id.register_area);

    }
}
