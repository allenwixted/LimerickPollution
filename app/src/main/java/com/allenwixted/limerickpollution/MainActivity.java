package com.allenwixted.limerickpollution;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    String phoneNo;
    String message;
    String url = "http://erc.epa.ie/real-time-air/www/aqindex/aqih_json.php";

    String[] people = {"Gerald Mitchell", "Emmett O'Brien", "William O'Donnell", "Jerry O'Dea", "Kieran O'Hanlon", "Joe Pond", "Eddie Ryan", "Jerome Scanlan", "Elena Secas", "Michael Sheahan", "John Sheahan", "Kevin Sheahan", "Brigid Teefy", "Adam Teskey"};
    String[] numbers = {"0862618866", "0877998199", "0868381425", "0872549808", "0861532783", "0876101615", "0879139145", "0879979353", "0863607872", "0879163370", "0872079268", "0872926333", "0878430603", "0874577495"};

    TextView text;
    int recipient = 0;
    String airQuality = "";
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
        send = (Button) findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < people.length; i++){
                    recipient = i;
                    Log.e("REC", String.valueOf(recipient));
                    sendSMSMessage();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        DownloadTask task = new DownloadTask();
        task.execute(url);
    }

    protected void sendSMSMessage() {
        Log.e("SMS", "HERE");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(numbers[recipient], null, people[recipient], null, null);
        int counter = recipient + 1;
        Toast.makeText(getApplicationContext(), "SMS sent to " + counter + " officials.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    send.setEnabled(true);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    send.setEnabled(false);
                    return;
                }
            }
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                //Log.i("RESULT", result);

                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result != null){
                //Log.i("AIR CONENT", result);

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray airInfo = (JSONArray) jsonObject.get("aqihsummary");
                    for(int i = 0; i < airInfo.length(); i ++){
                        JSONObject jsonPart = airInfo.getJSONObject(i);
                        String airRegion = jsonPart.getString("aqih-region");
                        //Log.i("AIR REGION", airRegion);
                        airQuality = jsonPart.getString("aqih");

                        if(airRegion.equals("Rural_West")){
                            String manifesto = "Dear Sir/Mam, today's air quality level is '" +  airQuality + "'. Let's see what it is when we start burning rubber.";
                            text.setText(manifesto);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
