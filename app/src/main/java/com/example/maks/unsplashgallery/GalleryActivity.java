package com.example.maks.unsplashgallery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maks.unsplashgallery.Adapter.GalleryAdapter;
import com.example.maks.unsplashgallery.Object.DataImage;
import com.example.maks.unsplashgallery.Other.CheckConnection;
import com.example.maks.unsplashgallery.Other.Unsplash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class GalleryActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Spinner spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private GridLayoutManager layoutManager;
    private ProgressBar progressBar_loadNewData;
    private ProgressDialog progressDialog;

    private List<DataImage> mData=null;
    private int mIval = 0;
    private int mLoadLimit = 10;
    private String mOrder_by="latest";
    private boolean mIsLoadNewData=false;
    private boolean mLoading=true;
    private boolean mIsError=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        toolbar=(Toolbar)findViewById(R.id.toolbar);
        spinner=(Spinner)findViewById(R.id.spinner_orderBy);
        ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(GalleryActivity.this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.order_by));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setSelection(1);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.black);

        mSwipeRefreshLayout.setOnRefreshListener(mSwipeUpListener);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Downloading data ...");
        progressDialog.show();

        progressBar_loadNewData=(ProgressBar)findViewById(R.id.progressBar_loadNewData);

        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        mData=new ArrayList<>();
        galleryAdapter=new GalleryAdapter(this,mData);
        layoutManager=new GridLayoutManager(getApplicationContext(),2);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.addOnScrollListener(onScrollListener);
        //getPhotos();
    }

    private int previousTotal = 0;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private RecyclerView.OnScrollListener onScrollListener=new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (dy > 0) {
//                int visibleItemCount = layoutManager.getChildCount();
//                int totalItemCount = layoutManager.getItemCount();
//                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
//                int tmp = layoutManager.findFirstCompletelyVisibleItemPosition();
//                int coutnItem = mData.size();
//                int lastItemPos = layoutManager.findLastVisibleItemPosition();
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (mLoading) {
                    if (totalItemCount > previousTotal) {
                        mLoading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!mLoading
                        && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                        mLoading=true;
                        mIsLoadNewData = true;
                        progressBar_loadNewData.setVisibility(View.VISIBLE);
                    Log.v("MyLog","onScrolled -> getPhotos()");
                    getPhotos();
                    }
                }
        }
    };

    private AdapterView.OnItemSelectedListener onItemSelectedListener=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mOrder_by=spinner.getSelectedItem().toString();
            retry();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void getPhotos(){
        if(!CheckConnection.isNetworkAvailable(this)) {
           // Toast.makeText(this, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
            ifSomethingWrong(getString(R.string.noInternetConnection));
            return;
        }
        Log.v("MyLog","getPhotos()");


        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Unsplash.root+"photos?page="+(mIval==0?1:mIval)+"&per_page="+(mIval+mLoadLimit)+"&order_by="+mOrder_by)
                .get()
                .addHeader("authorization", "Bearer "+Unsplash.token)
                .addHeader("cache-control", "no-cache")
                .build();
        mIval+=mLoadLimit;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call,final IOException e) {
                Log.v("MyLog","getPhotos : onFailure "+ e.getMessage().toString());
                GalleryActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(GalleryActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
                        ifSomethingWrong(e.getMessage().toString()+" Try again ?");
                        return;
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strdata=response.body().string().toString();

                if(response.code()==403) {
                    ifSomethingWrong(strdata);
                    Log.v("MyLog",String.valueOf(response.code()));
                    Log.v("MyLog",strdata);
                    return;
                }
                parseJSONtoList(strdata);
            }
        });

    }

    private void parseJSONtoList(String strdata){
        DataImage dataImageItem=null;
        try{
            JSONArray jsonArray=new JSONArray(strdata);
                for (int i=0;i<jsonArray.length();i++) {

                    JSONObject objects = jsonArray.getJSONObject(i);
                    JSONObject links = objects.getJSONObject("links");

                    String linkdownload = links.getString("download");
                    String id=objects.getString("id");
                    int likes = objects.getInt("likes");
                    String date = objects.getString("updated_at");
                    boolean isLiked = objects.getBoolean("liked_by_user");

                    dataImageItem = new DataImage();
                    dataImageItem.setLinkDownload(linkdownload);
                    dataImageItem.setId(id);
                    dataImageItem.setLikes(likes);
                    dataImageItem.setDate(date);
                    dataImageItem.setIsLiked(isLiked);
                    mData.add(dataImageItem);
                }
            Log.v("MyLog","---- "+String.valueOf(mData.size()));

        }
        catch (JSONException ex){
        Log.v("MyLog", "parseJSONtoList : JSONException "+ex.getMessage().toString());
            GalleryActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ifSomethingWrong("Try again?");
                    return;
                }
            });
    }
        catch (Exception ex){
        Log.v("MyLog", "parseJSONtoList : Exception "+ex.getMessage().toString());
            GalleryActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ifSomethingWrong("Try again?");
                    return;
                }
            });
        }

        checkState();
        showData();
    }

    private void checkState(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mIsLoadNewData)
                    progressBar_loadNewData.setVisibility(View.INVISIBLE);
                else
                    progressDialog.hide();
                // progressBar_loadData.setVisibility(View.INVISIBLE);

                if(mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(false);

                if (mIsError) {
                    recyclerView.setVisibility(View.VISIBLE);
                }

                mIsLoadNewData=false;
            }
        });
    }

    private void showData(){
        try{
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    galleryAdapter.notifyDataSetChanged();
                }
            });
        }
        catch (Exception ex){
            Log.v("MyLog", "showData : Exception "+ex.getMessage().toString());
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void ifSomethingWrong(final String message){
        GalleryActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

        mIsError=true;
        if(!mIsLoadNewData)
        progressDialog.hide();

        AlertDialog.Builder alert = new AlertDialog.Builder(GalleryActivity.this);
        alert.setTitle("Somenthig Wrong"); //Set Alert dialog title here
        alert.setMessage(message); //Message here

        alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                retry();
                dialog.cancel();
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();

            }
        });
    }

    private void retry(){
        progressDialog.show();
        mData.clear();
        recyclerView.getRecycledViewPool().clear();
        recyclerView.stopScroll();
        mLoading=true;
        previousTotal=0;
        mIval = 0;
        mLoadLimit = 10;
        getPhotos();
    }

    private SwipeRefreshLayout.OnRefreshListener mSwipeUpListener=new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mSwipeRefreshLayout.setRefreshing(true);
            retry();
            mSwipeRefreshLayout.setRefreshing(true);
        }
    };

    public void onClickRandom(View v){

        if(v.getId()==R.id.button_random){
            Intent intent=new Intent(this,ImageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("isRandom",true);
            intent.putExtras(bundle);
            this.startActivity(intent);

            //Toast.makeText(this, "Random photo", Toast.LENGTH_SHORT).show();
        }
    }

}
