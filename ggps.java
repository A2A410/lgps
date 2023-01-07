import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
  private SensorManager gpsGyro;
  private LocationManager gpsLog;
  private TimerTask gpsTime;
  private Date gpsDate;
  private TextView textview1;
  
  // Other code here...
  
  private void registerListeners() {
    gpsGyro.registerListener(_gpsGyro_sensor_listener, gpsGyro.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
    if (Build.VERSION.SDK_INT >= 23) {
      if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        gpsLog.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 3, _gpsLog_location_listener);
      }
    } else {
      gpsLog.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 3, _gpsLog_location_listener);
    }
  }
  
  private void retrieveGpsData() {
    gpsTime = new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // Get information about the satellites that are broadcasting
            List<GpsSatellite> satellites = gpsLog.getGpsStatus(null).getSatellites();
            String satelliteInfo = "";
            for (GpsSatellite satellite : satellites) {
              satelliteInfo += "Satellite: " + satellite.getPrn() + "\n";
              satelliteInfo += "  Elevation: " + satellite.getElevation() + "\n";
              satelliteInfo += "  Azimuth: " + satellite.getAzimuth() + "\n";
              satelliteInfo += "  SNR: " + satellite.getSnr() + "\n";
            }
            
            // Get SBAS (Satellite-Based Augmentation System) enhanced data if available
            GpsSatellite sbasSatellite = gpsLog.getGpsStatus(null).getSatellite(GpsSatellite.SVID_SBAS_SERVICE);
            String sbasInfo = "";
            if (sbasSatellite != null) {
              sbasInfo += "SBAS Satellite: " + sbasSatellite.getPrn() + "\n";
              sbasInfo += "  Elevation: " + sbasSatellite.getElevation() + "\n";
              sbasInfo += "  Azimuth: " + sbasSatellite.getAzimuth() + "\n";
              sbasInfo += "  SNR: " + sbasSatellite.getSnr() + "\n";
            }
            
            // Get the NMEA sentences from the GpsStatus object
            String[] nmeaSentences = gpsLog.getGpsStatus(null).getNmea();
            
            // Create a string with the NMEA sentences
            String nmeaString = "";
            for (String nmea : nmeaSentences) {
              nmeaString += nmea + "\n";
            }
            
            // Create a raw GPS and NMEA data string to use in the database
            String rawData = "";
            if (gpsDate != null) {
              rawData += "GPS Date: " + gpsDate.getTime() + "\n";
            }
            if (gpsLog != null) {
              rawData += "GPS Location: " + gpsLog.getLastKnownLocation(LocationManager.GPS_PROVIDER) + "\n";
              rawData += "GPS NMEA Data: " + nmeaString + "\n";
            }
            
            // Show all of the information in the debug TextView
            textview1.setText(textview1.getText() + "Getting GPS fix...\n");
            textview1.setText(textview1.getText() + "Satellite Info:\n" + satelliteInfo + "\n");
            textview1.setText(textview1.getText() + "SBAS Info:\n" + sbasInfo + "\n");
            textview1.setText(textview1.getText() + "Raw GPS Data:\n" + rawData + "\n");
            
            // Create a log file with the current date and time
            Calendar currentTime = Calendar.getInstance();
            String logFileName = currentTime.get(Calendar.HOUR_OF_DAY) + "." + currentTime.get(Calendar.MINUTE) + "." + currentTime.get(Calendar.SECOND) + "." + currentTime.get(Calendar.MILLISECOND) + ".txt";
            String logData = "GPS Data:\n" + satelliteInfo + "\n" + sbasInfo + "\n" + rawData + "\n";
            FileOutputStream fos = openFileOutput(logFileName, Context.MODE_PRIVATE);
            fos.write(logData.getBytes());
            fos.close();
          }
        });
      }
    };
  }
}
