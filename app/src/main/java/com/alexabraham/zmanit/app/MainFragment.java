package com.alexabraham.zmanit.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    private static final String FILE_NAME = "zmanimData.txt";

    private static final String TAG = "ZmanIt";

    ZmanimListAdapter mAdapter;

    Map<String, Object> zmanimMap;

    boolean usTime = true;

    SwipeRefreshLayout refreshView;
    ListView zmanimListView;

    TextView footer;
    TextView mTitleView;
    TextView mDateView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ZmanimListAdapter(this, getActivity().getApplicationContext());

        zmanimMap = new LinkedHashMap<String, Object>();





        refreshView = (SwipeRefreshLayout) getView().findViewById(R.id.refreshView);
        refreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "Refreshing zmanim with SwipeRefresh");
                getZmanim();
            }
        });
        refreshView.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        zmanimListView = (ListView) getView().findViewById(R.id.zmanimListView);
        zmanimListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (zmanimListView == null || zmanimListView.getChildCount() == 0) ?
                                0 : zmanimListView.getChildAt(0).getTop();
                refreshView.setEnabled(topRowVerticalPosition >= 0);
            }
        });



        mTitleView = (TextView) getView().findViewById(R.id.titleView);
        mDateView = (TextView) getView().findViewById(R.id.dateView);

        footer = (TextView) getView().findViewById(R.id.footerView);

        assert footer != null;
        footer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "Entered footerView.OnClickListener.onClick()");
                getZmanim();
            }
        });

        zmanimListView.setAdapter(mAdapter);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }



    public void updateList(){
        if(zmanimMap.size() != 0) {
            mAdapter.clear();
            for (String value : zmanimMap.keySet()) {
                if (!value.equals("mTitleView") && !value.equals("mDateView") && !value.equals("elevation")) {
                    mAdapter.add(new ZmanItem(value, (Date) zmanimMap.get(value)));

                } else if (value.equals("mTitleView")) {
                    mTitleView.setText((String) zmanimMap.get(value));
                } else if (value.equals("mDateView")) {
                    mDateView.setText((String) zmanimMap.get(value));
                }

            }
        }
        else{
            getZmanim();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.timeFormat);
    }



    @Override
    public void onPrepareOptionsMenu(Menu menu){
        MenuItem item = menu.findItem(R.id.timeFormat);
        if(!usTime){
            item.setTitle("Switch to US Time");
        }
        else{
            item.setTitle("Switch to Military Time");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.timeFormat:
                if (usTime){
                    usTime = false;
                    item.setTitle("Switch to US Time");
                    updateList();
                }
                else{
                    usTime = true;
                    item.setTitle("Switch to Military Time");
                    updateList();
                }


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load saved ToDoItems, if necessary

        if (mAdapter.getCount() == 0)
            loadItems();

        //Load menu items
        loadMenu();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save ToDoItems

        saveItems();

        //Save menu items
        saveMenu();

    }

    @SuppressWarnings("unused")
    private void dump() {

        for (int i = 0; i < mAdapter.getCount(); i++) {
            String data = ((ZmanItem) mAdapter.getItem(i)).toLog();
            log("Item " + i + ": " + data.replace(ZmanItem.ITEM_SEP, ","));
        }

    }

    // Load stored ToDoItems
    private void loadItems() {
        BufferedReader reader = null;
        try {
            FileInputStream fis = getActivity().openFileInput(FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(fis));

            String header1;
            String header2;

            String title = null;
            String time = null;
            String foo = null;

            if (null != (header1 = reader.readLine())){
                mTitleView.setText(header1);
            }
            if (null != (header2 = reader.readLine())){
                mDateView.setText(header2);
            }

            while (null != (title = reader.readLine())) {
                time = reader.readLine();
                Date date = new Date();
                date.setTime(Long.parseLong(time));
                mAdapter.add(new ZmanItem(title, date));
            }

            if(zmanimMap.size() == 0){
                zmanimMap.put("mTitleView", header1);
                zmanimMap.put("mDateView", header2);
                for(ZmanItem item : mAdapter.getItemsList()){
                    zmanimMap.put(item.getTitle(), item.getTime());
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Save ToDoItems to file
    private void saveItems() {
        PrintWriter writer = null;
        try {
            FileOutputStream fos = getActivity().openFileOutput(FILE_NAME, getActivity().MODE_PRIVATE);
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    fos)));

            writer.println(mTitleView.getText().toString());
            writer.println(mDateView.getText().toString());

            for (int idx = 0; idx < mAdapter.getCount(); idx++) {

                writer.println(mAdapter.getItem(idx));

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }

    private void log(String msg) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, msg);
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public boolean isConnected(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getZmanim() {
        Log.i(TAG, "Entered MainFragment.getZmanim()");
        new GetZmanimMap(MainFragment.this, zmanimMap, isConnected()).execute();
    }

    public void completeSwipeRefresh(){
        if (refreshView.isRefreshing()){
            refreshView.setRefreshing(false);
        }
    }

    private boolean preICS(){
        return (Build.VERSION.SDK_INT < 14);
    }

    private void saveMenu() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("timeFormat", usTime);
        editor.commit();
    }

    private void loadMenu() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        usTime =  sharedPreferences.getBoolean("timeFormat", true);
    }

    public boolean getTimeFormat(){
        return usTime;
    }
}