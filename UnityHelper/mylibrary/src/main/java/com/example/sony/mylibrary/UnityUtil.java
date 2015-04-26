package com.example.sony.mylibrary;

/**
 * Created by SONY on 26-04-2015.
 */


import android.util.Log;

import com.unity3d.player.UnityPlayerActivity;
import com.unity3d.player.UnityPlayer;

public class UnityUtil extends UnityPlayerActivity {

    public void testMethod(String data) {
        Log.i("TAG", "The data was " + data);
    }
}
