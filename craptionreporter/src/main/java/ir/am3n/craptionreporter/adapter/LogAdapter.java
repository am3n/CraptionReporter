package ir.am3n.craptionreporter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import ir.am3n.craptionreporter.R;
import androidx.recyclerview.widget.RecyclerView;

public class LogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<String> logList;

    public LogAdapter(Context context, ArrayList<String> allLogs) {
        this.context = context;
        logList = allLogs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itemlist_log, null);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((LogViewHolder) holder).setUpViewHolder(context, logList.get(position));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }


    public void updateList(ArrayList<String> allLogs) {
        logList = allLogs;
        notifyDataSetChanged();
    }


    private class LogViewHolder extends RecyclerView.ViewHolder {
        private TextView log;

        LogViewHolder(View itemView) {
            super(itemView);
            log = itemView.findViewById(R.id.log);
        }

        void setUpViewHolder(final Context context, final String message) {
            log.setText(message);
        }
    }
}
