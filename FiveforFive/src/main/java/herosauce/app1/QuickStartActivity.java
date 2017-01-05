package herosauce.app1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class QuickStartActivity extends FragmentActivity{

    FragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_start);
        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), this);
        vpPager.setAdapter(adapterViewPager);

    }
        public static class MyPagerAdapter extends FragmentPagerAdapter {
            private static int NUM_ITEMS = 5;
            Context context;

            public MyPagerAdapter(FragmentManager fragmentManager, Context mContext) {
                super(fragmentManager);
                context = mContext;
            }

            // Returns total number of pages
            @Override
            public int getCount() {
                return NUM_ITEMS;
            }


                    // Returns the fragment to display for that page
                    @Override
                    public Fragment getItem(int position) {
                        switch (position) {
                            case 0:
                                String welcome = context.getString(R.string.page_one_title);
                                return QuickStartFragment.newInstance(1,
                                        context.getString(R.string.page_one_title),
                                        context.getString(R.string.page_one_body_top),
                                        context.getString(R.string.page_one_body_bottom));
                            case 1:
                                return ThreePartFragment.newInstance(2,
                                        context.getString(R.string.page_two_title),
                                        context.getString(R.string.page_two_body_top),
                                        context.getString(R.string.page_two_middle),
                                        context.getString(R.string.page_two_body_bottom));
                            case 2:
                                return QuickStartFragment.newInstance(3,
                                        context.getString(R.string.page_three_title),
                                        context.getString(R.string.page_three_body_top),
                                        context.getString(R.string.page_three_body_bottom));
                            case 3:
                                return QuickStartFragment.newInstance(4,
                                        context.getString(R.string.page_four_title),
                                        context.getString(R.string.page_four_body_top),
                                        context.getString(R.string.page_four_body_bottom));
                            case 4:
                                return QuickStartFragment.newInstance(5,
                                        context.getString(R.string.page_five_title),
                                        context.getString(R.string.page_five_body_top),
                                        context.getString(R.string.page_five_body_bottom));
                            default:
                                return null;
                        }
                    }
            // Returns the page title for the top indicator
            @Override
            public CharSequence getPageTitle(int position) {
                position +=1;
                return "Page " + position;
            }

        }
    }

