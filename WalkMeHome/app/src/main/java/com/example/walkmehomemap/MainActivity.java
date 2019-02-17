//This is the WalkMeHome app
package com.example.walkmehomemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;



import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.MapView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureCollection;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureCollectionLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;

import androidx.annotation.NonNull;

import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private MapView mMapView;

    //reference to location display object
    private LocationDisplay mLocationDisplay;
    private GraphicsOverlay mGraphicsOverlay;
    private ServiceFeatureTable serviceFeatureTable;

    private void createGraphicsOverlay() {
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    }

    private void createGraphics(Point point) {
        //Point point = new Point(-118.69333917997633, 34.032793670122885, SpatialReferences.getWgs84());
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.rgb(226, 119, 40), 10.0f);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2.0f));
        Graphic pointGraphic = new Graphic(point, pointSymbol);
        mGraphicsOverlay.getGraphics().add(pointGraphic);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    handleRouteRequest();
                    return true;
                case R.id.navigation_dashboard:
                    // mTextMessage.setText(R.string.title_dashboard);
                    handleMapRequest();
                    return true;
                case R.id.navigation_notifications:
                    // mTextMessage.setText(R.string.title_notifications);
                    handleMarkRequest();
                    return true;
            }
            return false;
        }


    };

//    public MainActivity(GraphicsOverlay mGraphicsOverlay) {
//        GraphicsOverlay mGraphicsOverlay1 = mGraphicsOverlay;
//    }

    // ----------ICONS --------------------------------
    void handleRouteRequest() {
        Log.i("menu", "Route");

    }

    void handleMapRequest() {
        Log.i("menu", "Map");

    }

    void handleMarkRequest() {
        Log.i("menu", "Mark");
        //createGraphics();
    }

    private void addReportedDataLayer() {
        String url = "https://services9.arcgis.com/JXeVxlIbaMZJUnsl/arcgis/rest/services/sample/FeatureServer";
        serviceFeatureTable = new ServiceFeatureTable(url);//saved to layer
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
        ArcGISMap map = mMapView.getMap(); //gets map Model (Map and data) MapView (Visualize)
        map.getOperationalLayers().add(featureLayer); //automatically displays features >>Arr of layers
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //saves pic of app when not using
        setContentView(R.layout.activity_main); //ref to view
        mMapView = findViewById(R.id.mapView); //
        setupMap();
        addReportedDataLayer();
        createGraphicsOverlay();

        //This is mad because devices that do not have touch cannot use this feature
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPoint = new android.graphics.Point(
                        Math.round(e.getX()),
                        Math.round(e.getY()));
                Point mapPoint = mMapView.screenToLocation(screenPoint);
                Feature feature = serviceFeatureTable.createFeature();
                feature.setGeometry(mapPoint);
                serviceFeatureTable.addFeatureAsync(feature);
                return super.onSingleTapConfirmed(e);
            }
        });


        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setupLocationDisplay();
    }

    @Override
    protected void onPause() {
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    //for location services
    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }

            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (!(ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivity.this, requestPermissions, requestPermissionsCode);
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show(); //Toast shows message
            }
        });

        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
        mLocationDisplay.startAsync();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay.startAsync();
        } else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
        }
    }

    private void createFeatureCollection() {
        if (mMapView != null) {
            FeatureCollection featureCollection = new FeatureCollection();
            FeatureCollectionLayer featureCollectionLayer = new FeatureCollectionLayer(featureCollection);
            mMapView.getMap().getOperationalLayers().add(featureCollectionLayer);
            createPointTable(featureCollection);
        }
    }

    private void createPointTable(FeatureCollection featureCollection) {
        List<Feature> features = new ArrayList<>();
        List<Field> pointFields = new ArrayList<>();
        pointFields.add(Field.createString("Place", "Place Name", 50));
        FeatureCollectionTable pointsTable = new FeatureCollectionTable(pointFields, GeometryType.POINT, SpatialReferences.getWgs84());
        SimpleMarkerSymbol simpleMarkerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFF0000FF, 18);
        SimpleRenderer renderer = new SimpleRenderer(simpleMarkerSymbol);
        pointsTable.setRenderer(renderer);
        featureCollection.getTables().add(pointsTable);
        // Dodger Stadium
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put(pointFields.get(0).getName(), "Malibu Pier");
        Point point1 = new Point(-118.676726, 34.037288, SpatialReferences.getWgs84());
        features.add(pointsTable.createFeature(attributes1, point1));

        // Los Angeles Memorial Coliseum
        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put(pointFields.get(0).getName(), "Malibu Hindi Temple");
        Point point2 = new Point(-118.709726, 34.095097, SpatialReferences.getWgs84());
        features.add(pointsTable.createFeature(attributes2, point2));

        // Staples Center
        Map<String, Object> attributes3 = new HashMap<>();
        attributes3.put(pointFields.get(0).getName(), "Escondido Falls");
        Point point3 = new Point(-118.779438, 34.044211, SpatialReferences.getWgs84());
        features.add(pointsTable.createFeature(attributes3, point3));

        pointsTable.addFeaturesAsync(features);

    }

    private void setupMap() {
        if (mMapView != null) {
            Basemap.Type basemapType = Basemap.Type.STREETS_VECTOR;
            double latitude = 34.09042;
            double longitude = -118.71511; //esri!
            int levelOfDetail = 11;
            ArcGISMap map = new ArcGISMap(basemapType, latitude, longitude, levelOfDetail);
            mMapView.setMap(map);
            //map.getOperationalLayers().add(featureCollectionLayer);
            // *** ADD ***
            //createPointTable(featureCollection);

        }

    }

//    private void addReportedDataLayer() {
//        String url = "https://services9.arcgis.com/JXeVxlIbaMZJUnsl/arcgis/rest/services/sample/FeatureServer";
//        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(url);
//        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
//        ArcGISMap map = mMapView.getMap();
//        map.getOperationalLayers().add(featureLayer);
}

