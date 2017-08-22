package com.motiontech.photos;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private SpinKitView topSpinner;
    private SpinKitView bottomSpinner;

    private ImageButton retry;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private int current_page = 1;
    private int total_pages = 100;
    private boolean isLoading = false;
    private int layout = 2;

    private GridLayoutManager layoutManager;
    private ArrayList<Image> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topSpinner = (SpinKitView) findViewById(R.id.mainSpinner);
        bottomSpinner = (SpinKitView) findViewById(R.id.bottomSpinner);

        retry = (ImageButton) findViewById(R.id.retryBtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this, layout);
        recyclerView.setLayoutManager(layoutManager);

        new LoadImagesTask().execute(current_page);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadImagesTask().execute(current_page);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0 && !isLoading) { //check for scroll down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        System.out.println("Last Item on " + current_page + " of " + total_pages +". Lets fetch more images! #ScrollListener");
                        isLoading = true;

                        if (current_page < total_pages) {
                            new LoadImagesTask().execute(current_page);
                        } else {
                            System.out.println("Last Item Wow! Looks like that's everything! #ScrollListener");
                            showAlertWith("Wow! Looks like that's everything!");
                        }
                    }
                }
            }
        });
    }

    private void showAlertWith(String text) {
        TSnackbar.make(findViewById(android.R.id.content), text, TSnackbar.LENGTH_LONG).show();
    }

    private class LoadImagesTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected void onPreExecute() {
            isLoading = true;
            retry.setVisibility(View.GONE);

            if (recyclerView.getAdapter() == null) {
                topSpinner.setVisibility(View.VISIBLE);
            } else {
                bottomSpinner.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Integer doInBackground(Integer... param) {

            int error = 0;

            String page = "&page=" + param[0];
            String key = "&consumer_key=" + getResources().getString(R.string.consumer_key);
            //String sort = "&sort=created_at";
            String size = "&image_size=3";
            //String direction = "&sort_direction=asc";

            String endpoint = "https://api.500px.com/v1/photos?feature=popular" + size + page + key;

            Cache cache = new DiskBasedCache(MainActivity.this.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            RequestQueue queue = new RequestQueue(cache, network);
            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest request = new StringRequest(Request.Method.GET, endpoint, future, future);

            queue.add(request);
            queue.start();

            try {
                System.out.println("Into the future!");
                String response = future.get(5, TimeUnit.SECONDS).toString();
                System.out.println("Response is: " + response);

                adapter = new ImageAdapter(MainActivity.this, handleResponse(response));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                error = 100;
            }
            catch (ExecutionException e) {
                e.printStackTrace();
                error = 200;
            } catch (TimeoutException e) {
                e.printStackTrace();
                error = 300;
            }

            return error;
        }

        @Override
        protected void onPostExecute(Integer error) {
            topSpinner.setVisibility(View.GONE);
            bottomSpinner.setVisibility(View.GONE);

            isLoading = false;
            current_page++;

            if (error == 0){
                if (recyclerView.getAdapter() != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    recyclerView.setAdapter(adapter);
                }
            }
            else {
                retry.setVisibility(View.VISIBLE);

                if (error == 100) {
                    showAlertWith("Task interrupted while loading images");
                } else if (error == 200) {
                    showAlertWith("Execution error while loading images");
                } else if (error == 300) {
                    showAlertWith("Timeout error while loading images");
                } else {
                    showAlertWith("No internet connection");
                }
            }
        }
    }

    //only get the relevant data
    private ArrayList<Image> handleResponse(String response) {
        try {
            JSONObject object = new JSONObject(response);

            total_pages = object.getInt("total_pages");
            JSONArray photos = object.getJSONArray("photos");

            for (int i = 0; i < photos.length(); i++) {
                JSONObject current = photos.getJSONObject(i);

                Image image = new Image(current.getString("image_url"));
                image.setScore(current.getString("rating"));
                image.setName(current.getString("name"));

                images.add(image);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return images;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (id == R.id.app_bar_about) {
            final SpannableString s = new SpannableString("Photos is a gallery app plugging into 500px." +
                    "\n" +
                    "Built by Manenga Mungandi " +
                    "\n" +
                    "\n" +
                    "For more info " + "www.manengamungandi.com");
            Linkify.addLinks(s, Linkify.WEB_URLS);
            builder.setMessage(s).setTitle("About Photos").setPositiveButton(android.R.string.ok, null);

        }

        AlertDialog dialog = builder.create();
        dialog.show();

        //return true;
        return super.onOptionsItemSelected(item);
    }
}
