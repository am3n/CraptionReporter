package ir.am3n.craptionreporter.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ir.am3n.craptionreporter.R;
import ir.am3n.craptionreporter.ui.LogMessageActivity;
import ir.am3n.craptionreporter.utils.FileUtils;
import androidx.recyclerview.widget.RecyclerView;

public class CraptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<File> crashFileList;

    public CraptionAdapter(Context context, ArrayList<File> allCrashLogs) {
        this.context = context;
        crashFileList = allCrashLogs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_item, null);
        return new CrashLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((CrashLogViewHolder) holder).setUpViewHolder(context, crashFileList.get(position));
    }

    @Override
    public int getItemCount() {
        return crashFileList.size();
    }


    public void updateList(ArrayList<File> allCrashLogs) {
        crashFileList = allCrashLogs;
        notifyDataSetChanged();
    }


    private class CrashLogViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMsg, messageLogTime;

        CrashLogViewHolder(View itemView) {
            super(itemView);
            messageLogTime = (TextView) itemView.findViewById(R.id.messageLogTime);
            textViewMsg = (TextView) itemView.findViewById(R.id.textViewMsg);
        }

        void setUpViewHolder(final Context context, final File file) {
            final String filePath = file.getAbsolutePath();
            messageLogTime.setText(file.getName().replaceAll("[a-zA-Z_.]", ""));
            textViewMsg.setText(FileUtils.readFirstLineFromFile(new File(filePath)));

            textViewMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, LogMessageActivity.class);
                    intent.putExtra("LogMessage", filePath);
                    context.startActivity(intent);
                }
            });
        }
    }
}