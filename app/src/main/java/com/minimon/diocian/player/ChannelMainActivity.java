package com.minimon.diocian.player;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChannelMainActivity extends AppCompatActivity {

    MinimonEpisode minimonEpisode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_main);
        init();
        ContentValues values = new ContentValues();
        values.put("ep_idx","645");
        values.put("quality","his");
        values.put("id",UserInfo.getInstance().getUID());

        minimonEpisode.info(values);
    }

    private void init(){
        minimonEpisode = new MinimonEpisode();
        minimonEpisode.setListener(new MinimonEpisode.MinimonEpisodeListener() {
            @Override
            public void onResponse(JSONObject info) {
                try{
//                    setText(info);
                    temp(info);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

   private void temp(JSONObject info){
       String videoUrl;
       try{
           JSONArray videoArr = (JSONArray)info.getJSONObject("data").getJSONObject("list").getJSONObject("list_mp").get("video");
           JSONObject videoObj = (JSONObject) videoArr.get(0);
           videoUrl = videoObj.getString("playUrl");
       }catch (JSONException e){
           Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
           return;
       }
       Log.d("!!!!!!!!",info.toString());
       VideoPlayerFragment videoFrag = new VideoPlayerFragment();
       videoFrag.setVideoUrl(videoUrl);
       Fragment fragment = videoFrag;
       FragmentTransaction ft = getFragmentManager().beginTransaction();
       ft.replace(R.id.fragment_video, fragment);
       ft.commit();
   }
}
