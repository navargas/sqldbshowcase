package net.nickvargas.sqldbshowcase;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ServerInfo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ServerInfo#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ServerInfo extends Fragment {


    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param parent parent
     * @return A new instance of fragment NewServerInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static ServerInfo newInstance(Activity parent) {
        ServerInfo fragment = new ServerInfo();
        Bundle args = new Bundle();
        //args.put("parent", parent);
        fragment.setArguments(args);
        return fragment;
    }
    public ServerInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_server_info, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        REST_API.get("dbinfo", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)  {
                String result = response.toString();
                Log.d("httpjObj", result);
                try {
                    TextView service_name = (TextView)activity.findViewById(R.id.serviceName);
                    TextView database_info = (TextView)activity.findViewById(R.id.database);
                    TextView host_info = (TextView)activity.findViewById(R.id.host_info);
                    TextView port_info = (TextView)activity.findViewById(R.id.port_info);
                    TextView url_info = (TextView)activity.findViewById(R.id.url_info);
                    service_name.setText(response.getString("name"));
                    database_info.setText(response.getString("db"));
                    host_info.setText(response.getString("host"));
                    port_info.setText(response.getString("port"));
                    url_info.setText(response.getString("jdbcurl"));
                } catch (JSONException e) {

                }
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                String digest = timeline.toString();
                Log.d("httpArray", digest);
            }

            @Override

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject obj) {
                Log.d("httpFail", e.toString());
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
