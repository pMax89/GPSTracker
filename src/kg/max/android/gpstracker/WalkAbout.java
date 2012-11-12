package kg.max.android.gpstracker;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;



public class WalkAbout extends MapActivity implements LocationListener{

	
	private MapView mapView;
	private MapController mMapController;
	private PathOverlay mPathOverlay;
	private MyLocationOverlay mLocationOverlay;
	
	private ArrayList<GeoPoint> m_arrPathPoints;
	private LocationManager mLocManager;
	private boolean m_bRecording;

	private static final double GEO_CONVERSION = 1E6;
	private static final int MIN_TIME_CHANGE = 3000;
	private static final int MIN_DISTANCE_CHANGE = 3;
	
	private static final int PICTURE_REQUEST_CODE = 0;
	private static final int ENABLE_GPS_REQUEST_CODE = 1;
	
	private static final int STARTSTOP_MENU_ITEM = Menu.FIRST;
	private static final int SAVE_MENU_ITEM = Menu.FIRST+1;
	private static final int LOAD_MENU_ITEM = Menu.FIRST+2;
	private static final int PICTURE_MENU_ITEM = Menu.FIRST+3;
	private static final int ENABLEGPS_MENU_ITEM = Menu.FIRST+4;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
        
        initLocationData();
        initLayout(); 
    }
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
    
    private void initLocationData() {
    	// TODO
    	m_bRecording = false;
    	
    	mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	
    	m_arrPathPoints = new ArrayList<GeoPoint>();
    	
    }
    
	private void initLayout() {
		setContentView(R.layout.map_layout);
    	mapView = (MapView) findViewById(R.id.m_vwMap);
    	mMapController = mapView.getController();
    	mapView.setBuiltInZoomControls(true);    	
    	mMapController.setZoom(18);
    	
    	List<Overlay> mapOverlays = mapView.getOverlays();
    	mPathOverlay = new PathOverlay(m_arrPathPoints);
    	mLocationOverlay = new MyLocationOverlay(getBaseContext(), mapView);
    	mLocationOverlay.enableCompass();
    	mLocationOverlay.enableMyLocation();
    	mapOverlays.add(mPathOverlay);
    	mapOverlays.add(mLocationOverlay);
    	
    }
	
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(1, STARTSTOP_MENU_ITEM, Menu.NONE, R.string.startRecording);
		menu.add(Menu.NONE, SAVE_MENU_ITEM, Menu.NONE, R.string.save);
		menu.add(Menu.NONE, LOAD_MENU_ITEM, Menu.NONE, R.string.load);
		menu.add(4, PICTURE_MENU_ITEM, Menu.NONE, R.string.takePicture);
		menu.add(5, ENABLEGPS_MENU_ITEM, Menu.NONE, R.string.enableGPS);
		
		return true;		
	}
	
	public boolean onPrepareOptionsMenu(Menu menu){
		// Menu button
		if (!mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this,"GPS IS DISABLED", 5000).show();
			menu.setGroupVisible(5, true);
			menu.setGroupEnabled(1, false);
		}else {			
			menu.setGroupEnabled(1, true);
			menu.setGroupVisible(5, false);
		}
		
		if (!m_bRecording) {
			menu.setGroupEnabled(4, false);
		}else {
			menu.setGroupEnabled(4, true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case ENABLEGPS_MENU_ITEM:
			Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(gpsOptionsIntent, ENABLE_GPS_REQUEST_CODE);
			break;
			
		case STARTSTOP_MENU_ITEM:
			if (!m_bRecording) {
				m_bRecording = true;
				item.setTitle(R.string.stopRecording);
			}else {
				m_bRecording = false;
				item.setTitle(R.string.startRecording);
			}			
			setRecordingState(m_bRecording);
			break;
			
		case PICTURE_MENU_ITEM:			
			Intent i = new Intent(this, CameraPreview.class);
			startActivityForResult(i, PICTURE_REQUEST_CODE);
			break;
		
		case SAVE_MENU_ITEM:
			saveRecording();
			break;
			
		case LOAD_MENU_ITEM:
			loadRecording();
		default:
			break;
		}
		return false;
	}
	
		
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		//Log.d("myLogs", "requestCode = " + requestCode + ", ENABLE_GPS_REQUEST_CODE = " + ENABLE_GPS_REQUEST_CODE);
		if (requestCode == ENABLE_GPS_REQUEST_CODE) {
			mLocationOverlay.enableCompass();
	    	mLocationOverlay.enableMyLocation();
		}else {
			Toast.makeText(this, "Wrong result", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void setRecordingState(boolean bRecording) {
		// TODO
		//m_bRecording = true;
		if (bRecording) {
			m_arrPathPoints.clear();
			mapView.postInvalidate();
			Location location = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			onLocationChanged(location);			
			mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_CHANGE, MIN_DISTANCE_CHANGE, this);
		}else {
			mLocManager.removeUpdates(this);
			m_arrPathPoints.clear();
		}
		
	}
	String FILENAME = "Route4";
	private void saveRecording(){
		// TODO		
		deleteFile(FILENAME);
		try {
			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);	
			PrintWriter out = new PrintWriter(fos);
			for (GeoPoint gp : m_arrPathPoints) {
				out.print(gp.getLatitudeE6() + "," + gp.getLongitudeE6()+";");
			}
			out.close();	
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void loadRecording() {
		// TODO
		m_arrPathPoints.clear();		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					openFileInput(FILENAME)));
			String line="";
			String tmp = "";
		    while (( line = br.readLine()) != null) {
		    	//Log.d("3: ", line);	        
		        tmp+=line;
		    }
		    //Log.d("All coordinates: ", tmp);	
		    String[] point_arr = tmp.split(";");
		    for (int i = 0; i <= point_arr.length - 1; i++) {
		    	String[] point_t = point_arr[i].split(",");
		        //Log.d("5:",point_t[0] + " - " + point_t[1]);
		        int x = Integer.parseInt(point_t[0]);
		        int y = Integer.parseInt(point_t[1]); 
		        
		        GeoPoint point = new GeoPoint(x, y);
				m_arrPathPoints.add(point);
				mapView.postInvalidate();
			}		    
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
						
		// конвертируеи из Location в GeoPoint
		int latitude = (int) (location.getLatitude()*GEO_CONVERSION);
		int longitude = (int) (location.getLongitude()*GEO_CONVERSION);
		
		GeoPoint point = new GeoPoint(latitude, longitude);
		m_arrPathPoints.add(point);
		mapView.postInvalidate();
		mMapController.animateTo(point);

		Toast.makeText(this, 
                location.getLatitude()  + "," + 
                location.getLongitude() , 
                Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		setRecordingState(false);
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}