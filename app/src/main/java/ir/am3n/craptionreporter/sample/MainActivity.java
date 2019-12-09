package ir.am3n.craptionreporter.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ir.am3n.craptionreporter.CraptionReporter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        CraptionReporter.exception(new Exception("test 1"), "MainActivity > onCreate()");

        CraptionReporter.exception(new Exception("test 2"));

    }

}
