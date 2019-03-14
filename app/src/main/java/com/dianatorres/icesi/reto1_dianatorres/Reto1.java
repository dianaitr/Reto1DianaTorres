package com.dianatorres.icesi.reto1_dianatorres;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Reto1 extends FragmentActivity implements OnMapReadyCallback, OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private static final int REQUEST_CODE = 11 ;
    private GoogleMap mMap;
    private LocationManager manager;
    private Marker me;

    private TextView txtInfo;
    private String txtNamePlace;
    private LatLng currentLocation;

    private ArrayList<Ubicacion> ubicacionesAgregadas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reto1);

        txtNamePlace="A";
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        ubicacionesAgregadas = new ArrayList<Ubicacion>();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }


    private  Location current;
    private  AlertDialog dialog;


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);


        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMarkerDragListener(this);

        //Agregar el listener de ubicacion
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.e(">>>","LAT: "+location.getLatitude()+ " , LONG: "+location.getLongitude());

                if(me != null){
                    me.remove();
                }
                current= location;

                setLocation();


                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));

                currentLocation= new LatLng(current.getLatitude(),current.getLongitude());


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });



        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                //mostrar Dialogo, agregar nombre de la ubicacion

                AlertDialog.Builder mBuilder= new AlertDialog.Builder(Reto1.this);
                View mView= getLayoutInflater().inflate(R.layout.dialog_nueva_ubicacion,null);

                final EditText nombreLugar= (EditText) mView.findViewById(R.id.nombreLugar);
                Button btn_agregarUb= (Button) mView.findViewById(R.id.btn_agregarUb);

                btn_agregarUb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!nombreLugar.getText().toString().isEmpty()){

                            //agregar ubicacion al arraylist
                            Ubicacion nuevaUbicacion = new Ubicacion(nombreLugar.getText().toString()
                                    ,latLng.latitude,latLng.longitude);
                            ubicacionesAgregadas.add(nuevaUbicacion);


                            //snippet con info de a cúanto estoy del lugar


                            Location lugar= new Location("lugar");
                            lugar.setLatitude(nuevaUbicacion.latitud);
                            lugar.setLongitude(nuevaUbicacion.longitud);

                            float dist= current.distanceTo(lugar);
                            nuevaUbicacion.setDistancia(dist);


                            Toast.makeText(Reto1.this,
                                    "Se ha agregado exitosamente la ubicacion! ",
                                    Toast.LENGTH_SHORT).show();


                            if(dialog!=null){
                                dialog.dismiss();
                            }

                            refrescarMarcadores();

//                            //calcular menor distancia entre ubicaciones
                                Ubicacion ubCercana=encontrarLugarCercano();
//
//                             //mostrar en el cajón a cuanto está de la u
                            if(ubCercana!=null){
                                txtInfo.setText("El lugar más cercano es : "+ubCercana.getNombre());
                            }else{
                                txtInfo.setText("No existen lugares registrados ");
                            }




                        }else{
                            Toast.makeText(Reto1.this, "Porfavor llenar el campo vacío.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                mBuilder.setView(mView);
                dialog= mBuilder.create();
                dialog.show();





            }
        });






    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }


    private ArrayList<Float> distancias= new ArrayList<Float>();


    public void refrescarMarcadores(){



        for(int i=0; i<ubicacionesAgregadas.size();i++){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(ubicacionesAgregadas.get(i).latitud, ubicacionesAgregadas.get(i).longitud))
                    .title(ubicacionesAgregadas.get(i).getNombre() + " "+ "Usted se encuentra a "+ ubicacionesAgregadas.get(i).getDistancia()+" m"));
        }


    }



    //encuentra la ubicacion más cerca a la ubicacion actual
    public Ubicacion encontrarLugarCercano(){

        Ubicacion ubicacionMasCerca=null;

        if(currentLocation!=null && ubicacionesAgregadas.size()>0){


            Location current= new Location("currentLocation");
            current.setLatitude(currentLocation.latitude);
            current.setLongitude(currentLocation.longitude);

            ubicacionMasCerca=ubicacionesAgregadas.get(0);
            Location menorDist=new Location("lugar mas cercano");
            menorDist.setLatitude(ubicacionesAgregadas.get(0).latitud);
            menorDist.setLongitude(ubicacionesAgregadas.get(0).longitud);
            float distMenor=current.distanceTo(menorDist);
//            ubicacionMasCerca.setDistancia(distMenor);

            for (int i=1; i<ubicacionesAgregadas.size();i++){

                Location lugar= new Location("lugar"+i);
                lugar.setLatitude(ubicacionesAgregadas.get(i).latitud);
                lugar.setLongitude(ubicacionesAgregadas.get(i).longitud);

                float dist= current.distanceTo(lugar);
//                ubicacionesAgregadas.get(i).setDistancia(dist);

                if(dist<distMenor){
                    distMenor=dist;
                    ubicacionMasCerca=ubicacionesAgregadas.get(i);
                }

            }


        }

        return ubicacionMasCerca;
    }



    public void setLocation() {
        //Obtener la direccion desde la latitud y la longitud
        if (current.getLatitude() != 0.0 && current.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation( current.getLatitude(), current.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);

                    me = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(current.getLatitude(), current.getLongitude()))
                            .title("Usted se encuentra en Cl."+DirCalle.getAddressLine(0)
                            ).icon(BitmapDescriptorFactory.fromResource( R.drawable.personicon))
                    );
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
