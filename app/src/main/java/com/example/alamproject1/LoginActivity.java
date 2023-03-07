package com.example.alamproject1;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login = (Button) findViewById(R.id.loginbutton);
        TextView register = findViewById(R.id.registertextView);
        TextView findID = findViewById(R.id.searchIDtextView);
        TextView changPW = findViewById(R.id.changePWtextView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();  // 타이틀 바 숨기기


        //회원가입 클릭
       register.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
                Intent rintent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(rintent);
           }
       });

       //아이디 찾기 클릭
       findID.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent sintent = new Intent(getApplicationContext(),SearchIDActivity.class);
               startActivity(sintent);
           }
       });

        //비번 변경 클릭
        changPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cintent = new Intent(getApplicationContext(),ChangePWActivity.class);
                startActivity(cintent);
            }
        });

        //로그인 버튼 클릭
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}