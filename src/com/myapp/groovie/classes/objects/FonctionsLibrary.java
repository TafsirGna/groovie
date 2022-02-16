package com.myapp.groovie.classes.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ParseException;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class FonctionsLibrary {

	private static String[] liste_mois= {"Janvier","Février","Mars","Avril","Mai","Juin","Juillet","Août","Septembre","Octobre","Novembre","Décembre"};
	/*
	public static String formatDateTime(Context context, String timeToFormat) {

		String finalDateTime = "";          

		SimpleDateFormat iso8601Format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		Date date = null;
		if (timeToFormat != null) {
			try {
				try {
					date = (Date) iso8601Format.parse(timeToFormat);
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ParseException e) {
				date = null;
			}

			if (date != null) {
				long when = date.getTime();
				int flags = 0;
				flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
				flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
				flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
				flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

				finalDateTime = android.text.format.DateUtils.formatDateTime(context,
						when + TimeZone.getDefault().getOffset(when), flags);               
			}
		}
		return finalDateTime;
	}
	 */

	public static String formatDateTime( String timeToFormat) {

		String finalDateTime = "";          

		SimpleDateFormat iso8601Format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		Date date = null;
		if (timeToFormat != null) {
			try {
				try {
					date = (Date) iso8601Format.parse(timeToFormat);
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ParseException e) {
				date = null;
			}
			
			Calendar cal= Calendar.getInstance();
			cal.setTime(date);
			finalDateTime+=cal.get(Calendar.DAY_OF_MONTH)+" "+liste_mois[cal.get(Calendar.MONTH)]+" "+cal.get(Calendar.YEAR)+" à "+((cal.get(Calendar.HOUR)<10) ? "0"+cal.get(Calendar.HOUR) : cal.get(Calendar.HOUR))+"h"+((cal.get(Calendar.MINUTE)<10 ? "0"+cal.get(Calendar.MINUTE) : cal.get(Calendar.MINUTE)))+"min";
		}
		return finalDateTime;
	}
	
	public static Bitmap StringToBitMap(String encodedString) {
		try {
			byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			return bitmap;
		} catch (Exception e) {
			e.getMessage();
			return null;
		}
	}

	public static String BitMapToString(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		String temp = Base64.encodeToString(b, Base64.DEFAULT);
		return temp;
	}
	
	public static Bitmap getResizedBitmap(Bitmap image, int maxSize)
	{
		int width= image.getWidth();
		int height= image.getHeight();

		float bitmapRatio=(float) width/(float) height;
		if (bitmapRatio>0)
		{
			width=maxSize;
			height=(int) (width/bitmapRatio);
		}
		else
		{
			height=maxSize;
			width=(int) (width/bitmapRatio);
		}
		return Bitmap.createScaledBitmap(image, width, height, true);
	}
	
	public static Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight)
	{
		return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
	}
	
	public static Bitmap getImageBitmap(byte[] image)
	{
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	public static byte[] bitmap_to_byte(Bitmap bmp)
	{
		ByteArrayOutputStream stream= new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] bt= stream.toByteArray();
		return bt;
	}
	public static Bitmap decodeFile(File f,int REQUIRED_WIDTH, int REQUIRED_HEIGHT)
	{
		try {
			BitmapFactory.Options o= new BitmapFactory.Options();
			o.inJustDecodeBounds=true;
			FileInputStream stream1= new FileInputStream(f);
			BitmapFactory.decodeStream(stream1,null,o);
			stream1.close();
			
			int width_tmp=o.outWidth, height_tmp=o.outHeight;
			int scale=1;
			while(true)
			{
				if (width_tmp/2 < REQUIRED_WIDTH || height_tmp/2<REQUIRED_HEIGHT)
					break;
				width_tmp/=2;
				height_tmp/=2;
				scale*=2;
			}
			BitmapFactory.Options o2=new BitmapFactory.Options();
			o2.inSampleSize=scale;
			FileInputStream stream2= new FileInputStream(f);
			Bitmap bitmap=BitmapFactory.decodeStream(stream2,null,o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
			// TODO: handle exception
		}
	 catch (IOException e) {
		// TODO: handle exception
		 e.printStackTrace();
	}
		return null;
	}
}
