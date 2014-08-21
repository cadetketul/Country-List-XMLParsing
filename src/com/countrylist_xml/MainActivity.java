package com.countrylist_xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	
	EditText cont_ip;
	Button submitBtn;
	ListView lview;
	ArrayList<String> list;
	ArrayAdapter<String> adapter;
	String[][] XMLPullParserArray;
	int parseDataIncr = 0;
	DefaultHttpClient client;
	HttpGet get;
	HttpResponse response;
	int resultCount = 0;
	boolean processing = true;
	
	private final String TAG = "MainActivity";
	private static final String url1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.countries%20where%20place%3D%22";
	private static final String url2 = "%22&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cont_ip = (EditText) findViewById(R.id.editText1);
		cont_ip.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		lview = (ListView) findViewById(R.id.listView1);
		list = new ArrayList<String>();
		adapter = new ArrayAdapter<>(getApplicationContext(),
				android.R.layout.simple_list_item_1, list);
		lview.setAdapter(adapter);
		submitBtn = (Button) findViewById(R.id.button1);
		submitBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String url = cont_ip.getText().toString();
				if (url.contains(" ")) {
					url = url.replace(" ", "%20");
				}
				String finalUrl = url1+url+url2;
				processing = true;				
				new XMLParserTask().execute(finalUrl);			
			}
		});
	}
	
	private class XMLParserTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... args) {
			try {	
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(getFromUrl(args[0])));
				initPage(parser, "query");
				int eventType = parser.getEventType();
				int parseCount = 0;
				do {
					if (parseCount == resultCount) {
						break;
					}
					int temp_type;
					while ((temp_type=parser.next()) != parser.START_TAG){};
					parser.next();
					eventType = parser.getEventType();
					if(eventType == XmlPullParser.TEXT){
						String answer = parser.getText();
						parseDataIncr++;
						if (!answer.equals("Country") && parseDataIncr >1) {
							list.add(answer);
							parseCount++;				
						}
					}
				} while (eventType != XmlPullParser.END_DOCUMENT);
				processing = false;
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public InputStream getFromUrl(String url)
				throws IllegalStateException, IOException {		
			try {
				client = new DefaultHttpClient();
				get = new HttpGet(new URI(url));
				response = client.execute(get);			
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}		
			return response.getEntity().getContent();
		}
		
		public void initPage(XmlPullParser parser, String rootElement)
				throws XmlPullParserException, IOException {
			int TAG_TYPE;
			while((TAG_TYPE = parser.next()) != parser.START_TAG &&
					TAG_TYPE != parser.END_DOCUMENT){;}
			if (TAG_TYPE != parser.START_TAG) {
				Log.e(TAG, "XMLPullParserException");
			}
			else{
				resultCount = Integer.valueOf(parser.getAttributeValue(0));
			}
			if (!parser.getName().equals(rootElement)) {
				Log.e(TAG, "Root tag not found!");				
			}
		}
		
		protected void onPostExecute(String result){
			if (!processing) {
				adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "Finished downloading data!",
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
