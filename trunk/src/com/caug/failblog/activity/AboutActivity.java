package com.caug.failblog.activity;

import com.caug.failblog.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutActivity extends BaseActivity 
{
	public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
		Button backButton = (Button) findViewById(R.id.btn_back);
		
		backButton.setOnClickListener(backButtonClicked);
		
		trackPageView("/About");
    }
	
	@Override
	protected void onResume()
    {
		super.onResume();
    }
	
	private OnClickListener backButtonClicked = new OnClickListener()
	{
		public void onClick(View v)
		{
			startActivity(new Intent(getBaseContext(), ViewerActivity.class));
		}
	};
}