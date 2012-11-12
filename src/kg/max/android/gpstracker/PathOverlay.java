package kg.max.android.gpstracker;

import java.util.Iterator;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class PathOverlay extends Overlay {

	private final List<GeoPoint> m_arrPathPoints;
	
	private Point m_point;
	private Point m_point2;
	private Paint mPaint;
	private RectF m_rect;
	
	private static final int START_RADIUS = 10;
	private static final int PATH_WIDTH = 6;
	

	public PathOverlay(List<GeoPoint> pathPoints) {
		super();
    	// TODO
		m_arrPathPoints = pathPoints;
		m_point = new Point();
		m_point2 = new Point();
		mPaint = new Paint();
		m_rect = new RectF();
		
		
	}
		
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    	// TODO
		if(m_arrPathPoints.size() != 0){
			//draw strat green circle
			mPaint.setARGB(255, 0, 255, 0);
			Projection projection = mapView.getProjection();
			projection.toPixels(m_arrPathPoints.get(0), m_point);
			//Log.d("myLogs", "start point = " + m_arrPathPoints.get(0));			
			m_rect.set(m_point.x-START_RADIUS, m_point.y-START_RADIUS, m_point.x+START_RADIUS, 
					m_point.y+START_RADIUS);
			canvas.drawOval(m_rect, mPaint);
			
			//draw route path
			mPaint.setStrokeWidth(PATH_WIDTH);
			mPaint.setARGB(255, 255, 0, 0);
			for (GeoPoint geoPoint : m_arrPathPoints) {
				projection.toPixels(geoPoint, m_point2);
				canvas.drawLine(m_point.x, m_point.y, m_point2.x, m_point2.y, mPaint);
				m_point.set(m_point2.x, m_point2.y);
			}
		}
		
		
		
	}
}
