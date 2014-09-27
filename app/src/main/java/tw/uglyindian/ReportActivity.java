package tw.uglyindian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ReportActivity extends Activity {
    EditText etDescription;
    Button btnReport;
    ImageView imgPost;
    final static int cameraData=0;
    Bitmap bmp;
    String encoded,eMail;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        etDescription = (EditText) findViewById(R.id.etDescriptions);
        btnReport = (Button) findViewById(R.id.btnReport);
        imgPost = (ImageView) findViewById(R.id.imgPost);

        imgPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set image
                captureImage();
            }
        });

        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // click to report
                getMail();
                if(bmp!=null)
                decodeBmp();
                new LongOperation().execute("");
            }
        });
    }

    private void getMail() {
        String the_ugly_indian_prefs = "THE_UGLY_INDIAN_PREFS";
        sharedpreferences = getSharedPreferences(the_ugly_indian_prefs, Context.MODE_PRIVATE);
        eMail = sharedpreferences.getString("email","");
    }

    private void decodeBmp() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        encoded= Base64.encodeToString(byteArray, Base64.DEFAULT);
    }


    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,cameraData);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bmp = (Bitmap) extras.get("data");
            imgPost.setImageBitmap(bmp);
        }
    }
    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String desc = etDescription.getText().toString().trim();
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://sheltered-beyond-3165.herokuapp.com/spot_fix");

            try {
                // Add your data
                List<NameValuePair> nameSpotDetails = new ArrayList<NameValuePair>(2);
                nameSpotDetails.add(new BasicNameValuePair("event_name", "clean"));
                nameSpotDetails.add(new BasicNameValuePair("event_description",desc ));
                nameSpotDetails.add(new BasicNameValuePair("image", encoded));
                nameSpotDetails.add(new BasicNameValuePair("initiator", eMail));
                post.setEntity(new UrlEncodedFormEntity(nameSpotDetails));

                // Execute HTTP Post Request
                HttpResponse response = client.execute(post);


            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            Toast.makeText(ReportActivity.this,"Posted Successfully",Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
