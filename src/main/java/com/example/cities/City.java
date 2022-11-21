package com.example.cities;

import jakarta.persistence.*;

@Entity
@Table(name = "varos")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int Id;

    @Column(name = "nev")
    public String CityName;

    @Column(name = "megyeid")
    public int CountyId;

    @Column(name = "megyeszekhely")
    public boolean CountyCapital;

    @Column(name = "megyeijogu")
    public boolean CountyRights;

    public City() {
    }

    public City(String cityName, int countyId, boolean countyCapital, boolean countyRights) {
        CityName = cityName;
        CountyId = countyId;
        CountyCapital = countyCapital;
        CountyRights = countyRights;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public int getCountyId() {
        return CountyId;
    }

    public void setCountyId(int countyId) {
        CountyId = countyId;
    }

    public boolean isCountyCapital() {
        return CountyCapital;
    }

    public void setCountyCapital(boolean countyCapital) {
        CountyCapital = countyCapital;
    }

    public boolean isCountyRights() {
        return CountyRights;
    }

    public void setCountyRights(boolean countyRights) {
        CountyRights = countyRights;
    }
}
