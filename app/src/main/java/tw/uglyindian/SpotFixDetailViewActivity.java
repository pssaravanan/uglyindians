package tw.uglyindian;


import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;



import org.json.JSONException;
import org.json.JSONObject;

public class SpotFixDetailViewActivity extends Activity {

    private String spotFixImageUrl;
    private String eventDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_fix_detail_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String oid = getIntent().getStringExtra("oid");
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
        private ProgressDialog dialog;
        @Override
        protected String doInBackground(String... oids) {
            DataFetcher.fetchSpotDetails(oids[0], new DataFetchListener() {
                @Override
                public void onFetchData(JSONObject data) {
                    try {
                        eventDescription = data.getString("event_description");
                        spotFixImageUrl = data.getString("initial_image");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            TextView filename = (TextView)findViewById(R.id.description);
            filename.setText(eventDescription);

                if (dialog.isShowing()) {
                    dialog.dismiss();
           }
            showImage();
        }
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(SpotFixDetailViewActivity.this);
            dialog.setMessage("Fetching details..");
            dialog.show();
        }

        private void showImage() {
            byte[] decodedBytes = Base64.decode(spotFixImageUrl.getBytes(), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            ImageView img;
            img = (ImageView) findViewById(R.id.spotFixImage);
            img.setImageBitmap(bmp);
            img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.5f));
        }
    }
}

