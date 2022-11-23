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
import java.util.Optional;

public class CitiesController {
    SessionFactory factory;
    @FXML private Label lbTitle, lbWrongNumFormat, lbWrongNumFormatWomen;
    @FXML private GridPane gpAddCity, gpDeleteCity;
    @FXML private TextField tfCityName, tfPopulation, tfWomen;
    @FXML private ComboBox cbCounty, cbDelCity;
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
        gpDeleteCity.setVisible(false);
        gpDeleteCity.setManaged(false);
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
        List<String> countyList = factory.openSession().createQuery("SELECT CountyName FROM County", String.class)
                .getResultList();
        return countyList;
    }

    @FXML
    List<String> getAllCityNames() {
        List<String> cityList = factory.openSession().createQuery("SELECT CityName FROM City", String.class)
                .getResultList();
        return cityList;
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
/*            query = session.createQuery("SELECT Id FROM City WHERE CityName = :cityName");
            query.setParameter("cityName", tfCityName.getText());
            query.setMaxResults(1);*/
            population.cityForPopulation = city;
            population.CityId = city.Id;
            population.Year = Year.now();
            population.TotalPop = numPopulation;
            population.Women = numWomen;
            session.persist(population);
            t.commit();
            session.close();
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
        int result =(int) query.getSingleResult();
        session.close();
        return result;
    }

    @FXML
    public String getCountyNameById(int countyId) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Query query;
        query = session.createQuery("SELECT CountyName FROM County WHERE Id = :countyId");
        query.setParameter("countyId", countyId);
        query.setMaxResults(1);
        String result = (String) query.getSingleResult();
        session.close();
        return result;
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
        session.close();
    }

    @FXML
    protected void menuUpdateCityClick() {
    }

    @FXML
    protected void menuDeleteCityClick() {
        DeleteElements();
        cbDelCity.setItems(FXCollections.observableList(getAllCityNames()));
        lbTitle.setText("Város törlése");
        gpDeleteCity.setVisible(true);
        gpDeleteCity.setManaged(true);
    }

    @FXML
    public void btDeleteCityClick(ActionEvent actionEvent) {
        String cityNameToDelete = (String) cbDelCity.getValue();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Törlés megerősítése");
        alert.setHeaderText("%s véglegesen törlésre kerül a \"varos\" táblából."
                .formatted(cityNameToDelete));
        alert.setContentText("Biztosan folytatja?");
        Optional<ButtonType> result = alert.showAndWait();
        String msg;
        if (result.get() == ButtonType.OK) {
            if (DeleteCity(cityNameToDelete)) {
                msg = "Város törölve az adatbázisból.";
            } else {
                msg = "Törlés megszakítva.";
            }
            DeleteElements();
            cbDelCity.setItems(FXCollections.observableList(getAllCityNames()));
            lbTitle.setVisible(true);
            lbTitle.setManaged(true);
            lbTitle.setText(msg);
            gpDeleteCity.setVisible(true);
            gpDeleteCity.setManaged(true);
        } else {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Megszakítás");
            info.setHeaderText(null);
            info.setContentText("Törlés megszakítva");
            info.showAndWait();
        }
    }

    @FXML
    private boolean DeleteCity(String cityName) {
        try {
            Session session = factory.openSession();
            Transaction t = session.beginTransaction();
            List<City> cityList = session.createQuery("FROM City WHERE CityName = :cityName", City.class)
                    .setParameter("cityName", cityName).getResultList();
            if (cityList.isEmpty()) {
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("Warning");
                warningAlert.setHeaderText("A törlés nem sikerült");
                warningAlert.setContentText("Az adatbázisban nem létezik %s nevű város".formatted(cityName));
                warningAlert.showAndWait();
                return false;
            }
            Integer cityId = cityList.get(0).Id;
            Query query;
            query = session.createQuery("DELETE FROM City WHERE CityName = :cityName")
                    .setParameter("cityName", cityName);
            query.executeUpdate();
            query = session.createQuery("DELETE FROM Population WHERE CityId = :cityId")
                    .setParameter("cityId", cityId);
            query.executeUpdate();
            session.close();
            return true;
        } catch (Exception e) {
            Alert errAlert = new Alert(Alert.AlertType.ERROR);
            errAlert.setTitle("Error Dialog");
            errAlert.setHeaderText("The following error occured:");
            errAlert.setContentText(e.getMessage());
            errAlert.showAndWait();
            return false;
        }
    }
}