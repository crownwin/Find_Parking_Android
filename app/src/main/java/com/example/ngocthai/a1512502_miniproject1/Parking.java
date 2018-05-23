package com.example.ngocthai.a1512502_miniproject1;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ngoc Thai on 5/14/2018.
 */

public class Parking{
    private String name;
    private String address;
    private LatLng latLng;
    private String cost;

    public Parking(String name, String address, LatLng latLng, String cost) {
        this.name = name;
        this.address = address;
        this.latLng = latLng;
        this.cost = cost;
    }

    public void editName(String name) {
        this.name = name;
    }

    public void editAddress(String address) {
        this.address = address;
    }

    public void editLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void editCost(String cost) {
        this.cost = cost;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public LatLng getLatlng(){
        return this.latLng;
    }

    public String getCost(){
        return this.cost;
    }
}