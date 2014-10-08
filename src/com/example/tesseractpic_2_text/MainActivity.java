package com.example.tesseractpic_2_text;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private Button cameraButton;
	private Button galleryButton;
	private ImageView photoDisplay;
	private Button postButton;
	private Uri outputFileUri;
	private String selected;
	private String finalResultsText;
	private int REQUEST_GALLERY = 2;
	private int REQUEST_CAMERA = 1;
	public static String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/TesseractPic-2-Text/";
	
	public final static String EXTRA_MESSAGE = "com.example.tesseractpic_2_text.MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final View contentView = findViewById(R.id.fullscreen_content);		

		//Sets the camera button and to the id of camera button on activity_main
		cameraButton = (Button) findViewById(R.id.camera_button);
		//Sets the gallery button to the id of gallery button on activity_main
		galleryButton = (Button) findViewById(R.id.gallery_button);
		postButton = (Button) findViewById(R.id.post_button);

		//Makes the button start to "listen" to read if the Gallery button is pressed
		galleryButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View x) {

				Intent galleryIntent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
				outputFileUri = galleryIntent.getData();				
				galleryIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);				
				startActivityForResult(galleryIntent, REQUEST_GALLERY);				
			}
		});


		//Makes the button start to "listen" to read if the Gallery button is pressed
		cameraButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {            	
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, REQUEST_CAMERA);	
			}
		});
		
		postButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {            	
				Intent resultPage = new Intent(MainActivity.this, ProcessedText.class);
				startActivity(resultPage);
			}
		});

		
		//Sending the image to Tesseract
		//TessBaseAPI baseApi = new TessBaseAPI();
	/*	String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractPic-2-Text/";
		String lang = "eng";
		baseApi.init(DATA_PATH, lang);
				// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
		baseApi.setImage(bitmap);
		String recognizedText = baseApi.getUTF8Text();
		baseApi.end();
		 */
	}

	/*This function currently just finds the Uri based on the Intent that is returned from the 
	 * startActivity call from the main screen. It checks to make sure the result code is okay,
	 * and then based on the request code finds the Uri. 
	 * 
	 * If we end up needing the path as a String that is an easy fix, as we just remove the Uri.parse
	 * function calls. 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
			/* This just gets the last image taken ID and URI, which is returned as String.
			 * The last image is the one the user saved when thte capture image intent was started.
			 * The String is converted into uri using the Uri.parse function. 
			 * 
			 */
			selected = getLastImageId();
			
		}
		else if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK){
			/*	This code retreives the URI from the intent, then gets the absolute path 
			 *  using the getPath function that I found online. The Uri.parse part converts
			 *  the returned String back into Uri. The Log.d line is a debug message to the logcat.
			 */
			Uri selectedpic = data.getData();
			selected = getPath(selectedpic);
			Log.d("getting gallery", "gallery image uri: " + selected);
			
			
		}
		else{
			//TODO : deal with request code that is different and result code that isn't ok
		}
		
		try {
			Bitmap thePicture = prepareImage();
			String result = processImage(thePicture);
			setResultsText(result);
			Log.d("TestResult", "the text is: " + result);
			
			Intent resultPage = new Intent(this, ProcessedText.class);
			resultPage.putExtra(EXTRA_MESSAGE, getResultsText());
			
			startActivity(resultPage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	private void setResultsText(String mTemp)
	{
		finalResultsText = mTemp;
	}

	
	public String getResultsText()
	{
		return finalResultsText;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	/* This function I found online to retreive the URI of the last image taken by the camera and
	 * returns the path as a String.
	 * 
	 */
	private String getLastImageId(){
	    final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
	    final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
	    Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
	    if(imageCursor.moveToFirst()){
	        int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
	        String fullPath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
	        Log.d("getLastImageID", "getLastImageId::id " + id);
	        Log.d("getLastImageID", "getLastImageId::path " + fullPath);
	        imageCursor.close();
	        return fullPath;
	    }else{
	        return "error";
	    }
	}
	
	/* This function I found online to retreive the URI of the selected gallery image based on the 
	 * passed in intent uri and returns the path as a String. 
	 */
	public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
	
	
	/* this function prepares the image using the code I found in here already.
	 * It returns the bitmap to the function that called it.
	 * 
	 */
	public Bitmap prepareImage() throws IOException{
		 String _path = selected;
		 BitmapFactory.Options options = new BitmapFactory.Options();
		 options.inSampleSize = 4;
		    	
		 Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
		 
		 ExifInterface exif;
			
	     exif = new ExifInterface(_path);
			
	     int exifOrientation = exif.getAttributeInt(
			         ExifInterface.TAG_ORIENTATION,
			         ExifInterface.ORIENTATION_NORMAL);
			 
		 

		 int rotate = 0;

		 switch (exifOrientation) {
		 case ExifInterface.ORIENTATION_ROTATE_90:
		     rotate = 90;
		     break;
		 case ExifInterface.ORIENTATION_ROTATE_180:
		     rotate = 180;
		     break;
		 case ExifInterface.ORIENTATION_ROTATE_270:
		     rotate = 270;
		     break;
		 }

		 if (rotate != 0) {
		     int w = bitmap.getWidth();
		     int h = bitmap.getHeight();

		     // Setting pre rotate
		     Matrix mtx = new Matrix();
		     mtx.preRotate(rotate);

		     // Rotating Bitmap & convert to ARGB_8888, required by tess
		     bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
		 }
		 bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		 //Bitmap theOriginalImage = BitmapFactory.de
	
		return bitmap;
	}
	
	public String processImage(Bitmap selectedPic)
	{
		copyTrainedDatatoSD();
		TessBaseAPI baseApi = new TessBaseAPI();
		String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractPic-2-Text/";
		String lang = "eng";
		baseApi.init(DATA_PATH, lang);
				// Eg. baseApi.init("/mnt/sdcard/tesseract/tessdata/eng.traineddata", "eng");
		baseApi.setImage(selectedPic);
		String recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();
		recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		recognizedText = recognizedText.trim();
		return recognizedText;
		
	}

	public void copyTrainedDatatoSD()
	{
		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v("File Error", "ERROR: Creation of directory " + path + " on sdcard failed");
					//return;
				} else {
					Log.v("File Success", "Created directory " + path + " on sdcard");
				}
			}

		}

		// lang.traineddata file with the app (in assets folder)
		// You can get them at:
		// http://code.google.com/p/tesseract-ocr/downloads/list
		// This area needs work and optimization
		if (!(new File(DATA_PATH + "tessdata/eng.traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/eng.traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/eng.traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();

				Log.v("file copy success", "Copied eng traineddata");
			} catch (IOException e) {
				Log.e("file copy error", "Was unable to copy eng traineddata " + e.toString());
			}
		}
	}
}
