package ir.am3n.craptionreporter.server;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.utils.Constants;

import static ir.am3n.needtool.ContextHelperKt.device;
import static ir.am3n.needtool.ContextHelperKt.isDebug;

public class HelloThread extends Thread {

    private Listener listener;

    public HelloThread(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();

        JSONObject client = new JSONObject();
        try {
            client.put("uid", CraptionReporter.getInstance().getUid());
            client.put("identification", CraptionReporter.getInstance().getUserIdentification());
            client.put("extraInfo", CraptionReporter.getInstance().getExtraInfo());
            client.put("debug", isDebug(CraptionReporter.getInstance().getContext()));
            Map<String, String> device = device(CraptionReporter.getInstance().getContext());
            for(Map.Entry<String, String> entry : device.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                client.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DataOutputStream outputStream = null;
        BufferedReader bufferedReader = null;

        try {

            URL url = new URL(CraptionReporter.getInstance().getHelloUrl());
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
            outputStream.writeBytes(client.toString());
            outputStream.flush();
            outputStream.close();

            StringBuilder stringBuilder = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(conection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();

            onPostExecute(stringBuilder.toString());

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

    }

    private void onPostExecute(String uid) {
        if (uid!=null && uid.length()!=0 && listener!=null)
            listener.onSuccess(uid);
        if (listener != null)
            listener.onError();
    }

    public interface Listener {
        void onSuccess(String uid);
        void onError();
    }

}
