package net.nickvargas.sqldbshowcase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class TodoListView extends Fragment implements AbsListView.OnItemClickListener, AbsListView.OnItemLongClickListener {

    private OnFragmentInteractionListener mListener;

    // Probably should be a normal ArrayList
    final SparseArray<Integer> buttonId_serverId= new SparseArray<Integer>();

    private AbsListView mListView;

    private ArrayList<Integer> disabledItems = new ArrayList<Integer>();

    ArrayAdapter<String> adapter;

    public static TodoListView newInstance(Activity parent) {
        TodoListView fragment = new TodoListView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public TodoListView() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter=new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        Log.d("startup", "onCreate done");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fired when view is created
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        Log.d("startup", "onCreateView done");
        return view;
    }

    public void reloadContent() {
        // Pull content from server and update list
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Pulling from Bluemix");
        progress.show();
        REST_API.get("todolist", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                Log.d("todoListObj", result);
                JSONArray items;
                buttonId_serverId.clear();
                adapter.clear();
                String[] listArray = getResources().getStringArray(R.array.listarray);
                try {
                    items = response.getJSONArray("body");
                    for (int i = 0; i < items.length(); i++) {
                        buttonId_serverId.put(i, items.getJSONObject(i).getInt("id"));
                        adapter.add(items.getJSONObject(i).getString("name"));


                    }
                } catch (JSONException e) {

                }
                waitForListView();
                progress.dismiss();

            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                String digest = timeline.toString();
                Log.d("todoListArray", digest);
                progress.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject obj) {
                Log.d("httptodoFail", e.toString());
                progress.dismiss();
            }
        });

    }

    public void waitForListView() {
        // The list view will lag behind changes to adapter, causing missing items
        // Waiting a negligible amount of time will resolve the issue on refresh
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after .3 seconds
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshEnabled();//disableItems(disabledItems);
                    }
                });
            }
        }, 300);
    }

    public void refreshEnabled() {
        // Update the enabled list view items according to 'disabledItems'
        int length = mListView.getChildCount();
        for (int index = 0; index < length; index++) {
            Boolean isEnabled = !disabledItems.contains((Integer) index); // yikes sorry
            mListView.getChildAt(index).setEnabled(isEnabled);
        }
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        reloadContent();
        Log.d("startup", "onAttach done");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // User issues a short tap
        String name = adapter.getItem(position);
        Log.d("onItemClick", "Click: "+ Integer.toString(position)+ " is: " + name);
        getUserAction(position);//deleteDialog(view, position);

    }

    @Override
    public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
        // User holds down a list item
        if (disabledItems.contains(position)) {
            Log.d("Lbefore", disabledItems.toString());
            disabledItems.remove((Integer) position); // cast required to prevent index
            Log.d("LAfter", disabledItems.toString());
        } else {
            disabledItems.add(position);
        }
        refreshEnabled();//disableItems(disabledItems);
        return true;
    }

    public void deleteDialog(View view, final int buttonId) {
        // This is currently unused. It will prompt the user before deleting. TODO
        AlertDialog alertDialog = new AlertDialog.Builder(view.getContext()).create();
        alertDialog.setTitle("Delete Item");
        alertDialog.setMessage("Are you sure you wish to delete this item?");
        alertDialog.setButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Integer id = buttonId_serverId.get(buttonId);
                //Log.d("IDS", buttonId_serverId.toString() + id);
                deleteItem(id);
            }
        });
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.show();
    }

    public String getUserAction(final int position) {
        // The main user input pop up. Prompt user for a modified, or removed item
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Modify");
        final EditText input = new EditText(getActivity());
        input.setText(adapter.getItem(position));
        alert.setView(input);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Log.d("getItemText", "Found String: "+ value);
                if (!value.isEmpty()) {
                    RequestParams param = new RequestParams("id", buttonId_serverId.get(position));
                    param.put("id", buttonId_serverId.get(position));
                    param.put("name", value);
                    Log.d("httpput", "Dispatching: " + param.toString());
                    final ProgressDialog progress = new ProgressDialog(getActivity());
                    progress.setTitle("Loading");
                    progress.setMessage("Sending to Bluemix");
                    progress.show();
                    REST_API.put("todolist", param, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                              JSONObject response) {
                            Log.d("httpput", response.toString());
                            progress.dismiss();
                        }
                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                              JSONArray timeline) {
                            Log.d("httpput", timeline.toString());
                            progress.dismiss();
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              String thing, Throwable e) {
                            Log.e("httpput", e.toString());
                            progress.dismiss();
                            reloadContent();
                            //Log.d("oR", "other Refresh");
                        }
                    });
                }
                // Do something with value!
            }
        });

        alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Integer id = buttonId_serverId.get(position);
                deleteItem(id);
            }
        });

        alert.show();
        return "Unimplemented String Return";
    }

    public void deleteItem(int id) {
        // Send DELETE request to database
        REST_API.delete("todolist", id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String result = response.toString();
                Log.d("httpdelete", result);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                String digest = timeline.toString();
                Log.d("httpdelete", digest);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable e) {
                reloadContent();
                // This should get called because the http response from a delete is empty
            }
        });
    }

    public interface OnFragmentInteractionListener {
        // Not sure if this is even necessary.. So much boilerplate stuff
        public void onFragmentInteraction(String id);
    }

}
