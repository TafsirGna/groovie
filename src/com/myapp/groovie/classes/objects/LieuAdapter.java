package com.myapp.groovie.classes.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myapp.groovie.R;
import com.myapp.groovie.R.id;

public class LieuAdapter extends BaseAdapter{

	Context mContext;
	List<HashMap<String, Object>> mListe= new ArrayList<HashMap<String,Object>>();

	public LieuAdapter(Context context, List<HashMap<String, Object>> liste)
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
		View rowView=inflater.inflate(R.layout.item_lieu_listview_layout, parent,false);
		//if (concertView==null)
		//{
		TextView titre=(TextView) rowView.findViewById(R.id.titre);
		TextView description=(TextView) rowView.findViewById(R.id.DescriptionLieu);
		ImageView image=(ImageView) rowView.findViewById(R.id.lieuIcon);
		ImageView status_icone=(ImageView) rowView.findViewById(id.item_lieu_listview_layout_imageView_status);
		titre.setText(mListe.get(position).get("Titre").toString());
		description.setText((Spanned) mListe.get(position).get("SousTitre"));
		if (Integer.parseInt(mListe.get(position).get("status").toString())==0)
			status_icone.setImageResource(R.drawable.ic_action_notifications);

		Bitmap bp=(Bitmap) mListe.get(position).get("picture");
		if (bp!=null)
			image.setImageBitmap(bp);

		//}
		//else
		//{
		//	rowView=(View) concertView;
		//}
		return rowView;
	}

}
