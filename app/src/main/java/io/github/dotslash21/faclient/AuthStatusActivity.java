// Copyright (c) 2019 Arunangshu Biswas
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
// BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
package io.github.dotslash21.faclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AuthStatusActivity extends AppCompatActivity {
    ImageView authStatusImg;
    TextView textView1;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_status);

        this.authStatusImg = findViewById(R.id.authStatusImg);
        this.textView1 = findViewById(R.id.textView1);
        this.textView2 = findViewById(R.id.textView2);

        Intent intent = getIntent();
        String authStatus = intent.getStringExtra("AUTH_STATUS");

        if (authStatus.equals("PASS")) {
            Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
            String name = intent.getStringExtra("NAME");

            this.authStatusImg.setImageResource(R.drawable.tick);
            this.textView1.setText("WELCOME");
            this.textView2.setText(name);
        }
        else {
            Toast.makeText(this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
