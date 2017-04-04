package com.example.autoviewpager;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class AutoViewPager extends RelativeLayout {
	
	private static final int NEXT = 100;//开始轮播的消息值
	private ArrayList<ImageView> mList;//图片资源
	
	private LinearLayout mIndicator;//轮播指示器
	private GradientDrawable mPointSelected;//轮播指示器的选中状态
	private GradientDrawable mPointDefault;//轮播指示器的未选中状态
	
	private boolean isRunning = true;//是否自动轮播
	private OnPagerClickListener mOnPagerClickListener;//图片点击事件的监听类
	private int mDuration = 2000;//滚动的时间，默认为2秒
	private ViewPager mViewPager;//用于展示轮播图的ViewPager
	private int downX, downY, downTime; //条目被触摸时，按下的坐标以及时间
	
	//指示器的边距，默认为5dp
	private int indicatorMargin = dip2px(5);
	private int indicatorTop=indicatorMargin,indicatorBottom=indicatorMargin, 
				indicatorLeft=indicatorMargin, indicatorRight=indicatorMargin;
	public static final int INDICATOR_LOCATION_CENTER = 101;//指示器位于底部居中
	public static final int INDICATOR_LOCATION_LEFT = 102;//指示器位于左下角
	public static final int INDICATOR_LOCATION_RIGHT = 103;//指示器位于右下角
	private int indicatorLocation = INDICATOR_LOCATION_RIGHT;//指示器的当前位置，默认右下角
	
	private int pointSize = dip2px(10);//指示器圆点的半径，默认为10dp
	private int selectPointColor = Color.parseColor("#ff0000");//选中时指示器圆点颜色，默认为红色
	private int defaultPointColor = Color.parseColor("#cccccc");//未选中时指示器圆点颜色，默认为灰色
	//指示器圆点的外边距，默认为10dp
	private int pointMargin = dip2px(10);
	private int pointTop=pointMargin,pointBottom=pointMargin,pointLeft=pointMargin,pointRight=pointMargin;
	
	// 通过循环发送消息，实现自动轮播
	@SuppressLint("HandlerLeak") 
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == NEXT && isRunning){
				mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
				sendEmptyMessageDelayed(NEXT, 2000);
			}
		};
	};

	public AutoViewPager(Context context) {
		this(context,null);
	}

	public AutoViewPager(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public AutoViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * 设置用于轮播的图片资源
	 * @param  list 
	 * 		图片的集合，集合中为ImagerView对象
	 */
	public void setImageRes(ArrayList<ImageView> list){
		mList = list;
		initViewPager();
		initIndicator();
	}
	
	/**
	 *  ViewPager条目的点击事件监听接口
	 */
	public interface OnPagerClickListener{
		public void onPagerClick(int position);
	}
				
	/**
	 * 设置ViewPager条目的点击事件
	 * @param onPagerClickListener ViewPager
	 * 		条目的点击事件监听接口
	 */
	public void setOnPagerClickListener(OnPagerClickListener onPagerClickListener){
		mOnPagerClickListener = onPagerClickListener;
	}
	
	/**
	 * 设置ViewPager条目的滚动速度
	 * @param duration ViewPager
	 * 		单个条目执行滚动动画的时间，单位为毫秒。
	 */
	public void setViewPagerScroller(int duration){
		mDuration = duration;
	}
	
	/**
	 * 设置指示器的外边距，以调整指示器的位置，单位为dp
	 * @param top 
	 * 		距离顶部边距
	 * @param bottom 
	 * 		距离底部边距
	 * @param left 
	 * 		距离左侧边距
	 * @param right 
	 * 		距离右侧边距
	 */
	public void setIndicatorMargin(int top, int bottom, int left, int right){
		indicatorTop = dip2px(top);
		indicatorBottom = dip2px(bottom);
		indicatorLeft = dip2px(left);
		indicatorRight = dip2px(right);
	}
	
	/**
	 * 设置指示器的位置
	 * @param location 
	 * 		INDICATOR_LOCATION_CENTER：指示器位于底部居中；
	 * 		INDICATOR_LOCATION_LEFT：指示器位于左下角；
	 * 		INDICATOR_LOCATION_RIGHT：指示器位于右下角
	 */
	public void setIndicatorLocation(int location){
		indicatorLocation = location;
	}
	
	/**
	 * 设置指示器圆点的半径
	 * @param size 
	 * 		半径大小，单位为dp
	 */
	public void setPointSize(int size){
		pointSize = dip2px(size);
	}
	
	/**
	 * 设置指示器圆点选中和未选中时的颜色
	 * @param selectColor 
	 * 		选中时的颜色
	 * @param defaultColor 
	 * 		未选中时的颜色
	 */
	public void setPointColor(int selectColor, int defaultColor){
		selectPointColor = selectColor;
		defaultPointColor = defaultColor;
	}
	
	/**
	 * 设置指示器圆点的外边距，单位为dp
	 * @param top 
	 * 		距离顶部外边距
	 * @param bottom 
	 * 		距离底部外边距
	 * @param left 
	 * 		距离左侧外边距
	 * @param right 
	 * 		距离右侧外边距
	 */
	public void setPointMargin(int top, int bottom, int left, int right){
		pointTop = dip2px(top);
		pointBottom = dip2px(bottom);
		pointLeft = dip2px(left);
		pointRight = dip2px(right);
	}
	
	//dp换算为px
	private int dip2px(float dip) {
		float density = getContext().getResources().getDisplayMetrics().density;
		return (int) (dip * density + 0.5f);
	}

	//初始化用于显示轮播图的ViewPager
	private void initViewPager() {
		
		//将ViewPager添加到当前控件中
		mViewPager = new ViewPager(getContext());	
		LayoutParams viewPagerParams = new RelativeLayout.LayoutParams(
		LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		initViewPagerScroller(); //通过反射方式修改条目滚动速度
		addView(mViewPager, viewPagerParams);
		
		mViewPager.setAdapter(new MyAdapter()); //为ViewPager设置适配器
		mViewPager.setCurrentItem(100*mList.size()); //设置ViewPager的初始条目位置
		mViewPager.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {	
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					//当用户触摸时，停止轮播
					stopRoll();
					//获取按下时的坐标和时间
					downX = (int) event.getX();
					downY = (int) event.getY();
					downTime = (int) System.currentTimeMillis();
					break;
					
				case MotionEvent.ACTION_UP:
					//获取抬起时的坐标和时间
					int upX = (int) event.getX();
					int upY = (int) event.getY();
					int upTime = (int) System.currentTimeMillis();
					
					//计算得到抬起与按下的位移和时间间隔
					int disX = Math.abs(upX - downX);
					int disY = Math.abs(upY - downY);
					int dis = disX -disY;
					int disTime = upTime - downTime;
					
					//判断是否是点击事件，当是点击事件时，调用点击监听类中的方法
					if(disTime<500 && dis<5){
						if(mOnPagerClickListener!=null){
							int position = mViewPager.getCurrentItem() % mList.size();
							mOnPagerClickListener.onPagerClick(position);
						}
					}
					//触摸事件结束后，重新开始自动轮播
					startRoll();
					break;
					
				case MotionEvent.ACTION_CANCEL:
					//当触摸事件意外结束时，重新开始自动轮播
					startRoll();
					break;
				}
				return true;
			}
		});
	}
	
	//开始自动轮播
	public void startRoll(){
		isRunning = true;
		handler.sendEmptyMessageDelayed(NEXT, 2000);
	}
	
	//停止自动轮播
	public void stopRoll(){
		isRunning = false;
		handler.removeMessages(NEXT);
	}
	
	//当控件添加到显示窗时，该方法会被调用，此时开启自动轮播
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		startRoll();
	}
	
	//当控件从显示窗移除时，该方法会被调用，此时停止自动轮播
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopRoll();
	}
	
	//通过反射的方式修改ViewPager条目的滚动速度
	private void initViewPagerScroller() {
		try {  
	    	Field field = ViewPager.class.getDeclaredField("mScroller");  
	        field.setAccessible(true);  
	      
	        ViewPagerScroller scroller = new ViewPagerScroller(getContext());
	        field.set(mViewPager, scroller);  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	
	//用于修改ViewPager条目的滚动速度
	class ViewPagerScroller extends Scroller {
		
		public ViewPagerScroller(Context context) {
			super(context);
		}

		public ViewPagerScroller(Context context, Interpolator interpolator) {
			super(context, interpolator);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}
		
		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}		
	}
	
	//ViewPager的适配器
	class MyAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			position = position % mList.size();
			ImageView view = mList.get(position);
			container.addView(view);
			return view;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
	

	//初始化轮播图的指示器
	private void initIndicator() {
		
		//生成指示器的选中样式
		mPointSelected = new GradientDrawable();
		mPointSelected.setShape(GradientDrawable.OVAL);
		mPointSelected.setColor(selectPointColor);
		mPointSelected.setSize(pointSize, pointSize);
		
		//生成指示器的未选中样式
		mPointDefault = new GradientDrawable();
		mPointDefault.setShape(GradientDrawable.OVAL);
		mPointDefault.setColor(defaultPointColor);
		mPointDefault.setSize(pointSize, pointSize);
		
		//生成指示器容器
		mIndicator = new LinearLayout(getContext());
		mIndicator.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams indicatorParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
																	   LayoutParams.WRAP_CONTENT);
		//设置指示器的位置
		indicatorParams.addRule(ALIGN_PARENT_BOTTOM);
		switch (indicatorLocation) {
		case INDICATOR_LOCATION_LEFT:
			indicatorParams.addRule(ALIGN_PARENT_LEFT);
			break;

		case INDICATOR_LOCATION_CENTER:
			indicatorParams.addRule(CENTER_HORIZONTAL);
			break;
			
		case INDICATOR_LOCATION_RIGHT:
			indicatorParams.addRule(ALIGN_PARENT_RIGHT);
			break;
		}
		
		//设置指示器的外边距
		indicatorParams.topMargin = indicatorTop;
		indicatorParams.bottomMargin = indicatorBottom;
		indicatorParams.leftMargin = indicatorLeft;
		indicatorParams.rightMargin = indicatorRight;
		
		//将指示器加入当前控件中
		addView(mIndicator, indicatorParams);
		
		//将圆点加入容器内
		int currentItem = mViewPager.getCurrentItem();
		ImageView pointView;
		for(int i=0; i<mList.size(); i++){
			pointView = new ImageView(getContext());
			if(i == currentItem){
				pointView.setImageDrawable(mPointSelected);
			}else{
				pointView.setImageDrawable(mPointDefault);
			}
			//设置圆点之间的间距
			android.widget.LinearLayout.LayoutParams pointParams = new LinearLayout.LayoutParams(
											android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 
											android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
			pointParams.leftMargin = pointLeft;
			pointParams.rightMargin = pointRight;
			pointParams.bottomMargin = pointBottom;
			pointParams.topMargin = pointTop;
			mIndicator.addView(pointView, pointParams);
		}
		
		//随着ViewPager的轮播修改指示器状态
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){}
			@Override
			public void onPageScrollStateChanged(int state){}
			@Override
			public void onPageSelected(int position) {
				position = position % mList.size();
				for(int i=0; i<mList.size(); i++){
					ImageView point = (ImageView) mIndicator.getChildAt(i);
					if(i == position ){
						point.setImageDrawable(mPointSelected);
					}else{
						point.setImageDrawable(mPointDefault);
					}
				}
			}
		});
	}
	
}