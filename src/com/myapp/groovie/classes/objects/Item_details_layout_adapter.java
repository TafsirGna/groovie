package com.myapp.groovie.classes.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myapp.groovie.R;

public class Item_details_layout_adapter extends BaseAdapter{



	Context mContext;
	List<HashMap<String, Object>> mListe= new ArrayList<HashMap<String,Object>>();
	private int[] listeIconeDetails;
	//= new int[]{R.drawable.icone_localisation,R.drawable.icone_longitude,R.drawable.icone_latitude,R.drawable.icone_prix,R.drawable.icone_like,R.drawable.icone_calendrier,R.drawable.ic_action_profil,R.drawable.icone_calendrier};

	public Item_details_layout_adapter(Context context, List<HashMap<String, Object>> liste, int[] liste_icones)
	{
		super();
		mContext=context;
		mListe=liste;
		listeIconeDetails=liste_icones;
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
		View rowView=inflater.inflate(R.layout.item_details_lieu_listview_layout, parent,false);
		//if (concertView==null)
		//{
		TextView libelleDetail=(TextView) rowView.findViewById(R.id.item_details_lieu_textView_libelleDetails);
		TextView valueDetail=(TextView) rowView.findViewById(R.id.item_details_lieu_textView_valueDetails);
		ImageView icone=(ImageView) rowView.findViewById(R.id.item_details_lieu_ImageView_icone);
		libelleDetail.setText(mListe.get(position).get("libelleDetails").toString());
		valueDetail.setText((Spanned) mListe.get(position).get("valueDetails"));
		icone.setImageResource(listeIconeDetails[position]);
		//}
		//else
		//{
		//rowView=(View) concertView;
		//}
		return rowView;



	}


}
