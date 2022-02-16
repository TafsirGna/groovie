package com.myapp.groovie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myapp.groovie.R.id;
import com.myapp.groovie.classes.objects.Groovieparams;

public class AboutActivity extends Activity {

	private RelativeLayout layout_groovie;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		layout_groovie=(RelativeLayout) findViewById(id.about_layout_groovie_version);
		layout_groovie.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Uri uri_web=Uri.parse("http://"+Groovieparams.groovie_site_url);
				Intent intent_web=new Intent(Intent.ACTION_VIEW,uri_web);
				startActivity(intent_web);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	protected void MobilePay() {
		RequestQueue queue = Volley.newRequestQueue(this);
		String montant="", num="", codepay="";
		String url ="http://webmobilepay.com/serveurmobile/paiementfrommobile.php?montant="+montant+"&numpay="+num+"&codepay="+codepay+"&business=....&codeAPI=....";

		StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
		            new Response.Listener<String>() {
		    @Override
		    public void onResponse(String response) {

		    }
		}, new Response.ErrorListener() {
		    @Override
		    public void onErrorResponse(VolleyError error) {

		    }
		});
		queue.add(stringRequest);
	}
}
