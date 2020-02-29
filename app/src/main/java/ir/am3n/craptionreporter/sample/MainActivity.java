package ir.am3n.craptionreporter.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.server.Reporter;
import ir.am3n.craptionreporter.server.UploadCrashesAsyncTask;

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
        txt.setOnClickListener(v ->
                CraptionReporter.crash()
        );
        txt.setOnLongClickListener(v -> {
            CraptionReporter.exception(new Exception(""), "MainAct > txt.longClick()");
            return true;
        });

    }

}
