package com.example.cities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Year;

//class PopulationId implements Serializable {
//    private int cityId;
//    private Year year;
//    private int women;
//    private int totalPop;
//
//    public PopulationId(int cityId, Year year, int women, int totalPop) {
//        this.cityId = cityId;
//        this.year = year;
//        this.women = women;
//        this.totalPop = totalPop;
//    }
//}

@Entity
//@IdClass(PopulationId.class)
@Table(name = "lelekszam")
public class Population {
    @Id
    @Column(name = "varosid")
    public int CityId;

    @Id
    @Column(name = "ev")
    public Year Year;

    @Column(name = "no")
    public int Women;

    @Column(name = "osszesen")
    public int TotalPop;

    @ManyToOne
    @JoinColumn(
            name = "VAROSID", referencedColumnName = "ID",
            insertable = false, updatable = false
    )
    public City city;

    public int getCityId() {
        return CityId;
    }

    public void setCityId(int cityId) {
        CityId = cityId;
    }

    public Year getYear() {
        return Year;
    }

    public void setYear(Year year) {
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

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
