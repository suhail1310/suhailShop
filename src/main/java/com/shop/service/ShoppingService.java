package com.shop.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.bean.Results;
import com.shop.bean.ShopArray;
import com.shop.bean.ShopDetails;
import com.shop.bean.ShopLocation;

public class ShoppingService {

    String Api_key;
    ObjectMapper objectMapper;
    public ShoppingService(){
        objectMapper = new ObjectMapper();
        Api_key = "AIzaSyAp4g7MmJDUMMiob1_Gec7XjLFE86fuajQ";
    }
    /**
     * @param requestSD
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<ShopLocation> getShopLocation(String requestSD) throws JsonParseException, JsonMappingException, IOException{
        ShopArray shopArray = objectMapper.readValue(
            requestSD.getBytes(Charset.forName("UTF-8")), ShopArray.class);
        List<ShopLocation> response = addresstoString(shopArray.getShopDetails());
        return response;
    }

    /**
     * @param shopDetailsAry
     * @return
     * @throws IOException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws MalformedURLException
     * @throws ProtocolException
     */
    private List<ShopLocation> addresstoString(ShopDetails[] shopDetailsAry) throws IOException,
    JsonParseException,
    JsonMappingException,
    MalformedURLException,
    ProtocolException {

        List<ShopLocation> resLatLongList= new ArrayList<ShopLocation>();
        for (ShopDetails shopDetails : shopDetailsAry) {
            StringBuffer sb = new StringBuffer();

            if (shopDetails.getShopNumber() != null) {
                sb.append(shopDetails.getShopNumber());
            }
            if (shopDetails.getShopAddress() != null) {
                if (shopDetails.getShopAddress().getNumber() != null)

                {
                    sb.append(shopDetails.getShopAddress().getNumber());

                }
                if (shopDetails.getShopAddress().getPostalCode() != 0) {

                    sb.append(shopDetails.getShopAddress().getPostalCode());
                }
            }

            String addressRaw = sb.toString();
            String addressFormated = addressRaw.replaceAll("\\s", "%20");

            URL url =
                new URL("https://maps.googleapis.com/maps/api/geocode/json?address="
                    + addressFormated + "&key=" + Api_key);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException(
                    "Failed : HTTP error code : " + conn.getResponseCode());
            }
            System.out.println("url \n" + url);
            BufferedReader br =
                new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder apiOutput = new StringBuilder();
            String readLine;
            while ((readLine = br.readLine()) != null) {
                System.out.println(readLine);
                apiOutput.append(readLine.trim());
            }
            String jsonInString = apiOutput.toString();
            System.out.println("Output response " + jsonInString);
            conn.disconnect();

            ShopLocation results = objectMapper.readValue(
                jsonInString.getBytes(Charset.forName("UTF-8")), ShopLocation.class);

            System.out.println("address...." + results.getResults());
            resLatLongList.add(results);

        }


        return resLatLongList;
    }

    /**
     * @param locationsReq
     * @return
     */
    public String getShortestDistance(ShopLocation locationsReq) {

       //String customerAddress= locationsReq.getCustomerDetails().getCustomerAddress();
       float customerLat= locationsReq.getCustomerDetails().getLatitude();
       float customerLon= locationsReq.getCustomerDetails().getLongitute();

       Results[] resultAry= locationsReq.getResults();
       TreeMap<Double,String> addressDistance= shortestDistance(resultAry, customerLat,customerLon);
       addressDistance.forEach((k,v)->{
           System.out.println("Distance : " + k + " Address : " + v);

       });

         double distance=addressDistance.firstKey();
         String address= addressDistance.get(distance);

       System.out.println("Shortest distance address : " + address);

       return address;
    }
    /**
     * @param resultAry
     * @param customerLat
     * @param customerLon
     * @return
     */
    private TreeMap<Double,String> shortestDistance(Results[] resultAry, float customerLat,
                                    float customerLon) {
        TreeMap<Double,String> distanceMap= new TreeMap<Double,String>();
        for (Results results : resultAry) {
            String address = results.getFormattedAddress();
            float latitShop= results.getGeometry().getLocation().getLatitude();
            float longiShop= results.getGeometry().getLocation().getLongitute();

            double distance= distance(latitShop, longiShop, customerLat, customerLon, "K");
            distanceMap.put(distance, address);
        }
        return distanceMap;
    }

    /**
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param unit
     * @return
     */
    private double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    This function converts decimal degrees to radians                        :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::    This function converts radians to decimal degrees                        :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
