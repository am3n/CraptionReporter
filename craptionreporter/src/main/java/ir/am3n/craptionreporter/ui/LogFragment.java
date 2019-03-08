package ir.am3n.craptionreporter.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import ir.am3n.craptionreporter.CrashReporter;
import ir.am3n.craptionreporter.R;
import ir.am3n.craptionreporter.adapter.LogAdapter;
import ir.am3n.craptionreporter.utils.CrashUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogFragment extends Fragment {

    private RecyclerView recyclerView;
    private LogAdapter logAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.log, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdapter(getActivity(), recyclerView);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logAdapter.updateList(getAllLoges());
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void loadAdapter(Context context, RecyclerView crashRecyclerView) {

        logAdapter = new LogAdapter(context, getAllLoges());
        crashRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        crashRecyclerView.setAdapter(logAdapter);
    }

    public void clearLog() {
        if (logAdapter != null) {
            logAdapter.updateList(getAllLoges());
        }
    }


    private ArrayList<String> getAllLoges() {
        String directoryPath;
        String logReportPath = CrashReporter.getInstance().getLogReportPath();

        if (TextUtils.isEmpty(logReportPath)) {
            directoryPath = CrashUtil.getDefaultLogPath();
        } else {
            directoryPath = logReportPath;
        }

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RuntimeException("The path provided doesn't exists : " + directoryPath);
        }

        ArrayList<String> listOfLogs = new ArrayList<>();
        String filename = "logReports.txt";
        File file = new File(directoryPath + File.separator + filename);

        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));

            String line;
            while ((line = reader.readLine()) != null) {
                listOfLogs.add(line);
            }
            reader.close();
            fin.close();

            Collections.sort(listOfLogs, Collections.reverseOrder());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listOfLogs;
    }
}
