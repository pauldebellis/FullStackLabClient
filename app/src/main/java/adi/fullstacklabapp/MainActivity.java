package adi.fullstacklabapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "http://full-stack-lab-server.herokuapp.com/api/koans";
    private TextView koans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        koans = (TextView) findViewById(R.id.koans);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadTask().execute(URL);
        } else {
            Toast.makeText(this, "check network", Toast.LENGTH_LONG).show();
        }
    }
    private String downloadUrl(String myUrl)throws IOException, JSONException{
        InputStream is = null;
        try {
            java.net.URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();
            String contentAsString = readIt(is);
            String processedJson = parseJson(contentAsString);
            return processedJson;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String parseJson(String contentAsString) throws JSONException {
        StringBuilder koanList = new StringBuilder();
        JSONArray array = new JSONArray(contentAsString);
//        JSONArray array = object.getJSONArray("koans");
        for (int i=0; i<array.length(); i++){
            JSONObject koan = array.getJSONObject(i);
            String title = koan.getString("title");
            String text = koan.getString("text");
            koanList.append(title);
            koanList.append("\n"+text+"\n"+"\n");
        }
        return koanList.toString();
    }
    public String readIt(InputStream stream) throws IOException{
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String read;
        while ((read = br.readLine()) != null) {
            sb.append(read);
        }
        return sb.toString();
    }
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.d("IOException", e.getMessage());
                return "Unable to retrieve web page. URL may be invalid.";
            } catch (JSONException e) {
                return "JSON parsing issue: " + e.getMessage();
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            koans.setText(result);
        }
    }
}
