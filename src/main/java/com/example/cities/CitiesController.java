package com.example.cities;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.time.Year;
import java.util.List;
import java.util.Objects;

public class CitiesController {
    SessionFactory factory;
    @FXML private Label lbTitle, lbWrongNumFormat, lbWrongNumFormatWomen;
    @FXML private GridPane gpAddCity;
    @FXML private TextField tfCityName, tfPopulation, tfWomen;
    @FXML private ComboBox cbCounty;
    @FXML private ToggleGroup groupCountyCapital, groupCountyRights;
    @FXML private RadioButton rbCountyCapitalYes, rbCountyCapitalNo;
    @FXML private RadioButton rbCountyRightsYes, rbCountyRightsNo;
    @FXML private TableView tv;
    @FXML private TableColumn<City, String> IdCol;
    @FXML private TableColumn<City, String> NameCol;
    @FXML private TableColumn<County, String> CountyCol;
    @FXML private TableColumn<City, String> CountyCapitalCol;
    @FXML private TableColumn<City, String> CountyRightsCol;
    @FXML private TableColumn<City, String> MostRecentPopulationCol;

    @FXML
    void initialize() {
        DeleteElements();
        Configuration config = new Configuration().configure("hibernate.cfg.xml");
        factory = config.buildSessionFactory();
    }

    @FXML
    void DeleteElements() {
        lbTitle.setVisible(false);
        lbTitle.setManaged(false);
        gpAddCity.setVisible(false);
        gpAddCity.setManaged(false);
        tv.setVisible(false);
        tv.setManaged(false);
    }

    @FXML
    void menuCreateCityClick() {
        DeleteElements();
        cbCounty.setItems(FXCollections.observableList(getAllCountyNames()));
        lbTitle.setText("Város hozzáadása");
        gpAddCity.setVisible(true);
        gpAddCity.setManaged(true);
    }

    @FXML
    List<String> getAllCountyNames() {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        List<String> countyList = session.createQuery("SELECT CountyName FROM County", String.class).getResultList();
        return countyList;
    }

    @FXML
    String AddCity() {
        int numPopulation;
        try {
            numPopulation = Integer.parseInt(tfPopulation.getText());
        } catch (NumberFormatException nfe) {
            lbWrongNumFormat.setVisible(true);
            return "Helytelen számformátum!";
        }
        int numWomen;
        try {
            numWomen = Integer.parseInt(tfWomen.getText());
        } catch (NumberFormatException nfe) {
            lbWrongNumFormatWomen.setVisible(true);
            return "Helytelen számformátum!";
        }
        try {
            Session session = factory.openSession();
            Transaction t = session.beginTransaction();

            RadioButton selectedCountyCapital = (RadioButton) groupCountyCapital.getSelectedToggle();
            String valueCountyCapital = selectedCountyCapital.getText();
            boolean countyCapital = Objects.equals(valueCountyCapital, "Igen");

            RadioButton selectedCountyRights = (RadioButton) groupCountyRights.getSelectedToggle();
            String valueCountyRights = selectedCountyRights.getText();
            boolean countyRights = Objects.equals(valueCountyRights, "Igen");

            City city = new City(
                    tfCityName.getText(),
                    getCountyIdByName((String) cbCounty.getValue()),
                    countyCapital,
                    countyRights
            );
            Query query;
            int countyId = getCountyIdByName((String) cbCounty.getValue());
            if (countyId == 0) {
                return "%s megye nem található az adatbázisban".formatted(cbCounty.getValue());
            }
            List<City> cityFound = session.createQuery(
                    "FROM City WHERE CityName = :cityName AND CountyId = :countyId", City.class)
                    .setParameter("cityName", tfCityName.getText())
                    .setParameter("countyId", countyId)
                    .getResultList();
            if (cityFound.size() > 0) {
                return "%s nevű város %s nevű megyében már létezik az adatbázisban!"
                        .formatted(tfCityName.getText(), cbCounty.getValue());
            }

            session.persist(city);
            t.commit();

            t = session.beginTransaction();
            Population population = new Population();
            query = session.createQuery("SELECT Id FROM City WHERE CityName = :cityName");
            query.setParameter("cityName", tfCityName.getText());
            query.setMaxResults(1);
            population.CityId = (int) query.getSingleResult();
            population.city = city;
            population.Year = Year.now();
            population.TotalPop = numPopulation;
            population.Women = numWomen;
            session.persist(population);
            t.commit();
            return "";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @FXML
    public void btAddCityClick(ActionEvent actionEvent) {
        String addResult = AddCity();
        lbTitle.setVisible(true);
        lbTitle.setManaged(true);
        if (addResult == "") {
            DeleteElements();
            lbTitle.setVisible(true);
            lbTitle.setManaged(true);
            lbTitle.setText("Adatok sikeresen elmentve az adatbázisba");
        } else {
            lbTitle.setText(addResult);
            if (!addResult.equals("Helytelen számformátum!")) {
                lbWrongNumFormat.setVisible(false);
                lbWrongNumFormat.setManaged(false);
                lbWrongNumFormatWomen.setVisible(false);
                lbWrongNumFormatWomen.setVisible(false);
            }
        }
    }

    @FXML
    public int getCountyIdByName(String countyName) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Query query;
        query = session.createQuery("SELECT Id FROM County WHERE CountyName = :countyName");
        query.setParameter("countyName", countyName);
        query.setMaxResults(1);
        return (int) query.getSingleResult();
    }

    @FXML
    public String getCountyNameById(int countyId) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Query query;
        query = session.createQuery("SELECT CountyName FROM County WHERE Id = :countyId");
        query.setParameter("countyId", countyId);
        query.setMaxResults(1);
        return (String) query.getSingleResult();
    }

    @FXML
    protected void menuReadCitiesClick() {
        DeleteElements();
        tv.setVisible(true);
        tv.setManaged(true);
        tv.getColumns().removeAll(tv.getColumns());
        IdCol = new TableColumn<>("Id");
        NameCol = new TableColumn<>("Település");
        CountyCol = new TableColumn<>("Megye");
        CountyCapitalCol = new TableColumn<>("Megyeszékhely");
        CountyRightsCol = new TableColumn<>("Megyei jogú város");
        MostRecentPopulationCol = new TableColumn<>("Legfrissebb népességi adat");
        tv.getColumns().addAll(IdCol, NameCol, CountyCol, CountyCapitalCol, CountyRightsCol, MostRecentPopulationCol);
        IdCol.setCellValueFactory(new PropertyValueFactory<>("Id"));
        NameCol.setCellValueFactory(new PropertyValueFactory<>("CityName"));
        CountyCol.setCellValueFactory(new PropertyValueFactory<>("CountyName"));
        CountyCapitalCol.setCellValueFactory(new PropertyValueFactory<>("CountyCapital"));
        CountyRightsCol.setCellValueFactory(new PropertyValueFactory<>("CountyRights"));
        MostRecentPopulationCol.setCellValueFactory(new PropertyValueFactory<>("MostRecentPopulation"));
        tv.getItems().clear();
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Query query;
//        query = session.createQuery("SELECT city, county FROM City city JOIN County county ON county.Id = city.CountyId ORDER BY city.Id");
        List<City> results = session.createQuery("FROM City", City.class).getResultList();

        for (City resultRow : results) {
            DetailedCityView detailedCityView = new DetailedCityView();
            detailedCityView.Id = resultRow.Id;
            detailedCityView.CityName = resultRow.CityName;
            detailedCityView.CountyName = resultRow.countyOfCity.CountyName;
            if (resultRow.CountyCapital) detailedCityView.CountyCapital = "igen";
            else detailedCityView.CountyCapital = "nem";
            if (resultRow.CountyRights) detailedCityView.CountyRights = "igen";
            else detailedCityView.CountyRights = "nem";
            detailedCityView.CityPopulation = resultRow.populationList;
            detailedCityView.MostRecentPopulation = detailedCityView.FindMostRecentPopulation();
            tv.getItems().add(detailedCityView);
        }
        System.out.println();
        t.commit();
    }

    @FXML
    protected void menuUpdateCityClick() {
    }

    @FXML
    protected void menuDeleteCityClick() {
    }
}