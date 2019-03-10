package ir.am3n.craptionreporter.server;

import ir.am3n.craptionreporter.CraptionReporter;
import ir.am3n.craptionreporter.utils.Constants;
import android.os.AsyncTask;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadMappingFileAsyncTask extends AsyncTask<Void, Void, Void> {

    private Listener listener;
    private boolean downloaded = false;

    public DownloadMappingFileAsyncTask(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {

            URL url = new URL(CraptionReporter.getInstance().getMappingFileUrl());
            URLConnection conection = url.openConnection();
            conection.setConnectTimeout(Constants.SERVER_TIMEOUTS);
            conection.setReadTimeout(Constants.SERVER_TIMEOUTS);
            conection.connect();

            inputStream = new BufferedInputStream(conection.getInputStream(), 8192);

            File mappingFile = new File(CraptionReporter.getInstance().getRetraceMappingFilePath());
            if (mappingFile.exists())
                mappingFile.delete();
            mappingFile.createNewFile();
            outputStream = new FileOutputStream(mappingFile);

            byte[] data = new byte[4096];
            while (true) {
                int read = inputStream.read(data);
                if (read == -1) break;
                outputStream.write(data, 0, read);
            }
            outputStream.flush();

            downloaded = true;
            return null;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        downloaded = false;
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!downloaded && listener != null)
            listener.onError();
    }

    interface Listener {
        void onError();
    }

}