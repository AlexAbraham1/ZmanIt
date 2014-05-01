package com.alexabraham.zmanit.app;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.hebrewcalendar.HebrewDateFormatter;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;


public class GetZmanimMap extends AsyncTask<Void, Void, Map<String, Object>> {

    WeakReference<MainFragment> mParentActivity;
    Context mContext;
    Map<String, Object> map;

    private JSONTools jtools;
    private JewishCalendar jcal;
    private HebrewDateFormatter hdate;
    private Map<String, Object> zmanimMap;

    private SwipeRefreshLayout mRefreshLayout;

    private boolean isConnected;

    public GetZmanimMap(MainFragment parentActivity, Map<String, Object> zmanimMap, boolean connected){
        super();
        mParentActivity = new WeakReference<MainFragment>(parentActivity);
        mContext = parentActivity.getActivity().getApplicationContext();
        map = zmanimMap;

        jtools = new JSONTools();
        jcal = new JewishCalendar();
        hdate = new HebrewDateFormatter();
        hdate.setHebrewFormat(true);
        isConnected = connected;

    }

    @Override
    protected void onPreExecute(){
        if(!isConnected){
            String message = "No Network Available";
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            cancel(true);
        }
        else {
            String message = "Downloading Zmanim from Network";
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Map<String, Object> doInBackground(final Void... args) {

        zmanimMap = null;


        try {
            zmanimMap = getZmanimMapFromIP();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }

        if (null != zmanimMap) {
            for (String value : zmanimMap.keySet()) {
                map.put(value, zmanimMap.get(value));
            }
        }

        return zmanimMap;
    }

    @Override
    protected void onPostExecute(Map<String, Object> result){

        if (zmanimMap != null) {
            mParentActivity.get().updateList();
            //Tell SwipeRefreshLayout to stop refresh animation
            mParentActivity.get().completeSwipeRefresh();
        }

    }




    private double getElevation(Double longitude, Double latitude) {
        double result = Double.NaN;
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String url = "http://gisdata.usgs.gov/"
                + "xmlwebservices2/elevation_service.asmx/"
                + "getElevation?X_Value=" + String.valueOf(longitude)
                + "&Y_Value=" + String.valueOf(latitude)
                + "&Elevation_Units=METERS&Source_Layer=-1&Elevation_Only=true";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                int r = -1;
                StringBuffer respStr = new StringBuffer();
                while ((r = instream.read()) != -1)
                    respStr.append((char) r);
                String tagOpen = "<double>";
                String tagClose = "</double>";
                if (respStr.indexOf(tagOpen) != -1) {
                    int start = respStr.indexOf(tagOpen) + tagOpen.length();
                    int end = respStr.indexOf(tagClose);
                    String value = respStr.substring(start, end);
                    result = Double.parseDouble(value);
                }
                instream.close();
            }
        } catch (ClientProtocolException e) {}
        catch (IOException e) {}
        return result;
    }

    private String standardDate(Date date) throws java.text.ParseException {
        String result = (new SimpleDateFormat("EEE, MMM d, yyyy").format(date));
        return result;
    }

    public Map<String, Object> getZmanimMapFromIP() throws java.text.ParseException, IOException, org.json.simple.parser.ParseException {
        Map<String, Object> zmanimMap = new LinkedHashMap<String, Object>();



        Map jsonMap = jtools.getFromIP();
        String city = jtools.getFromMap(jsonMap, "city");
        String state = jtools.getFromMap(jsonMap, "region_code");
        String locationName = city + ", " + state;
        double latitude = Double.parseDouble(jtools.getFromMap(jsonMap, "latitude")); //latitude based on GeoLocation
        Log.i("TAG - LATITUDE", "" + latitude);
        double longitude = Double.parseDouble(jtools.getFromMap(jsonMap, "longitude")); //longitude based on Geolocation
        Log.i("TAG - LONGITUDE", "" + longitude);
        double elevation = getElevation(longitude, latitude); //elevation based on Geolocation
        //use a Valid Olson Database timezone listed in java.util.TimeZone.getAvailableIDs()
        TimeZone timeZone = TimeZone.getDefault();
        //create the location object
        GeoLocation location = new GeoLocation(locationName, latitude, longitude, elevation, timeZone);
        //create the ComplexZmanimCalendar
        ComplexZmanimCalendar czc = new ComplexZmanimCalendar(location);

        zmanimMap.put("mTitleView", "Zmanim for " + locationName);
        zmanimMap.put("mDateView", "Date: " + standardDate(jcal.getTime()) + " | " + hdate.format(jcal));
        zmanimMap.put("Alot Hashachar (Dawn):", czc.getAlosHashachar());
        zmanimMap.put("Earliest Tallit & Tefillin:", czc.getMisheyakir10Point2Degrees());
        zmanimMap.put("Sunrise:", czc.getSunrise());
        zmanimMap.put("Sof Zman Shema MGA:", czc.getSofZmanShmaMGA());
        zmanimMap.put("Sof Zman Shema GRA:", czc.getSofZmanShmaGRA());
        zmanimMap.put("Chatzot (Midday):", czc.getChatzos());
        zmanimMap.put("Mincha Gedola (Earliest Mincha):", czc.getMinchaGedola());
        zmanimMap.put("Plag Hamincha:", czc.getPlagHamincha());
        if (jcal.isErevYomTov() || jcal.getDayOfWeek() == 6) {
            int candleLightingOffset = (int) czc.getCandleLightingOffset();
            zmanimMap.put("Candle Lighting: " + " (" +
                            candleLightingOffset + " minutes before sunset)",
                    czc.getCandleLighting());

        }
        zmanimMap.put("Sunset:", czc.getSunset());
        zmanimMap.put("Nightfall (3 Stars)", czc.getTzaisAteretTorah());
        zmanimMap.put("Nightfall (72 Minutes)", czc.getTzais72Zmanis());

        return zmanimMap;
    }
}
