package com.athreyaanand.earthy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rv;

    Button tryAgain;

    EarthAdapter earthAdapter;

    LinearLayoutManager mLayoutManager;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    public static int navItemIndex = 0;

    private List<EarthPackage> earthyList;

    View contentUnavailable;
    SwipeRefreshLayout refreshLayout;
    FetchTask fetchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // initializing navigation menu
        setUpNavigationView();

        earthyList = new ArrayList<>();

        contentUnavailable = findViewById(R.id.content_unavailable);

        rv = (RecyclerView) findViewById(R.id.stories);
        rv.setHasFixedSize(true);
        //rv.addItemDecoration(new DividerItemDecoration(this));

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refreshLayout.setRefreshing(true);

        /**
         * Updating parsed JSON data into ListView
         * */
        earthAdapter = new EarthAdapter(earthyList, getApplicationContext());
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(earthAdapter);
        rv.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), rv, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(MainActivity.this, "Clicked at position "+position+"!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Toast.makeText(MainActivity.this, "Held at position "+position+"!", Toast.LENGTH_SHORT).show();;
                    }
                })
        );

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) //check for scroll down
                {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                    if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                        fetchTask.fetch();
                    }
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                fetchTask.refresh();
            }
        });


        tryAgain = (Button) findViewById(R.id.try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshLayout.setRefreshing(true);
                fetchTask.refresh();
            }
        });

        fetchTask = new FetchTask();
        fetchTask.execute();
    }

    private void showContentUnavailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("in hide contentU");
                contentUnavailable.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            }
        });
    }

    private void hideContentUnavailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("in hide contentU");
                contentUnavailable.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Check to see which item was being clicked and perform appropriate action
                switch (item.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_hot:
                        navItemIndex = 0;
                        fetchTask.setSortType(SortType.HOT);
                        break;
                    case R.id.nav_new:
                        navItemIndex = 1;
                        fetchTask.setSortType(SortType.NEW);
                        break;
                    case R.id.nav_top:
                        navItemIndex = 2;
                        fetchTask.setSortType(SortType.TOP);
                        break;
                    case R.id.nav_noads:
                        navItemIndex = 3;
                        break;
                    case R.id.nav_about_us: navItemIndex = 4; break;
                    default: navItemIndex = 4;
                }

                fetchTask.refresh();
                drawer.closeDrawers();
                invalidateOptionsMenu();

                //Checking if the item is in checked state or not, if not make it in checked state
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                item.setChecked(true);

                /*if (navItemIndex==3){
                    PurchaseItem(SKU_NOADS);
                }else if (navItemIndex==4){
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                }
*/
                return true;
            }
        });


        /*ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                if (SKUOwnershipHashMap.containsKey(SKU_NOADS) ? SKUOwnershipHashMap.get(SKU_NOADS) : false){
                    Menu nav_Menu = navigationView.getMenu();
                    nav_Menu.findItem(R.id.nav_noads).setVisible(false);
                }
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (SKUOwnershipHashMap.containsKey(SKU_NOADS) ? SKUOwnershipHashMap.get(SKU_NOADS) : false){
                    Menu nav_Menu = navigationView.getMenu();
                    nav_Menu.findItem(R.id.nav_noads).setVisible(false);
                }
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();*/
    }

    private class FetchTask extends AsyncTask<Void, Void, Void> {

        private List<EarthPackage> earthPackageList;
        private List<String> lastFetchedIDs;

        private OkHttpClient client;

        private SortType sortType;

        boolean isFirstFetch;
        boolean wasFetchSuccessful;

        boolean isUserAdFree;

        private String lastThingID;

        //URLs to get JSON
        private final String REDDIT_STATIC_URL = "https://static.reddit.com/";
        private final String MINDLESS_ROOT = "https://www.reddit.com/r/EarthPorn/";
        private final String _HOT = MINDLESS_ROOT + "hot/";
        private final String _NEW = MINDLESS_ROOT + "new/";
        private final String _TOP = MINDLESS_ROOT + "top/";
        private final String _ITERATOR = "?count=25&after=";
        private final String _JSON = ".json";

        private int page = 0;
        private final int ARTICLES_PER_PAGE_COUNT = 25;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            isFirstFetch = true;
            earthPackageList = new ArrayList<>();
            lastFetchedIDs = new ArrayList<>();
            client = new OkHttpClient();
            sortType = SortType.HOT;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //TODO: remember to add content unavailable screen if no connectivity
            /*if (canConnectToSource()) {
                hideContentUnavailable();
                fetch();
            } else
                showContentUnavailable();*/
            return null;
        }

        private boolean canConnectToSource() {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(REDDIT_STATIC_URL)
                        .build();

                Response response = client.newCall(request).execute();

                return (response.body().string().equals("404 Not Found"));
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            updateList();
            updateView();
        }

        private void updateList() {
            earthyList.clear();
            for (EarthPackage p : earthPackageList)
                earthyList.add(p);
        }

        private void updateView() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                    earthAdapter.notifyDataSetChanged();
                }
            });
        }

        public void refresh() {
            isFirstFetch = true;
            lastFetchedIDs.clear();
            destroyStories();
            fetch();
            rv.scrollToPosition(0);
        }

        public void fetch() {

            if (isFirstFetch) {
                isFirstFetch = false;
                page++;
                fetchStories(buildURL(sortType));
            } else {
                //only fetch if not fetched already
                if (!lastFetchedIDs.contains(lastThingID)) {
                    page++;
                    lastFetchedIDs.add(lastThingID);
                    fetchStories(buildURL(sortType, lastThingID));
                } else
                    return;
            }
        }

        private void fetchStories(String url) {
            clientCall(url);
        }

        private void buildStories(JSONObject json) {

            List<EarthPackage> newEarthPackages = new ArrayList<>();

            try {
                JSONObject data = json.getJSONObject("data");
                JSONArray children = data.getJSONArray("children");

                System.out.println("# OF STORIES FOUND: " + children.length());
                // looping through All stories
                for (int i = 0; i < children.length(); i++) {
                    JSONObject childData = children.getJSONObject(i);

                    JSONObject elData = childData.getJSONObject("data");

                    String title = elData.getString("title");
                    String domain = elData.getString("domain");
                    String url = elData.getString("url");
                    long utc = elData.getLong("created_utc");

                    String thumbnail;
                    if (elData.has("preview")) {
                        JSONObject preview = elData.getJSONObject("preview");
                        JSONArray images = preview.getJSONArray("images");
                        JSONObject d = images.getJSONObject(0);
                        JSONObject source = d.getJSONObject("source");
                        thumbnail = source.getString("url");
                    } else {
                        thumbnail = "http://placehold.it/100x100";
                    }

                    //grab last id
                    if (i == 24) {
                        lastThingID = elData.getString("name");
                    }

                    // tmp hash map for single contact
                    EarthPackage cell = new EarthPackage(title, domain, url, thumbnail, utc);
                    System.out.println((i + earthyList.size()) + ":" + cell.toString());

                    // adding to cache list
                    newEarthPackages.add(cell);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            earthPackageList.addAll(newEarthPackages);
        }

        private void removeDups() {
            List<String> urls = new ArrayList<>();
            for (int i = (page * ARTICLES_PER_PAGE_COUNT); i < earthPackageList.size(); i++) {
                EarthPackage n = (EarthPackage) earthPackageList.get(i);
                if (urls.contains(n.getUrl())) {
                    System.out.println("DUPE ARTICLE FOUND (" + n.getTitle().substring(0,7) + "...) and removed");
                    //remove
                    earthPackageList.remove(i);
                    i--;
                } else
                    urls.add(n.getUrl());
            }
        }

        private void clientCall(String url) {
            System.out.println("GEETIN INFO FROM URL: " + url);
            Request request = new Request.Builder().url(url).build();
            Callback callback = new Callback() {


                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    wasFetchSuccessful = false;
                    //showContentUnavailable();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //hideContentUnavailable();
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    //hideContentUnavailable();

                    JSONObject json = null;
                    try {
                        json = new JSONObject(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    buildStories(json);
                    updateList();
                    updateView();
                }
            };

            client.newCall(request).enqueue(callback);
        }

        public void destroyStories() {
            earthyList.clear();
            earthPackageList.clear();
        }

        private String buildURL(SortType type) {
            switch (type) {
                case NEW:
                    return _NEW + _JSON;
                case TOP:
                    return _TOP + _JSON;
                case HOT: //HOT is Default
                default:
                    return _HOT + _JSON;
            }
        }

        private String buildURL(SortType type, String tid) {
            switch (type) {
                case NEW:
                    return _NEW + _JSON + _ITERATOR + tid;
                case TOP:
                    return _TOP + _JSON + _ITERATOR + tid;
                case HOT: //HOT is Default
                default:
                    return _HOT + _JSON + _ITERATOR + tid;
            }
        }

        public void setSortType(SortType sortType) {
            this.sortType = sortType;
        }
    }
}