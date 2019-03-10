package ir.am3n.craptionreporter.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.R;
import ir.am3n.craptionreporter.adapter.CraptionAdapter;
import ir.am3n.craptionreporter.utils.Constants;
import ir.am3n.craptionreporter.utils.CraptionUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CrashesFragment extends Fragment {

    private CraptionAdapter logAdapter;

    private RecyclerView crashRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.crash_log, container, false);
        crashRecyclerView = view.findViewById(R.id.crashRecyclerView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdapter(getActivity(), crashRecyclerView);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                logAdapter.updateList(getAllCrashes());
                handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void loadAdapter(Context context, RecyclerView crashRecyclerView) {

        logAdapter = new CraptionAdapter(context, getAllCrashes());
        crashRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        crashRecyclerView.setAdapter(logAdapter);
    }

    public void clearLog() {
        if (logAdapter != null) {
            logAdapter.updateList(getAllCrashes());
        }
    }


    private ArrayList<File> getAllCrashes() {
        String directoryPath;
        String crashReportPath = CraptionReporter.getInstance().getCrashReportPath();

        if (TextUtils.isEmpty(crashReportPath)) {
            directoryPath = CraptionUtil.getDefaultCrashPath();
        } else {
            directoryPath = crashReportPath;
        }
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RuntimeException("The path provided doesn't exists : " + directoryPath);
        }
        ArrayList<File> listOfFiles = new ArrayList<>(Arrays.asList(directory.listFiles()));
        for (Iterator<File> iterator = listOfFiles.iterator(); iterator.hasNext(); ) {
            if (iterator.next().getName().contains(Constants.EXCEPTION_SUFFIX)) {
                iterator.remove();
            }
        }
        Collections.sort(listOfFiles, Collections.reverseOrder());
        return listOfFiles;
    }

}
