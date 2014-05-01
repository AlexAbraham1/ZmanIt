package com.alexabraham.zmanit.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by alexabraham on 3/23/14.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch(index){
            case 0:
                //Main Fragment Activity
                return new MainFragment();
            case 1:
                //Alarms fragment
                return new AlarmsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        //get item count - equal to number of tabs
        return 2;
    }
}