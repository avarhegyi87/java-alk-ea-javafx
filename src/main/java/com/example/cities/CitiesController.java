package com.example.cities;

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

import java.util.List;
import java.util.Objects;

public class CitiesController {
    SessionFactory factory;
    @FXML private Label lbAddCity;
    @FXML private GridPane gpAddCity;
    @FXML private TextField tfCityName;
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
        lbAddCity.setVisible(false);
        lbAddCity.setManaged(false);
        gpAddCity.setVisible(false);
        gpAddCity.setManaged(false);
        tv.setVisible(false);
        tv.setManaged(false);
    }

    @FXML
    void menuCreateCityClick() {
        DeleteElements();
        gpAddCity.setVisible(true);
        gpAddCity.setManaged(true);
    }

    @FXML
    void AddCity() {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();

        RadioButton selectedCountyCapital = (RadioButton)groupCountyCapital.getSelectedToggle();
        String valueCountyCapital = selectedCountyCapital.getText();
        boolean countyCapital = Objects.equals(valueCountyCapital, "Igen");

        RadioButton selectedCountyRights = (RadioButton)groupCountyRights.getSelectedToggle();
        String valueCountyRights = selectedCountyRights.getText();
        boolean countyRights = Objects.equals(valueCountyRights, "Igen");

        City city = new City(tfCityName.getText(), 1, countyCapital, countyRights);
        session.persist(city);
        t.commit();
    }

    @FXML
    public void btAddCityClick(ActionEvent actionEvent) {
        AddCity();
        DeleteElements();
        lbAddCity.setVisible(true);
        lbAddCity.setManaged(true);
        lbAddCity.setText("Adatok elmentve az adatbázisba");
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
        tv.getColumns().addAll(IdCol, NameCol, CountyCol, CountyCapitalCol, CountyRightsCol); // , MostRecentPopulationCol
        IdCol.setCellValueFactory(new PropertyValueFactory<>("Id"));
        NameCol.setCellValueFactory(new PropertyValueFactory<>("CityName"));
        CountyCol.setCellValueFactory(new PropertyValueFactory<>("CountyName"));
        CountyCapitalCol.setCellValueFactory(new PropertyValueFactory<>("CountyCapital"));
        CountyRightsCol.setCellValueFactory(new PropertyValueFactory<>("CountyRights"));
//        MostRecentPopulationCol.setCellValueFactory(new PropertyValueFactory<>("MostRecentPopulation"));
        tv.getItems().clear();
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Query query;
//        query = session.createQuery("SELECT city, county FROM City city JOIN County county ON county.Id = city.CountyId ORDER BY city.Id");
        query = session.createQuery("FROM City");
        List<City> results;
        results = query.list();
        for (City resultRow : results) {
            DetailedCityView detailedCityView = new DetailedCityView();
            detailedCityView.Id = resultRow.Id;
            detailedCityView.CityName = resultRow.CityName;
            detailedCityView.CountyName = resultRow.countyOfCity.CountyName;
            if (resultRow.CountyCapital) detailedCityView.CountyCapital = "igen";
            else detailedCityView.CountyCapital = "nem";
            if (resultRow.CountyRights) detailedCityView.CountyRights = "igen";
            else detailedCityView.CountyRights = "nem";
//            detailedCityView.CityPopulation = resultRow.populationList;
//            detailedCityView.MostRecentPopulation = detailedCityView.FindMostRecentPopulation();
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