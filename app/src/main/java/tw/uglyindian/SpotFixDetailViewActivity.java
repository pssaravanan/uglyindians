package tw.uglyindian;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class SpotFixDetailViewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_fix_detail_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String oid = getIntent().getStringExtra("oid");
        Log.d("UGLYY", oid);
        new LongOperation().execute(oid);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... oids) {
            HttpClient client = new DefaultHttpClient();
            Log.d("UGLY", oids[0].toString());
            HttpGet get = new HttpGet("http://sheltered-beyond-3165.herokuapp.com/spot_fix/"+oids[0]);
            try {
                HttpResponse response = client.execute(get);
                Log.d("UGLY", response.getEntity().toString());
            } catch (IOException e) {

            }
            return "success";
        }
    }
}

