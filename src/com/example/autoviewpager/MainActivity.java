package com.example.autoviewpager;

import java.util.ArrayList;

import com.example.autoviewpager.AutoViewPager.OnPagerClickListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener{

    private AutoViewPager pager;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        pager = (AutoViewPager) findViewById(R.id.pager);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        
        int[] imageIds = {R.drawable.top1, R.drawable.top2,R.drawable.top3,R.drawable.top4};
        ArrayList<ImageView> list = new ArrayList<ImageView>();
        ImageView imageView;
        
        for(int i=0; i<imageIds.length; i++){
        	imageView = new ImageView(getApplicationContext());
        	imageView.setImageResource(imageIds[i]);
        	list.add(imageView);
        }
        
        pager.setImageRes(list);
        
        pager.setOnPagerClickListener(new OnPagerClickListener() {
			@Override
			public void onPagerClick(int position) {
				System.out.println("条目"+position+"被点击了");	
			}
		});
        
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			pager.setIndicatorMargin(0, 0, 0, 40);
			break;
		case R.id.button2:
			pager.setPointInterval(10);
			break;
		case R.id.button3:
			pager.setIndicatorLocation(AutoViewPager.INDICATOR_LOCATION_CENTER);
			break;
		case R.id.button4:
			pager.setPointSize(5);
			break;
		case R.id.button5:
			pager.setPointColor(0xff00ff00, 0xffcccccc);
			break;
		case R.id.button6:
			pager.setViewPagerScroller(1000);
			break;
		}
	}
    
}
