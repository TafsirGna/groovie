package com.myapp.groovie;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.myapp.groovie.R.drawable;
import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.database.UtilisateurDataSource;

public class PhotoUserActivity extends Activity {

	private ImageView photo_user_imageView;
	private UtilisateurDataSource UtilisateurDS;
	private byte[] photo;
	private String titreActivity;
	private String activityParent;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_user);

		//j'initialise les variables nécessaires
		photo_user_imageView= (ImageView) findViewById(id.photo_user_layout_imageView_picture);
		UtilisateurDS= new UtilisateurDataSource(this);
		UtilisateurDS.open();

		Intent profil_intent= getIntent();
		titreActivity=profil_intent.getStringExtra("titre");
		photo=profil_intent.getByteArrayExtra("photo");
		activityParent=profil_intent.getStringExtra("activityParent");

		//je récupère le bar d'actions et renomme la barre
		ActionBar action_bar= getActionBar();
		action_bar.setTitle(titreActivity);

		//Je fournis l'image de l'utilisateur à l'imageView de la photo d'utilisateur
		if (photo!=null && photo.length!=3 && photo.length!=0)
			photo_user_imageView.setImageBitmap(Bitmap.createScaledBitmap(getImageBitmap(photo), 640, 640, true));
		else
		{
			if (activityParent.equals("utilisateur"))
				photo_user_imageView.setImageResource(drawable.icon_user);
			else if (activityParent.equals("lieu"))
				photo_user_imageView.setImageResource(drawable.map2);
		}
	}
	private Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo_user, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.photo_user_menu_action_ok:
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		UtilisateurDS.open();

	}

	@Override
	protected void onPause()
	{
		super.onPause();
		UtilisateurDS.close();
	}
	@Override
	protected void onStop()
	{
		super.onPause();
		UtilisateurDS.close();
	}
}

