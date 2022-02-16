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
import android.widget.RatingBar;
import android.widget.TextView;

import com.myapp.groovie.R;

public class UtilisateurAdapter extends BaseAdapter{

	Context mContext;
	List<HashMap<String, Object>> mListe= new ArrayList<HashMap<String,Object>>();
	int id_icone;

	public UtilisateurAdapter(Context context, List<HashMap<String, Object>> liste, int icone)
	{
		super();
		mContext=context;
		mListe=liste;
		this.id_icone=icone;
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
		View rowView=inflater.inflate(R.layout.item_utilisateur_layout, parent,false);
		//if (concertView==null)
		//{
		TextView pseudo=(TextView) rowView.findViewById(R.id.pseudoItem);
		TextView email=(TextView) rowView.findViewById(R.id.emailItem);
		TextView nombreUser=(TextView) rowView.findViewById(R.id.nbUtilisateurItem);
		ImageView image=(ImageView) rowView.findViewById(R.id.UserIcon);
		RatingBar ratingbar=(RatingBar) rowView.findViewById(R.id.item_utilisateur_layout_ratingbar);

		pseudo.setText(mListe.get(position).get("Pseudo").toString());
		email.setText((Spanned) mListe.get(position).get("Email"));
		nombreUser.setText(mListe.get(position).get("nbUtilisateur").toString());
		//
		Bitmap bp=(Bitmap) mListe.get(position).get("photo");
		if (bp==null)
			image.setImageResource(id_icone);
		else
			image.setImageBitmap(bp);

		ratingbar.setRating(Float.parseFloat(mListe.get(position).get("note").toString()));

		//}
		//else
		//{
		//	rowView=(View) concertView;
		//}
		return rowView;
	}

}

