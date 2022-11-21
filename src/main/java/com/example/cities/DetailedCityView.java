package com.example.cities;

import java.util.List;

public class DetailedCityView {
    public int Id;
    public String CityName;
    public String CountyName;
    public String CountyCapital;
    public String CountyRights;
    public List<Population> CityPopulation;
    public int MostRecentPopulation;

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

    public String getCountyName() {
        return CountyName;
    }

    public void setCountyName(String countyName) {
        CountyName = countyName;
    }

    public String getCountyCapital() {
        return CountyCapital;
    }

    public void setCountyCapital(String countyCapital) {
        CountyCapital = countyCapital;
    }

    public String getCountyRights() {
        return CountyRights;
    }

    public void setCountyRights(String countyRights) {
        CountyRights = countyRights;
    }

    public List<Population> getCityPopulation() {
        return CityPopulation;
    }

    public void setCityPopulation(List<Population> cityPopulation) {
        CityPopulation = cityPopulation;
    }

    public int getMostRecentPopulation() {
        return MostRecentPopulation;
    }

    public void setMostRecentPopulation(int mostRecentPopulation) {
        MostRecentPopulation = mostRecentPopulation;
    }

    public int FindMostRecentPopulation() {
        Population maxYear = new Population();
        for (Population population : this.CityPopulation) {
            if (maxYear.Year.compareTo(population.Year) > 0) {
                maxYear = population;
            }
        }
        return maxYear.TotalPop;
    }
}
