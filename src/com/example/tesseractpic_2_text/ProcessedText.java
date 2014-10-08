package com.example.tesseractpic_2_text;



import com.example.tesseractpic_2_text.R.id;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProcessedText extends Activity {

	private EditText textResults;
	private TextView.BufferType editText;
	private Button cancelButton;
	private Button copyButton;
	private MainActivity mActivity;
	private CharSequence tempString = "Result Text Here!!";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_processed_text);

	cancelButton = (Button) findViewById(R.id.cancel_button);
	copyButton = (Button) findViewById(R.id.copy_2_clipboard);

	textResults = (EditText) findViewById(R.id.text_preview);
	//Intent intent = getIntent();
	
	setResultsDisplay();


	cancelButton.setOnClickListener(new View.OnClickListener() {
	public void onClick(View v) {
	Intent cancelIntent = new Intent(ProcessedText.this, MainActivity.class);
	startActivity(cancelIntent);
	}
	});

	copyButton.setOnClickListener(new View.OnClickListener() {
	public void onClick(View v) {

	}
	});
	}
	
	public void setResultsDisplay() {
		//tempString = mActivity.getResultsText();
		Intent intent = getIntent();
		tempString = intent.getStringExtra(MainActivity.EXTRA_MESSAGE); 
		textResults.setText(tempString);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.processed_text, menu);
	return true;
	}


}

