package com.example.maks.unsplashgallery.Adapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.maks.unsplashgallery.GalleryActivity;
import com.example.maks.unsplashgallery.ImageActivity;
import com.example.maks.unsplashgallery.Object.DataImage;
import com.example.maks.unsplashgallery.Other.CheckConnection;
import com.example.maks.unsplashgallery.Other.Unsplash;
import com.example.maks.unsplashgallery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zinch on 06.09.2017.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.Holder> {

    private List<DataImage> mData;
    private GalleryActivity mActivity;

    public GalleryAdapter(GalleryActivity activity, List<DataImage> data) {
        this.mActivity=activity;
        this.mData = data;
        Log.v("MyLog",String.valueOf(data.size()));
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_rv,parent,false);
        Holder tmpHolder=new Holder(itemView);

        return tmpHolder;
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
    if(mData.size()==0||mData==null)return;
        final DataImage item=mData.get(position);
        int width =mActivity.getWindowManager().getDefaultDisplay().getWidth();
        holder.imageView_image.getLayoutParams().width=width/2;
                Glide.with(mActivity)
                .load(item.getLinkDownload())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar_loadImage.setVisibility(View.GONE);
                        holder.imageView_image.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar_loadImage.setVisibility(View.GONE);
                        holder.imageView_image.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .override((width/2),(width/2))
                .centerCrop()
                .crossFade(300)
                .placeholder(R.color.black)
                .into(holder.imageView_image);

        holder.textView_likes.setText(item.getLikes().toString());
        holder.textView_date.setText(item.getDate().toString());
        if (item.getIsLiked())
            holder.button_like.setBackgroundResource(R.drawable.like);
        else
            holder.button_like.setBackgroundResource(R.drawable.unlike);

        holder.button_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!CheckConnection.isNetworkAvailable(mActivity)) {
                      Toast.makeText(mActivity, R.string.noInternetConnection, Toast.LENGTH_SHORT).show();
                    return;
                }
                item.setIsLiked(!item.getIsLiked());

                if (item.getIsLiked())
                    likePhoto(item.getId(), holder);
                else
                    unlikePhoto(item.getId(), holder);

            }
        });

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(mActivity,ImageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isRandom",false);
                bundle.putString("idPhoto", item.getId());
                intent.putExtras(bundle);
                mActivity.startActivity(intent);

                //Toast.makeText(mActivity, "Click on image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void likePhoto(String id, final Holder hld){

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(null, new byte[]{});

        Request request = new Request.Builder()
                .url(Unsplash.root+"photos/"+id+"/like")
                .method("POST",body)
                .addHeader("authorization", "Bearer 6f17fc2dd20c4af4c1c56ef81c85a174c337db097f4f1fc63df16e89697c86e6")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MyLog","likePhoto : onFailure "+ e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String strdata=response.body().string().toString();
                if(response.code()==403) {
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mActivity, strdata, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });

                    Log.d("MyLog",String.valueOf(response.code()));
                    Log.d("MyLog",strdata);
                    return;
                }
                parseJSON_likeUnlike(strdata,hld);
            }
        });
    }
    private void parseJSON_likeUnlike(String strdata, final Holder hld){

        try{
            JSONObject jsonObject=new JSONObject(strdata);
            JSONObject photo=jsonObject.getJSONObject("photo");

            final String likes=photo.getString("likes");
            final boolean liked_by_user=photo.getBoolean("liked_by_user");

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (liked_by_user)
                        hld.button_like.setBackgroundResource(R.drawable.like);
                    else
                        hld.button_like.setBackgroundResource(R.drawable.unlike);
                    hld.textView_likes.setText(likes);
                }
            });

        }
        catch (JSONException ex){
            Log.d("MyLog", "parseJSON_likeUnlike : JSONException "+ex.getMessage().toString());
        }
        catch (Exception ex){
            Log.d("MyLog", "parseJSON_likeUnlike : Exception "+ex.getMessage().toString());
        }



    }
    private void unlikePhoto(String id,final Holder hld ){

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Unsplash.root+"photos/"+id+"/like")
                .delete(null)
                .addHeader("authorization", "Bearer 6f17fc2dd20c4af4c1c56ef81c85a174c337db097f4f1fc63df16e89697c86e6")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MyLog","unlikePhoto : onFailure "+ e.getMessage().toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String strdata=response.body().string().toString();
                if(response.code()==403) {
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mActivity, strdata, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });

                    Log.d("MyLog",String.valueOf(response.code()));
                    Log.d("MyLog",strdata);
                    return;
                }
                parseJSON_likeUnlike(strdata,hld);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    public void setData(List<DataImage> data) {
        this.mData = data;
    }

    public class Holder extends RecyclerView.ViewHolder{
        public LinearLayout linearLayout;
        public ImageView imageView_image;
        public Button button_like;
        public TextView textView_likes;
        public TextView textView_date;
        public ProgressBar progressBar_loadImage;

        public Holder(View itemView){
            super(itemView);
            imageView_image=(ImageView)itemView.findViewById(R.id.imageView_image);
            button_like=(Button)itemView.findViewById(R.id.button_like);
            textView_likes=(TextView)itemView.findViewById(R.id.textView_likes);
            textView_date=(TextView)itemView.findViewById(R.id.textView_date);
            linearLayout=(LinearLayout)itemView.findViewById(R.id.linearLayout);
            progressBar_loadImage=(ProgressBar)itemView.findViewById(R.id.progressBar_loadImage);
        }

    }
}
