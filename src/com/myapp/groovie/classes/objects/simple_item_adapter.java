package com.myapp.groovie.classes.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.myapp.groovie.R;

public class simple_item_adapter extends BaseAdapter{

	Context mContext;
	List<HashMap<String, Object>> mListe= new ArrayList<HashMap<String,Object>>();
	
	public simple_item_adapter(Context context, List<HashMap<String, Object>> liste)
	{
		super();
		mContext=context;
		mListe=liste;
		
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListe.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListe.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View concertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView=inflater.inflate(R.layout.item_listview_simple, parent,false);
		//if (concertView==null)
		//{
			TextView text1=(TextView) rowView.findViewById(R.id.item_listview_simple_textView_text1);
			TextView text2=(TextView) rowView.findViewById(R.id.item_listview_simple_textView_text2);
			
			text1.setText(mListe.get(position).get("text1").toString());
			text2.setText(mListe.get(position).get("text2").toString());
		//}
		//else
		//{
		//	rowView=(View) concertView;
		//}
		return rowView;
	}
	
}
