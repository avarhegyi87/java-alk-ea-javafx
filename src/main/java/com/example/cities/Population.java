package com.example.cities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class Population {
    @Id
    @Column(name = "varosid")
    public int CityId;

    @Id
    @Column(name = "ev")
    public int Year;

    @Column(name = "no")
    public int Women;

    @Column(name = "osszesen")
    public int TotalPop;

    public int getCityId() {
        return CityId;
    }

    public void setCityId(int cityId) {
        CityId = cityId;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public int getWomen() {
        return Women;
    }

    public void setWomen(int women) {
        Women = women;
    }

    public int getTotalPop() {
        return TotalPop;
    }

    public void setTotalPop(int totalPop) {
        TotalPop = totalPop;
    }
}
