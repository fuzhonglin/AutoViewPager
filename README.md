# AutoViewPager #
一个可以自动轮播，且带有圆点指示器的ViewPager控件，使用时，只需要设置需要显示的图片资源即可，显示资源以list<ImageView>的形式传入。

**该控件具有以下常用方法**

* public void setViewPagerScroller(int duration) 设置ViewPager条目的滚动速度
* public void setIndicatorLocation(int location) 设置指示器的位置
* public void setPointSize(int size) 设置指示器圆点的半径
* public void setPointColor(int selectColor, int defaultColor) 设置指示器圆点选中和未选中时的颜色
* public void setPointInterval(int interval) 设置指示器圆点间的间距
* public void setOnPagerClickListener(OnPagerClickListener onPagerClickListener设置ViewPager条目的点击事件监听
