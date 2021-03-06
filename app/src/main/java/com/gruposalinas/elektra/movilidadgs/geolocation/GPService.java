package com.gruposalinas.elektra.movilidadgs.geolocation;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.gruposalinas.elektra.movilidadgs.beans.RegistroGPS;
import com.gruposalinas.elektra.movilidadgs.ormlite.DBHelper;
import com.gruposalinas.elektra.movilidadgs.utils.Constants;
import com.gruposalinas.elektra.movilidadgs.utils.Utils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by oemy9 on 03/03/2017.
 */

public class GPService extends Service implements  GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public static final String TAG="GPS_SERVICE";
    private Date date;
    private String dateString;
    private String hourString;
    private DBHelper mDBHelper;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private void startTracking() {
        Log.d(TAG, "startTracking");
        GoogleApiAvailability googleAPI= GoogleApiAvailability.getInstance();
        if (googleAPI.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).
                            addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "GOOGLE PLAY SERVICES NO PARECE ESTAR INSTALADO");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.startTracking();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mDBHelper != null) {
            OpenHelperManager.releaseHelper();
            mDBHelper = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
             Log.e(TAG,"Posición"+location.getLatitude()+","+ location.getLongitude()+"accuracy"+ location.getAccuracy());
             this.muestraPosicionActual(location);
             this.stopUpdates();
        }
    }

    private void stopUpdates(){
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            Log.d(TAG, "CONECTANDO GPS");
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000); //1 SEGUNDO
            locationRequest.setFastestInterval(1000); // EL INTERVALO QUE LA APP PUEDE VOLVER A OBENER LA UBICACIÓN
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
        catch (SecurityException ex){
            Log.e(TAG,"ERROR NO PERMISO LOCALIZACIÓN");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
            Log.e(TAG,"CONEXIÓN SUSPENDIDA");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,"CONEXION FALLIDA:"+connectionResult.getErrorMessage());
    }

    @SuppressLint("LongLogTag")
    public void muestraPosicionActual(Location _location){
        Log.i("gps", "17- muestraPosicionActual");

        Log.i("gps", "18- Latitude = " + _location.getLatitude() + "\nLongitud = " + _location.getLongitude());
        date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.DAY_FORMAT);
        dateFormatter.setLenient(false);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormatter = new SimpleDateFormat(Constants.HOUR_FORMAT);
        timeFormatter.setLenient(false);

        dateString = dateFormatter.format(date);
        hourString = timeFormatter.format(date);


        Log.i("gps", "19- Date = " + dateString + " " + hourString);


        RegistroGPS registroGps = new RegistroGPS(_location.getLatitude(), _location.getLongitude(), getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE).getString(Constants.SP_ID, ""),
                dateString,
                hourString,
                Utils.getBatteryLevel(this),
                Utils.getJsonDate(date),
                Utils.generateMovementId(dateString, hourString),1,_location.getAccuracy());
        registroGps.setJsonDate(generateJsonDate(registroGps));

        onPositionSaved(registroGps);

    }

    @SuppressLint("LongLogTag")
    private String generateJsonDate(RegistroGPS registro){
        //Setting jsonDate
        String jsonDate = "";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);
        Date date = new Date();
        try {
            date = formatter.parse(registro.getFecha() + " " + registro.getHora());
        } catch (java.text.ParseException e) {
            Log.e("ddd", "Error al parsear fecha");
        }
        jsonDate = Utils.getJsonDate(date);
        registro.setJsonDate(jsonDate);

        return jsonDate;
    }

    @SuppressLint("LongLogTag")
    public void onPositionSaved(RegistroGPS result){
        Log.i(TAG, "20- onPositionSaved");
        Log.d(TAG, "20.1 - Registro con id = " + result.getId() + "\nFecha = " + result.getFecha() + " " + result.getHora() + "\nGeoloc = " + result.getLatitud() + "|" + result.getLongitud());
        Dao dao;

        try {
            if(result.isSuccess()){
                Log.i(TAG, "21- Se ha mandado la ubicación exitosamente: " + result.getId());
            }
            else{
                Log.i(TAG, "22- No se ha mandado la ubicación. Guardando en cola. Hora = " + result.getFecha() + " " + result.getHora());
                Log.i(TAG, "22.1- jsonDate = " + result.getJsonDate());
            }

            boolean isNew = true;

            //TODO revisar primero si ya existe el ID, para crearlo o actualizarlo
            //TODO compara el registro con los de BD
            DBHelper dbHelper = null;
            List<RegistroGPS> registrosTemp = null;

            dao = DBHelper.getHelper(getApplicationContext(), mDBHelper).getGpsDAO();


            try {
                Log.i(TAG, "23-Test probando buscar registro con ID = " + result.getId());
                QueryBuilder queryBuilder = dao.queryBuilder();
                queryBuilder.setWhere(queryBuilder.where().eq(RegistroGPS.ID, result.getId()));
                registrosTemp = dao.query(queryBuilder.prepare());

                Log.d(TAG, "24.1- ID = " + registrosTemp.get(0).getId() + "/" + registrosTemp.get(0).isSuccess());
                Log.e(TAG, "Result id = " + result.getId());
                Log.e(TAG, "registroTemp id = " + registrosTemp.get(0).getId());
                if (registrosTemp == null){
                    Log.i(TAG, "25- No se ha encontrado nada");
                }
                else if(registrosTemp.size() == 0 && !registrosTemp.get(0).getId().equals(result.getId())) {
                    Log.i(TAG, "26- Ningún registro con id = " + result.getId());
                }
                else {
                    Log.i(TAG, "27- Recuperado registro con id = " + registrosTemp.get(0).getId());
                    isNew = false;
                }

            }catch(Exception e){
                Log.e(TAG, "28- No se hizo la búsqueda el registro");
            }

            if(isNew){
                Log.i(TAG, "29- Se creará la coordenada: " + result.getJsonDate());
                dao.create(result);
                Log.i(TAG, "30- Coordenada creado exitosamente");
            }
            else{
                if(result.isSuccess()){
                    Log.i(TAG, "31- Se actualizará la coordenada: " + result.getJsonDate());
                    dao.update(result);

                    Log.i(TAG, "32- Coordenada actualizada exitosamente");
                }
                else{
                    Log.i(TAG, "33- Falló el reintento de mandar la solicitud");
                }
            }

        } catch (SQLException e) {
            Log.e(TAG, "33- Error creando coordenada");
        }
    }
}
