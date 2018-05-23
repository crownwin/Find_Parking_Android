package com.example.ngocthai.a1512502_miniproject1;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Ngoc Thai on 5/15/2018.
 */

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;
    public List<LatLng> points;
}
