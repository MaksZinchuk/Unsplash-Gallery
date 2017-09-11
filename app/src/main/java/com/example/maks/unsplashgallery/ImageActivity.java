package com.example.maks.unsplashgallery;

import android.app.ProgressDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.maks.unsplashgallery.Adapter.GalleryAdapter;
import com.example.maks.unsplashgallery.Other.CheckConnection;
import com.example.maks.unsplashgallery.Other.Unsplash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageActivity extends AppCompatActivity {

    //private ProgressBar progressBar_loadImage;

    public Button button_like;
    public TextView textView_likes;
    public TextView textView_date;
    public TextView textView_nameAuthor;
    public TextView textView_bioAuthor;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean mIsLiked=false;
    private boolean mIsRandom=false;
    private String mIdPhoto=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        // progressBar_loadImage=(ProgressBar)findViewById(R.id.progressBar_loadImageItem);
        button_like=(Button)findViewById(R.id.button_likeItem);
        textView_likes=(TextView)findViewById(R.id.textView_likesItem);
        textView_date=(TextView)findViewById(R.id.textView_dateItem);
        textView_nameAuthor=(TextView)findViewById(R.id.textView_nameAuthorItem);
        textView_bioAuthor=(TextView)findViewById(R.id.textView_bioAuthorItem);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeToRefreshImage);
        swipeRefreshLayout.setColorSchemeResources(R.color.black);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);

        Bundle bundle=getIntent().getExtras();
        if (bundle!=null) {
            mIsRandom=bundle.getBoolean("isRandom");
            if (!mIsRandom) {
                mIdPhoto = bundle.getString("idPhoto");
                getPhoto(mIdPhoto);
            }
            else {
                getRandomPhoto();
            }
        }

    }

    private void getPhoto(String id){
        if (id==null)return;
        if(!CheckConnection.isNetworkAvailable(this)) {
             Toast.makeText(this, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Wait please", Toast.LENGTH_SHORT).show();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Unsplash.root+"/photos/"+id)
                .get()
                .addHeader("authorization", "Bearer 6f17fc2dd20c4af4c1c56ef81c85a174c337db097f4f1fc63df16e89697c86e6")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("MyLog","getPhoto : onFailure "+ e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               final String strdata=response.body().string().toString();
                Log.v("MyLog",strdata);

                if(response.code()==403) {
                    ImageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ImageActivity.this, strdata, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                    Log.v("MyLog",String.valueOf(response.code()));
                    Log.v("MyLog",strdata);
                    return;
                }
                parseJSON_photo(strdata);
            }
        });

    }
    private void parseJSON_photo(String strdata){
            if (strdata==null)return;
        try{
            JSONObject jsonObject=new JSONObject(strdata);
            mIdPhoto=jsonObject.getString("id");
            final String likes=jsonObject.getString("likes");
            final boolean liked_by_user=jsonObject.getBoolean("liked_by_user");
            final String date = jsonObject.getString("updated_at");
            mIsLiked=liked_by_user;

            JSONObject user=jsonObject.getJSONObject("user");
            final String name=user.getString("name");
            final String bio=user.getString("bio");

            JSONObject links = jsonObject.getJSONObject("links");
            final String linkdownload = links.getString("download");


            Log.v("MyLog",mIdPhoto+" | "+likes+" | "+liked_by_user+" | "+date+" | "+name+" | "+bio+" | "+linkdownload);

            ImageActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (liked_by_user)
                        button_like.setBackgroundResource(R.drawable.like);
                    else
                        button_like.setBackgroundResource(R.drawable.unlike);
                    textView_likes.setText(likes);
                    textView_date.setText(date);
                    textView_nameAuthor.setText(name);
                    textView_bioAuthor.setText(bio);

                    setPhoto(linkdownload);
                }
            });

        }
        catch (JSONException ex){
            Log.v("MyLog", "parseJSON_photo : JSONException "+ex.getMessage().toString());
        }
        catch (Exception ex){
            Log.v("MyLog", "parseJSON_photo : Exception "+ex.getMessage().toString());
        }
    }
    private void getRandomPhoto(){
        if(!CheckConnection.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Toast.makeText(this, "Wait please", Toast.LENGTH_SHORT).show();

        Request request = new Request.Builder()
                .url(Unsplash.root+"/photos/random")
                .get()
                .addHeader("authorization", "Bearer 6f17fc2dd20c4af4c1c56ef81c85a174c337db097f4f1fc63df16e89697c86e6")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("MyLog","getPhoto : onFailure "+ e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
               final String strdata=response.body().string().toString();
                    if(response.code()==403) {
                    ImageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ImageActivity.this, strdata, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });Log.v("MyLog",String.valueOf(response.code()));
                    Log.v("MyLog",strdata);
                    return;
                }
                parseJSON_photo(strdata);
            }
        });

    }

    private void setPhoto(String link){
        final ImageView imageView_image=(ImageView)findViewById(R.id.imageView_imageItem);
        int width =this.getWindowManager().getDefaultDisplay().getWidth();
        int height =this.getWindowManager().getDefaultDisplay().getHeight();
        imageView_image.getLayoutParams().width=width;
        imageView_image.getLayoutParams().height=height;
        try {
            Glide.with(this)
                    .load(link)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            //progressBar_loadImage.setVisibility(View.GONE);
                            imageView_image.setVisibility(View.VISIBLE);
                            Log.v("MyLog", "setPhoto : onException " + e.getMessage().toString());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            // progressBar_loadImage.setVisibility(View.GONE);
                            imageView_image.setVisibility(View.VISIBLE);
                            Log.v("MyLog", "setPhoto : onResourceReady ");
                            return false;
                        }
                    })
                    .override(width, height)
                    .centerCrop()
                    .crossFade(300)
                    .placeholder(R.color.black)
                    .into(imageView_image);
        }
        catch (Exception ex)
        {
            Log.v("MyLog","setPhoto : Exception: "+ ex.getMessage().toString());
        }

    }

    private void likePhoto(){
        if (mIdPhoto==null)return;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(null, new byte[]{});

        Request request = new Request.Builder()
                .url(Unsplash.root+"photos/"+mIdPhoto+"/like")
                .method("POST",body)
                .addHeader("authorization", "Bearer 6f17fc2dd20c4af4c1c56ef81c85a174c337db097f4f1fc63df16e89697c86e6")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("MyLog","likePhoto : onFailure "+ e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String strdata=response.body().string().toString();
                if(response.code()==403) {
                    ImageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ImageActivity.this, strdata, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                    Log.v("MyLog",String.valueOf(response.code()));
                    Log.v("MyLog",strdata);
                    return;
                }
                parseJSON_likeUnlike(strdata);
            }
        });
    }
    private void parseJSON_likeUnlike(String strdata){

        try{
            JSONObject jsonObject=new JSONObject(strdata);
            JSONObject photo=jsonObject.getJSONObject("photo");

            final String likes=photo.getString("likes");
            final boolean liked_by_user=photo.getBoolean("liked_by_user");

            ImageActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (liked_by_user)
                        button_like.setBackgroundResource(R.drawable.like);
                    else
                        button_like.setBackgroundResource(R.drawable.unlike);
                    textView_likes.setText(likes);
                }
            });

        }
        catch (JSONException ex){
            Log.v("MyLog", "parseJSON_likeUnlike : JSONException "+ex.getMessage().toString());
        }
        catch (Exception ex){
            Log.v("MyLog", "parseJSON_likeUnlike : Exception "+ex.getMessage().toString());
        }



    }
    private void unlikePhoto(){
        if (mIdPhoto==null)return;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Unsplash.root+"photos/"+mIdPhoto+"/like")
                .delete(null)
                .addHeader("authorization", "Bearer 6f17fc2dd20c4af4c1c56ef81c85a174c337db097f4f1fc63df16e89697c86e6")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("MyLog","unlikePhoto : onFailure "+ e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String strdata=response.body().string().toString();
                if(response.code()==403) {
                    ImageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ImageActivity.this, strdata, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                    Log.v("MyLog",String.valueOf(response.code()));
                    Log.v("MyLog",strdata);
                    return;
                }
                parseJSON_likeUnlike(strdata);
            }
        });
    }

    public void onClickLike(View v){
        if (v.getId()==R.id.button_likeItem){
            if(!CheckConnection.isNetworkAvailable(this)) {
                Toast.makeText(this, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
                return;
            }
            mIsLiked=!mIsLiked;
            if (mIsLiked)
                likePhoto();
            else
                unlikePhoto();
        }
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener=new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            swipeRefreshLayout.setRefreshing(true);
            if (mIsRandom)
                getRandomPhoto();
            else
                getPhoto(mIdPhoto);
            swipeRefreshLayout.setRefreshing(false);
        }
    };


}
