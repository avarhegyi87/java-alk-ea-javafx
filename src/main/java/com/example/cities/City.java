package com.example.cities;

import jakarta.persistence.*;

import java.util.List;

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

    @ManyToOne
    @JoinColumn(
            name = "MEGYEID", referencedColumnName = "ID",
            insertable = false, updatable = false
    )
    public County countyOfCity;

    @OneToMany(mappedBy = "cityForPopulation", cascade = {CascadeType.REMOVE})
    public List<Population> populationList;

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

    public County getCountyOfCity() {
        return countyOfCity;
    }

    public void setCountyOfCity(County county) {
        this.countyOfCity = county;
    }

    public List<Population> getPopulationList() {
        return populationList;
    }

    public void setPopulationList(List<Population> populationList) {
        this.populationList = populationList;
    }
}
