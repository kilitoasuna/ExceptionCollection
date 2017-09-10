package com.example.songxh.exceptioncollection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.tt);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    /**
     * 异常捕获测试
     */
    private void test(){
        String[] arry = new String[3];
        arry[0] = "12";
        arry[1] = "12";
        arry[2] = "12";
        System.out.println("caught exception----->" + arry[3]);
    }
}
