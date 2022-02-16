package com.myapp.groovie.classes.objects;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.myapp.groovie.MesLieuxFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	private final List mfragments;

	public ViewPagerAdapter(FragmentManager fm,List fragments) {
		super(fm);
		mfragments=fragments;
	}

	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return (Fragment) this.mfragments.get(position);
		//switch(position) {
		//case 1: return MesLieuxFragment.newInstance("Je suis le premier écran!");
		//case 2: return DummyFragment.newInstance("Je suis le second écran!");
		//}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mfragments.size();
	}
}