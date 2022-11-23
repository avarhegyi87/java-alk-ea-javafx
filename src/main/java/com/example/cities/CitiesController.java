package com.example.cities;

import com.google.gson.*;
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

import java.io.IOException;
import java.time.Year;
import java.util.*;

public class CitiesController {
    SessionFactory factory;
    @FXML private Label lbTitle, lbWrongNumFormat, lbWrongNumFormatWomen;
    @FXML private GridPane gpAddCity, gpDeleteCity;
    @FXML private TextField tfCityName, tfPopulation, tfWomen;
    @FXML private ComboBox cbCounty, cbDelCity;
    @FXML private ToggleGroup groupCountyCapital, groupCountyRights;
    @FXML private RadioButton rbCountyCapitalYes, rbCountyCapitalNo;
    @FXML private RadioButton rbCountyRightsYes, rbCountyRightsNo;
    @FXML private TableView tvCities, tvPersons;
    @FXML private TableColumn<City, String> IdCol;
    @FXML private TableColumn<City, String> NameCol;
    @FXML private TableColumn<County, String> CountyCol;
    @FXML private TableColumn<City, String> CountyCapitalCol;
    @FXML private TableColumn<City, String> CountyRightsCol;
    @FXML private TableColumn<City, String> MostRecentPopulationCol;
    @FXML private TableColumn<Person, String> PersonIdCol, PersonNameCol, PersonEmailCol, PersonGenderCol, PersonStatusCol;

    @FXML
    void initialize() {
        DeleteElements();
        Configuration config = new Configuration().configure("hibernate.cfg.xml");
        factory = config.buildSessionFactory();
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
    void DeleteElements() {
        lbTitle.setVisible(false);
        lbTitle.setManaged(false);
        gpAddCity.setVisible(false);
        gpAddCity.setManaged(false);
        gpDeleteCity.setVisible(false);
        gpDeleteCity.setManaged(false);
        tvCities.setVisible(false);
        tvCities.setManaged(false);
        tvPersons.setVisible(false);
        tvPersons.setManaged(false);
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
    void menuCreateCityClick() {
        DeleteElements();
        cbCounty.setItems(FXCollections.observableList(getAllCountyNames()));
        lbTitle.setText("Város hozzáadása");
        gpAddCity.setVisible(true);
        gpAddCity.setManaged(true);
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
    protected void menuReadCitiesClick() {
        DeleteElements();
        tvCities.setVisible(true);
        tvCities.setManaged(true);
        tvCities.getColumns().removeAll(tvCities.getColumns());
        IdCol = new TableColumn<>("Id");
        NameCol = new TableColumn<>("Település");
        CountyCol = new TableColumn<>("Megye");
        CountyCapitalCol = new TableColumn<>("Megyeszékhely");
        CountyRightsCol = new TableColumn<>("Megyei jogú város");
        MostRecentPopulationCol = new TableColumn<>("Legfrissebb népességi adat");
        tvCities.getColumns().addAll(IdCol, NameCol, CountyCol, CountyCapitalCol, CountyRightsCol, MostRecentPopulationCol);
        IdCol.setCellValueFactory(new PropertyValueFactory<>("Id"));
        NameCol.setCellValueFactory(new PropertyValueFactory<>("CityName"));
        CountyCol.setCellValueFactory(new PropertyValueFactory<>("CountyName"));
        CountyCapitalCol.setCellValueFactory(new PropertyValueFactory<>("CountyCapital"));
        CountyRightsCol.setCellValueFactory(new PropertyValueFactory<>("CountyRights"));
        MostRecentPopulationCol.setCellValueFactory(new PropertyValueFactory<>("MostRecentPopulation"));
        tvCities.getItems().clear();
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
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
            tvCities.getItems().add(detailedCityView);
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

    public void menuCreateGoRestClick() throws IOException {}
    public void menuReadGoRestClick() throws IOException {
        DeleteElements();
        String resp = GoRestClient.GET(null);
        String title = "";
        if (resp == "Hiba!") {
            title = resp;
        } else {
            JsonObject jsonObject = new JsonParser().parse(resp).getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
            List<Person> personList = new ArrayList<>();
            for (JsonElement jsonItem :
                    jsonArray) {
                Person person = new Person();
                person.setId(Integer.parseInt(jsonItem.getAsJsonObject().get("id").toString()));
                person.setName(jsonItem.getAsJsonObject().get("name").toString());
                person.setEmail(jsonItem.getAsJsonObject().get("email").toString());
                person.setGender(jsonItem.getAsJsonObject().get("gender").toString());
                person.setStatus(jsonItem.getAsJsonObject().get("status").toString());
                personList.add(person);
            }
            title = "A GoRest API users lekérdezés eredménye:";
            tvPersons.setVisible(true);
            tvPersons.setManaged(true);
            tvPersons.getColumns().removeAll(tvPersons.getColumns());
            PersonIdCol = new TableColumn<>("Id");
            PersonNameCol = new TableColumn<>("Name");
            PersonEmailCol = new TableColumn<>("E-mail");
            PersonGenderCol = new TableColumn<>("Gender");
            PersonStatusCol = new TableColumn<>("Status");
            tvPersons.getColumns().addAll(PersonIdCol, PersonNameCol, PersonEmailCol, PersonGenderCol, PersonStatusCol);
            PersonIdCol.setCellValueFactory(new PropertyValueFactory<>("Id"));
            PersonNameCol.setCellValueFactory(new PropertyValueFactory<>("Name"));
            PersonEmailCol.setCellValueFactory(new PropertyValueFactory<>("Email"));
            PersonGenderCol.setCellValueFactory(new PropertyValueFactory<>("Gender"));
            PersonStatusCol.setCellValueFactory(new PropertyValueFactory<>("Status"));
            tvPersons.getItems().clear();
            for (Person person :
                    personList) {
                tvPersons.getItems().add(person);
            }
        }
        lbTitle.setText(title);
        lbTitle.setVisible(true);
        lbTitle.setManaged(true);
    }

    public void menuUpdateGoRestClick() throws IOException {}

    public void menuDeleteGoRestClick() throws IOException {}
}