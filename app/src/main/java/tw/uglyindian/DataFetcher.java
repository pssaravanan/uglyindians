package tw.uglyindian;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class DataFetcher {

    private static JSONObject getSampleObject() {
        float latitude = 13.08389f, longitude = 80.27f;
        try {
            JSONObject object = new JSONObject();
            JSONArray spots = new JSONArray();
            for (int i=0; i<10;i++)
                spots.put(getSpot(latitude, longitude));
            return object.put("spots", spots);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private static JSONObject getSpot(float latitude, float longitude) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("latitude" , latitude + (Math.random() - 0.5))
                .put("longitude", longitude + Math.random() - 0.5)
                .put("date", new Date().getTime() + (864000000 * (Math.random() - 0.5)))
                .put("fixed", Math.random() > 0.5);
        return object;
    }

    public static void fetchData(DataFetchListener listener){
        if(listener == null)
            return;
        listener.onFetchData(getSampleObject());
    }
}
