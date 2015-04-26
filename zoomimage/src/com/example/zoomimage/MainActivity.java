package com.example.zoomimage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends Activity {  
	
	HorizontalScrollView hsv_handle;
	ImageView img_handle;
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);
        hsv_handle = (HorizontalScrollView) findViewById(R.id.hsv);
        img_handle = (ImageView) findViewById(R.id.imageView1);
    	
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	focusOnView();
    	}
  
    
    private final void focusOnView(){
        new Handler().postDelayed(new Runnable() {
        	
        	int x=(img_handle.getWidth())/4;
        	int y=0;
            @Override
            public void run() {
                hsv_handle.scrollTo(x,y);
                Toast.makeText(getApplicationContext(),
     					"srcoll done", Toast.LENGTH_SHORT)
     					.show();
            }
        },5000);
    }
    
    /*private void scrollmethod(int xcord,int ycord){
    	
    }*/
}
    	
    	
  //(img_handle.getWidth)-x  	
    	
    	
   /* 	 try {
 			Thread.sleep(5000);
 			Toast.makeText(getApplicationContext(),
 					"5 seconds done", Toast.LENGTH_SHORT)
 					.show();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
        // hsv_handle.scrollTo(10000, 10000);
         hsv_handle.scrollBy(5000, 5000);
    }*/
    
    
   
    
    