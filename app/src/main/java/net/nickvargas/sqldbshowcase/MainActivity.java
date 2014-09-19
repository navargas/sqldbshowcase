package net.nickvargas.sqldbshowcase;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity
        implements NavigationDrawer.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawer mNavigationDrawerFragment;
    //private Context mContext;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int currentlySelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mNavigationDrawerFragment = (NavigationDrawer)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        String target = prefs.getString("target", "http://todorest.mybluemix.net/api/");
        Log.d("saved", prefs.getAll().toString());
        REST_API.updateTarget(target);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Fragment myFragment;
        if (position == 0) {
            currentlySelected = 0;
            myFragment = new TodoListView().newInstance(this);
        } else {
            currentlySelected = 1;
            myFragment = new ServerInfo().newInstance(this);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, myFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.my, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("Menu", item.toString() + ": " + Integer.toString(id));
        if (id == R.id.action_target) {
            getTargetText();
            return true;
        } else if (id == R.id.new_item) {
            String result = getItemText();
            Log.d("onOptionsItemSelected", "Found String: "+ result);
            return true;
        } else if (id == R.id.refresh_button) {
            reload();//onNavigationDrawerItemSelected(currentlySelected);
        }
        return super.onOptionsItemSelected(item);
    }

    public void reload() {
        onNavigationDrawerItemSelected(currentlySelected);
    }

    public String getTargetText() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Target App");
        alert.setMessage("Press \"Ok\" to submit");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(".mybluemix.net");

        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Log.d("getTargetText", "Found String: " + value);
                if (!value.isEmpty()) {
                    value = "http://" + value + "/api/";
                    Log.d("SettingAPI", value);
                    SharedPreferences prefs = MainActivity.this.getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putString("target", value);
                    edit.commit();
                    REST_API.updateTarget(value);
                    reload();
                }
                // Do something with value!
            }
        });
        alert.show();
        return "";
    }

    public String getItemText() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New ToDo Item");
        alert.setMessage("Press \"Ok\" to submit");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Log.d("getItemText", "Found String: "+ value);
                if (!value.isEmpty()) {
                    RequestParams param = new RequestParams("name", value);
                    Log.d("001",getApplicationContext().toString());
                    final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                    progress.setTitle("Loading");
                    progress.setMessage("Sending to Bluemix");
                    progress.show();
                    REST_API.post("todolist", param, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                              JSONObject response) {
                            Log.d("httppost", response.toString());
                            progress.dismiss();
                            reload();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                              JSONArray timeline) {
                            progress.dismiss();
                        }

                        @Override

                        public void onFailure(int statusCode, Header[] headers, Throwable e,
                                              JSONObject obj) {
                            progress.dismiss();
                        }
                    });
                }
                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
        return "return string";
    }
}
