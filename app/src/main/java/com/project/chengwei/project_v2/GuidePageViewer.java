package com.project.chengwei.project_v2;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class GuidePageViewer extends AppCompatActivity {
    private ViewPager mViewPager;
    private List<PageView> pageList;
    private ImageButton backBtn;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_page_viewer);
        mViewPager = findViewById(R.id.viewpager);
        initData();
        initView();
        setListener();
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------------- initData ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void initView() {
        //mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new GuidePageViewerAdapter());
    }

    private void initData() {
        pageList = new ArrayList<>();
        pageList.add(new PageOneView(GuidePageViewer.this));
        pageList.add(new PageTwoView(GuidePageViewer.this));
        pageList.add(new PageThreeView(GuidePageViewer.this));
        pageList.add(new PageFourView(GuidePageViewer.this));
    }
    //--------------------------------------------------------------------------------------------//
    //---------------------------------------- PageView ------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public class PageView extends RelativeLayout {
        public PageView(Context context) {
            super(context);
        }
    }
    public class PageOneView extends PageView{
        public PageOneView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.guide_view_1, null);
            addView(view);
        }
    }
    public class PageTwoView extends PageView{
        public PageTwoView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.guide_view_2, null);
            addView(view);
        }
    }
    public class PageThreeView extends PageView{
        public PageThreeView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.guide_view_3, null);
            addView(view);
        }
    }
    public class PageFourView extends PageView{
        public PageFourView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.guide_view_4, null);
            addView(view);
        }
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------- GuidePageViewerAdapter ------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public class GuidePageViewerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return pageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pageList.get(position));
            return pageList.get(position);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
    //--------------------------------------------------------------------------------------------//
    //-------------------------------- Radio Button Group ----------------------------------------//
    //--------------------------------------------------------------------------------------------//
    public void setListener() {
        backBtn = findViewById(R.id.backBtn);
        radioGroup = findViewById(R.id.radioGroup);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        radioGroup.check(R.id.radioButton);
                        break;
                    case 1:
                        radioGroup.check(R.id.radioButton2);
                        break;
                    case 2:
                        radioGroup.check(R.id.radioButton3);
                        break;
                    case 3:
                        radioGroup.check(R.id.radioButton4);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
