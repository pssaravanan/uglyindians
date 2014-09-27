package tw.uglyindian;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataFetcher {

    public static void fetchData(DataFetchListener listener) {
        String jsonString = getJSON("http://sheltered-beyond-3165.herokuapp.com/spot_fix");

        JSONArray jsonObject = toJson(jsonString);
        listener.onFetchData(jsonObject);
    }

    private static JSONArray toJson(String jsonString) {
        JSONArray jsonArray = null;

        try {
            jsonArray = new JSONArray(jsonString);
            JSONArray jsonObjects = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObjects.put(new JSONObject(jsonArray.get(0).toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    private static String getJSON(String address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        httpGet.setHeader("Content-type", "application/json");

        try {
            HttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream content = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}