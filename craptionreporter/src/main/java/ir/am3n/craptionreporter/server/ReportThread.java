package ir.am3n.craptionreporter.server;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.utils.Constants;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.function.BiConsumer;

public class ReportThread extends Thread {

    private JSONObject crashes;
    private Listener listener;
    private boolean uploaded = false;

    public ReportThread(JSONObject crashes, Listener listener) {
        this.crashes = crashes;
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();

        DataOutputStream outputStream = null;
        BufferedReader bufferedReader = null;

        try {

            URL url = new URL(CraptionReporter.getInstance().getRepoterUrl());
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.setDoInput(true);
            conection.setDoOutput(true);
            conection.setConnectTimeout(Constants.SERVER_TIMEOUTS/2);
            conection.setReadTimeout(Constants.SERVER_TIMEOUTS);

            Map<String, String> headers = CraptionReporter.getInstance().getServerHeaders();
            if (headers != null)
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conection.setRequestProperty(entry.getKey(), entry.getValue());
                }

            conection.connect();


            outputStream = new DataOutputStream(conection.getOutputStream());
            outputStream.writeBytes(crashes.toString());
            outputStream.flush();
            outputStream.close();

            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(conection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            bufferedReader.close();
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            uploaded = true;
            onPostExecute(jsonArray);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }

        uploaded = false;
        onPostExecute(new JSONArray());

    }

    private void onPostExecute(JSONArray jsonArray) {
        if (uploaded && jsonArray!=null && listener!=null)
            listener.onUploaded(jsonArray);
        if (!uploaded && listener != null)
            listener.onError();
    }

    public interface Listener {
        void onUploaded(JSONArray response);
        void onError();
    }

}
