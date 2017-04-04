package com.example.autoviewpager.sample;

import java.util.ArrayList;

import com.example.autoviewpager.AutoViewPager;
import com.example.autoviewpager.R;
import com.example.autoviewpager.AutoViewPager.OnPagerClickListener;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        AutoViewPager pager = (AutoViewPager) findViewById(R.id.pager);
        
        //设置ViewPager条目的滚动速度
        pager.setViewPagerScroller(2000);
        
        //设置指示器的外边距
        pager.setIndicatorMargin(5, 5, 5, 5);
        
        //设置指示器的位置
        pager.setIndicatorLocation(AutoViewPager.INDICATOR_LOCATION_RIGHT);
        
        //设置指示器圆点半径
        pager.setPointSize(10);
        
        //设置指示器圆点间的间距
        pager.setPointMargin(10, 10, 10, 10);
        
        //设置指示器圆点颜色
        pager.setPointColor(0xffff0000, 0xffcccccc);
        
        //设置资源图片
        int[] imageIds = {R.drawable.top1, R.drawable.top2, R.drawable.top3, R.drawable.top4};
        ArrayList<ImageView> list = new ArrayList<ImageView>();
        ImageView imageView;
        for(int i=0; i<imageIds.length; i++){
        	imageView = new ImageView(getApplicationContext());
        	imageView.setImageResource(imageIds[i]);
        	list.add(imageView);
        }
        pager.setImageRes(list);
        
        //设置条目的点击监听
        pager.setOnPagerClickListener(new OnPagerClickListener() {
        	@Override
        	public void onPagerClick(int position) {
        		System.out.println("条目"+position+"被点击了");	
        	}
        });
    	
    }

}
