package com.example.testserver;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import java.util.List;


public class JsonActivity extends Activity {

    private TextView textView;

    private List<Student> list;



    /** Called when the activity is first created. */

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_socket__android);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        textView = (TextView) findViewById(R.id.studentShow);
        String data = null;
        try {
            data = HttpUtils.getData("http://192.168.191.1:8088/testPage.jsp");
            Log.i("data",data);
        } catch (Exception e) {

            e.printStackTrace();

        }
        String result = "";
        list = JsonUtils.parseStudentFromJson(data);

        for (Student s : list) {

            result += "name: " + s.getName() + "   " + "age: " + s.getAge()

                    + "   " + "id: " + s.getId() + "\n";

        }

        textView.setText(result);

    }
}