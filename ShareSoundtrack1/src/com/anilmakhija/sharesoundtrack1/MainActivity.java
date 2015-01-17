package com.anilmakhija.sharesoundtrack1;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.anilmakhija.helpers.GPSTracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

 public class MainActivity extends Activity {
	
	InputStream is=null;
	String result=null;
	String line=null;
	int code;
	
	private ProgressDialog progress;
	
   static final String FTP_HOST= "mathdemat.comuf.com";
    
    static final String FTP_USER = "a7332706";
    static final String FTP_PASS  ="9468697576anil";
	
	String filePath,filePathForDatabase;
	String Latitude,Longitude;
	GPSTracker gps;
	 Button upload;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progress=new ProgressDialog(this);
        progress.setMessage("Uploading..");
        progress.setIndeterminate(true);
        progress.setCancelable(true);
        gps = new GPSTracker(MainActivity.this);
        upload=(Button)findViewById(R.id.buttonUpload);
      
        upload.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 if(gps.canGetLocation()){       
					 getGPS();
			        }else{
			            gps.showSettingsAlert();
			            return;
			        }
				
				openGalleryAudio();
			}
		});
    }
    
    
    public void getGPS()
    {
       double latitude = gps.getLatitude();Latitude= String.valueOf(latitude);
       double longitude = gps.getLongitude(); Longitude= String.valueOf(longitude);
       Toast.makeText(this, "Your Location is\nLatitude::"+Latitude+"\nLongitude::"+Longitude, Toast.LENGTH_SHORT).show();
    }


    
    public void insert()
    {
    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
   	nameValuePairs.add(new BasicNameValuePair("lat",Latitude));
   	nameValuePairs.add(new BasicNameValuePair("long",Longitude));
	nameValuePairs.add(new BasicNameValuePair("link",filePathForDatabase));
    	
    	try
    	{
		HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost("http://mathdemat.comuf.com/insert.php");
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        HttpResponse response = httpclient.execute(httppost); 
	        HttpEntity entity = response.getEntity();
	        is = entity.getContent();
	        Log.e("pass 1", "connection success ");
	}
        catch(Exception e)
	{
        	Log.e("Fail 1", e.toString());
	    	Toast.makeText(getApplicationContext(), "Invalid IP Address",
			Toast.LENGTH_LONG).show();
	}     
        
        try
        {
            BufferedReader reader = new BufferedReader
			(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
	    {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
	    Log.e("pass 2", "connection success ");
	}
        catch(Exception e)
	{
            Log.e("Fail 2", e.toString());
	}     
       

    }
    
    public void openGalleryAudio(){
    	 
        Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Audio "), 1);
       }
     
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
     
            if (resultCode == RESULT_OK) {
     
                if (requestCode == 1)
                {
                    System.out.println("SELECT_AUDIO");
                    Uri selectedAudioUri = data.getData();
                    String selectedPath = getPath(getApplicationContext(),selectedAudioUri);
                    System.out.println("SELECT_AUDIO Path : " + selectedPath);
                    filePath=selectedPath;
                    String fname="";
                    fname=selectedPath.substring(selectedPath.lastIndexOf("/")+1, selectedPath.length());
                    System.out.println(fname);
                    filePathForDatabase="http://mathdemat.comuf.com/uploads/"+fname;
                    progress.setMessage("Uploading .."+fname);
                    
                    new uploadRequest().execute();
                    
                   
                    
                }
     
            }
        }
    
    public static String getPath(final Context context,final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
            String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    public void uploadFile(File fileName){
        
        
        FTPClient client = new FTPClient();
        System.out.println("Milestone 1 "+ fileName);
         
       try {
    	   System.out.println("Milestone 2");
           client.connect(FTP_HOST,21);
           
           client.login(FTP_USER, FTP_PASS);
           System.out.println("Milestone 4");
           client.setType(FTPClient.TYPE_BINARY);
           System.out.println("Milestone 5");
          client.changeDirectory("/public_html/uploads");
           System.out.println("Milestone 6");
            
           
           client.upload(fileName, new MyTransferListener());
           System.out.println("Milestone 7");
       } catch (Exception e) {
           e.printStackTrace();
           try {
               client.disconnect(true);    
           } catch (Exception e2) {
               e2.printStackTrace();
           }
       }
        
   }
    
   /*******  Used to file upload and show progress  **********/
    
   public class MyTransferListener implements FTPDataTransferListener {

       public void started() {
    	   
    	 
           
           // Transfer started
           //Toast.makeText(getBaseContext(), " Upload Started ...", Toast.LENGTH_SHORT).show();
           System.out.println(" Upload Started ...");
       }

       public void transferred(int length) {
            
           // Yet other length bytes has been transferred since the last time this
           // method was called
          // Toast.makeText(getBaseContext(), " transferred ..." + length, Toast.LENGTH_SHORT).show();
           System.out.println(" transferred ..." + length);
       }

       public void completed() {
          
            
            
           // Transfer completed
            
          // Toast.makeText(getBaseContext(), " completed ...", Toast.LENGTH_SHORT).show();
           System.out.println(" completed ..." );
       }

       public void aborted() {
            
           // Transfer aborted
           //Toast.makeText(getBaseContext()," transfer aborted ,please try again...", Toast.LENGTH_SHORT).show();
           System.out.println(" aborted ..." );
       }

       public void failed() {
            
           // Transfer failed
    	   //Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
           System.out.println(" failed ..." );
       }

   }
   
   public class uploadRequest extends AsyncTask<Void,Void,Void>
   {

	   
	 
	
	   
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		
		progress.show();
	}



	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		
		
		 File f = new File(filePath);
			
			System.out.println("Milestone 1  "+filePath);
			uploadFile(f);
			insert();
			
			return null;
	}

	  
	  @Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progress.dismiss();
		}

   }
   
}
