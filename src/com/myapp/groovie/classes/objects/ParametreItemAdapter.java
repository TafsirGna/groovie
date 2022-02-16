package com.myapp.groovie.classes.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myapp.groovie.R;

public class ParametreItemAdapter extends BaseAdapter{

	Context mContext;
	List<HashMap<String, Object>> mListe= new ArrayList<HashMap<String,Object>>();
	private int[] listeIconeParametre;
	
	public ParametreItemAdapter(Context context, List<HashMap<String, Object>> liste, int[] listeIcone )
	{
		super();
		mContext=context;
		mListe=liste;
		listeIconeParametre=listeIcone;
		
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
		View rowView=inflater.inflate(R.layout.itemparametre, parent,false);
		if (concertView==null)
		{
			TextView parametre=(TextView) rowView.findViewById(R.id.ItemParametre);
			ImageView icone=(ImageView) rowView.findViewById(R.id.IconItemParametre);
			parametre.setText(mListe.get(position).get("libelleParametre").toString());
			icone.setImageResource(listeIconeParametre[position]);
			
		}
		else
		{
			rowView=(View) concertView;
		}
		return rowView;
	}

}
