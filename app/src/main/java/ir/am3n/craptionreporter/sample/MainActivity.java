package ir.am3n.craptionreporter.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import ir.am3n.craptionreporter.CraptionReporter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*CraptionReporter.exception(new Exception("test 1"), "MainActivity > onCreate()");

        CraptionReporter.exception(new Exception("test 2"));

        CraptionReporter.log("main 1");
        CraptionReporter.log("main 2");
        CraptionReporter.log("main 3");*/

        //new Handler().postDelayed(CraptionReporter::crash, 3000);

        TextView txt = findViewById(R.id.txt);
        TextView btn = findViewById(R.id.btn);

        txt.setOnClickListener(v ->
            CraptionReporter.exception(new Exception(""), "MainAct > txt.click()")
        );
        txt.setOnLongClickListener(v -> {
            CraptionReporter.crash();
            return true;
        });

        btn.setOnClickListener(v ->
            CraptionReporter.log("MainAct - onCreate() > log a string ")
        );

    }

}
