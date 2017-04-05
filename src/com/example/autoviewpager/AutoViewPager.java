package com.example.autoviewpager;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class AutoViewPager extends RelativeLayout {
	
	private static final int NEXT = 100;//开始轮播的消息值
	private ArrayList<ImageView> mList;//图片资源
	private int mHeight = 0;//控件高度
	
	private LinearLayout mIndicator;//轮播指示器
	private GradientDrawable mPointSelected;//轮播指示器的选中状态
	private GradientDrawable mPointDefault;//轮播指示器的未选中状态
	
	private boolean mIsRunning = true;//是否自动轮播
	private OnPagerClickListener mOnPagerClickListener;//图片点击事件的监听类
	private int mDuration = 2000;//滚动的时间，默认为2秒
	private ViewPager mViewPager;//用于展示轮播图的ViewPager
	private int downX, downY, downTime; //条目被触摸时，按下的坐标以及时间
	
	//指示器的边距，默认为5dp
	private int mIndicatorTop=dip2px(5),mIndicatorBottom=dip2px(5),mIndicatorLeft=dip2px(5),mIndicatorRight=dip2px(5);
	public static final int INDICATOR_LOCATION_CENTER = 101;//指示器位于底部居中
	public static final int INDICATOR_LOCATION_LEFT = 102;//指示器位于左下角
	public static final int INDICATOR_LOCATION_RIGHT = 103;//指示器位于右下角
	private int mIndicatorLocation = INDICATOR_LOCATION_RIGHT;//指示器的当前位置，默认右下角
	
	private int mPointSize = dip2px(10);//指示器圆点的半径，默认为10dp
	private int mSelectPointColor = 0xffff0000;//选中时指示器圆点颜色，默认为红色
	private int mDefaultPointColor = 0xffcccccc;//未选中时指示器圆点颜色，默认为灰色
	private int mPointInterval = dip2px(5);//指示器圆点间的间距，默认为5dp
	
	//通过循环发送消息，实现自动轮播
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == NEXT && mIsRunning){
				if(mViewPager!=null){
					mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
					sendEmptyMessageDelayed(NEXT, 2000);
				}
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
		if(mViewPager==null)return;
		initViewPagerScroller();
	}
	
	/**
	 * 设置控件的高度
	 * @param height
	 * 		控件高度，单位为dp
	 */
	public void setAutoViewPagerHeight(int height){
		mHeight = dip2px(height);
		if(mViewPager==null)return;
		LayoutParams params = (LayoutParams) mViewPager.getLayoutParams();
		params.height = mHeight;
		mViewPager.setLayoutParams(params);
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
		mIndicatorTop = dip2px(top);
		mIndicatorBottom = dip2px(bottom);
		mIndicatorLeft = dip2px(left);
		mIndicatorRight = dip2px(right);
		
		if(mIndicator==null)return;
		LayoutParams params = (LayoutParams) mIndicator.getLayoutParams();
		
		params.topMargin = mIndicatorTop;
		params.bottomMargin = mIndicatorBottom;
		params.leftMargin = mIndicatorLeft;
		params.rightMargin = mIndicatorRight;
		
		mIndicator.setLayoutParams(params);
	}
	
	/**
	 * 设置指示器的位置
	 * @param location 
	 * 		INDICATOR_LOCATION_CENTER：指示器位于底部居中；
	 * 		INDICATOR_LOCATION_LEFT：指示器位于左下角；
	 * 		INDICATOR_LOCATION_RIGHT：指示器位于右下角
	 */
	public void setIndicatorLocation(int location){
		mIndicatorLocation = location;
		
		if(mIndicator==null)return;
		LayoutParams params = (LayoutParams) mIndicator.getLayoutParams();
		
		switch (mIndicatorLocation) {
		case INDICATOR_LOCATION_LEFT:
			params.removeRule(CENTER_HORIZONTAL);
			params.removeRule(ALIGN_PARENT_RIGHT);
			params.addRule(ALIGN_PARENT_LEFT); 
			break;
		case INDICATOR_LOCATION_CENTER:
			params.removeRule(ALIGN_PARENT_LEFT);
			params.removeRule(ALIGN_PARENT_RIGHT);
			params.addRule(CENTER_HORIZONTAL); 
			break;
		case INDICATOR_LOCATION_RIGHT:
			params.removeRule(CENTER_HORIZONTAL);
			params.removeRule(ALIGN_PARENT_LEFT);
			params.addRule(ALIGN_PARENT_RIGHT); 
			break;
		}
		
		mIndicator.setLayoutParams(params);
	}
	
	/**
	 * 设置指示器圆点的半径
	 * @param size 
	 * 		半径大小，单位为dp
	 */
	public void setPointSize(int size){
		mPointSize = dip2px(size);
		
		if(mPointSelected==null || mPointDefault==null) return;
		mPointSelected.setSize(mPointSize, mPointSize);
		mPointDefault.setSize(mPointSize, mPointSize);
	}
	
	/**
	 * 设置指示器圆点选中和未选中时的颜色
	 * @param selectColor 
	 * 		选中时的颜色
	 * @param defaultColor 
	 * 		未选中时的颜色
	 */
	public void setPointColor(int selectColor, int defaultColor){
		mSelectPointColor = selectColor;
		mDefaultPointColor = defaultColor;
		
		if(mPointSelected==null || mPointDefault==null) return;
		mPointSelected.setColor(mSelectPointColor);
		mPointDefault.setColor(mDefaultPointColor);
	}
	
	/**设置指示器圆点的间距，单位为dp*/
	public void setPointInterval(int interval){
		mPointInterval = dip2px(interval);
		
		if(mIndicator==null) return;
		int count = mIndicator.getChildCount();
		LinearLayout.LayoutParams params;
		View child;
		
		for(int i=0; i<count; i++){
			child = mIndicator.getChildAt(i);
			params = (LinearLayout.LayoutParams)child.getLayoutParams();
			
			switch (mIndicatorLocation) {
			case INDICATOR_LOCATION_LEFT:
				if(i == 0){
					params.leftMargin = 0;
					params.rightMargin = mPointInterval;
				}else{
					params.leftMargin = mPointInterval;
					params.rightMargin = mPointInterval;
				}
				break;
				
			case INDICATOR_LOCATION_CENTER:
				params.leftMargin = mPointInterval;
				params.rightMargin = mPointInterval;
				break;
					
			case INDICATOR_LOCATION_RIGHT:
				if(i == mList.size()-1){
					params.leftMargin = mPointInterval;
					params.rightMargin = 0;
				}else{
					params.leftMargin = mPointInterval;
					params.rightMargin = mPointInterval;
				}
				break;
			}
			child.setLayoutParams(params);
		}
	}
	
	/**设置是否开启自动轮播*/
	public void setIsAuto(boolean isAuto){
		mIsRunning = isAuto;
		if(isAuto){
			startRoll();
		}else{
			stopRoll();
		}
	}
	
	/**开始自动轮播*/
	public void startRoll(){
		mIsRunning = true;
		handler.sendEmptyMessageDelayed(NEXT, 2000);
	}
		
	/**停止自动轮播*/
	public void stopRoll(){
		mIsRunning = false;
		handler.removeMessages(NEXT);
	}

	/**初始化用于显示轮播图的ViewPager*/
	private void initViewPager() {
		creatViewPager();
		initViewPagerTouch();
	}
	
	/**生成ViewPager*/
	private void creatViewPager() {
		
		int with = LayoutParams.WRAP_CONTENT;
		int height = (mHeight== 0? LayoutParams.WRAP_CONTENT : mHeight);
		
		//将ViewPager添加到当前控件中
		mViewPager = new ViewPager(getContext());	
		LayoutParams viewPagerParams = new RelativeLayout.LayoutParams(with,height);
		initViewPagerScroller(); //通过反射方式修改条目滚动速度
		addView(mViewPager, viewPagerParams);
		
		mViewPager.setAdapter(new MyAdapter()); //为ViewPager设置适配器
		mViewPager.setCurrentItem(100*mList.size()); //设置ViewPager的初始条目位置
	}
	
	/**初始化ViewPager的触摸事件*/
	private void initViewPagerTouch() {
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

	/**初始化轮播图的指示器*/
	private void initIndicator() {
		initPoint();
		creatIndicator();
		addPointToIndicator();
		changeIndicatorState();
	}

	/**初始化指示器圆点*/
	private void initPoint() {
		//生成指示器的选中样式
		mPointSelected = new GradientDrawable();
		mPointSelected.setShape(GradientDrawable.OVAL);
		mPointSelected.setColor(mSelectPointColor);
		mPointSelected.setSize(mPointSize, mPointSize);
					
		//生成指示器的未选中样式
		mPointDefault = new GradientDrawable();
		mPointDefault.setShape(GradientDrawable.OVAL);
		mPointDefault.setColor(mDefaultPointColor);
		mPointDefault.setSize(mPointSize, mPointSize);
	}
	
	/**生成指示器容器*/
	private void creatIndicator() {
		mIndicator = new LinearLayout(getContext());
		mIndicator.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams indicatorParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//设置指示器的位置
		indicatorParams.addRule(ALIGN_PARENT_BOTTOM);
		switch (mIndicatorLocation) {
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
		indicatorParams.topMargin = mIndicatorTop;
		indicatorParams.bottomMargin = mIndicatorBottom;
		indicatorParams.leftMargin = mIndicatorLeft;
		indicatorParams.rightMargin = mIndicatorRight;	
		
		//将指示器加入当前控件中
		addView(mIndicator, indicatorParams);
	}

	/**将圆点加入容器内*/
	private void addPointToIndicator() {
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
			int with = LinearLayout.LayoutParams.WRAP_CONTENT;
			int height = LinearLayout.LayoutParams.WRAP_CONTENT;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(with, height);
				
			switch (mIndicatorLocation) {
			case INDICATOR_LOCATION_LEFT:
				if(i == 0){
					params.leftMargin = 0;
					params.rightMargin = mPointInterval;
				}else{
					params.leftMargin = mPointInterval;
					params.rightMargin = mPointInterval;
				}
				break;

			case INDICATOR_LOCATION_CENTER:
				params.leftMargin = mPointInterval;
				params.rightMargin = mPointInterval;
				break;
					
			case INDICATOR_LOCATION_RIGHT:
				if(i == mList.size()-1){
					params.leftMargin = mPointInterval;
					params.rightMargin = 0;
				}else{
					params.leftMargin = mPointInterval;
					params.rightMargin = mPointInterval;
				}
				break;
			}
			mIndicator.addView(pointView, params);
		}
	}
	
	/**随着ViewPager的轮播修改指示器状态*/
	private void changeIndicatorState() {
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
	
	/**通过反射的方式修改ViewPager条目的滚动速度*/
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
	
	/**dp换算为px*/
	private int dip2px(float dip) {
		float density = getContext().getResources().getDisplayMetrics().density;
		return (int) (dip * density + 0.5f);
	}
	
	//用于修改ViewPager条目的滚动速度
	private class ViewPagerScroller extends Scroller {
		
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
	private class MyAdapter extends PagerAdapter{

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
			view.setScaleType(ScaleType.CENTER_CROP);
			container.addView(view);
			return view;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	}
	
}