package com.reptile.nomad.changedReptile.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.reptile.nomad.changedReptile.Fragments.FragmentNewsFeed;

import java.util.List;

/**
 * Created by sankarmanoj on 17/05/16.
 */
public class NewsFeedFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private List<FragmentNewsFeed> fragments;
    public NewsFeedFragmentPagerAdapter(android.support.v4.app.FragmentManager fm, List<FragmentNewsFeed> fragments) {
        super(fm);
        this.fragments = fragments;

    }

    @Override
    public int getCount() {
        return  fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).title;
    }

}
