package net.nickvargas.sqldbshowcase; /**
 * Created by navargas on 9/12/14.
 */

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class REST_API {
    private static String BASE_URL = "http://todorest.mybluemix.net/api/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void updateTarget(String target) {
        BASE_URL = target;
    }

    public static void get(String url, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
        Log.d("jsonget", (String) getAbsoluteUrl(url));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params,
                            AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void delete(String url, int id, AsyncHttpResponseHandler responseHandler) {
        url = url + "?id=" + Integer.toString(id);
        Log.d("jsondelete", (String) getAbsoluteUrl(url));
        client.delete(getAbsoluteUrl(url), responseHandler);
    }

    public static void put(String url, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
