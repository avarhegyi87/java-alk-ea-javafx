package com.example.cities;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import javafx.beans.property.SimpleStringProperty;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Utils;

import java.io.IOException;
import java.sql.*;
import java.time.Year;
import java.util.*;


public class CitiesController {
    SessionFactory factory;
    Timer tr = new Timer();
    Timer tr1 = new Timer();
    public List<Mindenegyben> mlist = new ArrayList<>();
    boolean tFlag = false;
    int c1 = 0;
    int c2 = 0;
    @FXML
    private Label lbTitle, lbWrongNumFormat, lbWrongNumFormatWomen, lbFirst, lbSecond, lbML1, lbML2;
    @FXML
    private TextArea taMlalg;

    @FXML
    private GridPane gpAddCity, gpDeleteCity, gpUser, gpParallel, gpStream, gpML1, gpML2;
    @FXML
    private TextField tfCityName, tfPopulation, tfWomen, tfUserName, tfUserEmail;
    @FXML
    private ComboBox cbCounty, cbDelCity, cbSelectUser, cbMnevek, cbVnevek, cbAlg;
    @FXML
    private ToggleGroup groupCountyCapital, groupCountyRights, groupStatuses, groupGenders;
    @FXML
    private RadioButton rbCountyCapitalYes, rbCountyCapitalNo;
    @FXML
    private RadioButton rbCountyRightsYes, rbCountyRightsNo;
    @FXML
    public RadioButton rbGenderMale, rbGenderFemale, rbStatusActive, rbStatusInactive;
    @FXML
    public Button btUpdateUser, btDeleteUser, btAddUser, btStartblink, btStopblink, btStartstr;
    @FXML
    private TableView tvCities, tvPersons, tvMinden;
    @FXML
    private TableColumn<City, String> IdCol;
    @FXML
    private TableColumn<City, String> NameCol;
    @FXML
    private TableColumn<County, String> CountyCol;
    @FXML
    private TableColumn<City, String> CountyCapitalCol;
    @FXML
    private TableColumn<City, String> CountyRightsCol;
    @FXML
    private TableColumn<City, String> MostRecentPopulationCol;
    @FXML
    private TableColumn<User, String> UserIdCol, UserNameCol, UserEmailCol, UserGenderCol, UserStatusCol;
    @FXML
    private TableColumn<Mindenegyben, String> mNev, vNev, mSzekh, mJog, datum, lakosok, nLakosok, fLakosok;

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
        gpML1.setVisible(false);
        gpML1.setManaged(false);
        lbFirst.setVisible(false);
        lbSecond.setVisible(false);
        btStartblink.setVisible(false);
        btStopblink.setVisible(false);
        gpAddCity.setVisible(false);
        gpAddCity.setManaged(false);
        gpStream.setVisible(false);
        gpStream.setManaged(false);
        gpML2.setVisible(false);
        gpML2.setManaged(false);
        gpParallel.setVisible(false);
        gpParallel.setManaged(false);
        gpDeleteCity.setVisible(false);
        gpDeleteCity.setManaged(false);
        gpUser.setVisible(false);
        gpUser.setManaged(false);
        tfCityName.setText("");
        tfPopulation.setText("");
        tfWomen.setText("");
        tvCities.setVisible(false);
        tvCities.setManaged(false);
        tvPersons.setVisible(false);
        tvPersons.setManaged(false);
        tfUserName.setDisable(false);
        tfUserEmail.setDisable(false);
        tfUserName.setText("");
        tfUserEmail.setText("");
        rbGenderMale.setDisable(false);
        rbGenderFemale.setDisable(false);
        rbStatusActive.setDisable(false);
        rbStatusInactive.setDisable(false);
        cbMnevek.setVisible(false);
        cbVnevek.setVisible(false);
        btStartstr.setVisible(false);
        tvMinden.setVisible(false);
        tvMinden.setManaged(false);
        taMlalg.setVisible(false);
        taMlalg.setManaged(false);
    }

    @FXML
    public int getCountyIdByName(String countyName) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Query query;
        query = session.createQuery("SELECT Id FROM County WHERE CountyName = :countyName");
        query.setParameter("countyName", countyName);
        query.setMaxResults(1);
        int result = (int) query.getSingleResult();
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
        if (Objects.equals(addResult, "")) {
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
                msg = "Hiba a törlés közben.";
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

    public void menuCreateGoRestClick() throws IOException {
        DeleteElements();
        gpUser.setVisible(true);
        gpUser.setManaged(true);
        cbSelectUser.setVisible(false);
        cbSelectUser.setManaged(false);
        btAddUser.setVisible(true);
        btAddUser.setManaged(true);
        btUpdateUser.setVisible(false);
        btUpdateUser.setManaged(false);
        btDeleteUser.setVisible(false);
        btDeleteUser.setManaged(false);
    }

    public void btAddUserClick(ActionEvent actionEvent) throws IOException {
        String gender = ((RadioButton) groupGenders.getSelectedToggle()).getText();
        String status = ((RadioButton) groupStatuses.getSelectedToggle()).getText();
        String resp = GoRestClient.POST(tfUserName.getText(), gender, tfUserEmail.getText(), status);
        if (resp.equals("Hiba!")) {
            lbTitle.setText(resp);
            lbTitle.setVisible(true);
            lbTitle.setManaged(true);
        } else {
            JsonObject jsonObject = new JsonParser().parse(resp).getAsJsonObject().get("data").getAsJsonObject();

            Alert successMsg = new Alert(Alert.AlertType.CONFIRMATION);
            successMsg.setTitle("Sikeres mentés");
            successMsg.setHeaderText("A felhasználót sikeresen elmentettük az adatbázisba");
            successMsg.setContentText("Név: %s\nEmail: %s\nNem: %s\nStátusz: %s"
                    .formatted(jsonObject.get("id"), jsonObject.get("name"), jsonObject.get("email"), jsonObject.get("status")));
            successMsg.showAndWait();
            menuReadGoRestClick();
        }
    }

    public List<User> getAllUsersInList(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
        List<User> userList = new ArrayList<>();
        for (JsonElement jsonItem :
                jsonArray) {
            User user = new User();
            user.setId(Integer.parseInt(jsonItem.getAsJsonObject().get("id").toString()));
            user.setName(jsonItem.getAsJsonObject().get("name").toString());
            user.setEmail(jsonItem.getAsJsonObject().get("email").toString());
            user.setGender(jsonItem.getAsJsonObject().get("gender").toString());
            user.setStatus(jsonItem.getAsJsonObject().get("status").toString());
            userList.add(user);
        }
        return userList;
    }

    public void menuReadGoRestClick() throws IOException {
        DeleteElements();
        String resp = GoRestClient.GET(null);
        String title = "";
        if (resp.equals("Hiba!")) {
            title = resp;
        } else {
            List<User> userList = getAllUsersInList(resp);
            title = "A GoRest API users lekérdezés eredménye:";
            tvPersons.setVisible(true);
            tvPersons.setManaged(true);
            tvPersons.getColumns().removeAll(tvPersons.getColumns());
            UserIdCol = new TableColumn<>("Id");
            UserNameCol = new TableColumn<>("Name");
            UserEmailCol = new TableColumn<>("E-mail");
            UserGenderCol = new TableColumn<>("Gender");
            UserStatusCol = new TableColumn<>("Status");
            tvPersons.getColumns().addAll(UserIdCol, UserNameCol, UserEmailCol, UserGenderCol, UserStatusCol);
            UserIdCol.setCellValueFactory(new PropertyValueFactory<>("Id"));
            UserNameCol.setCellValueFactory(new PropertyValueFactory<>("Name"));
            UserEmailCol.setCellValueFactory(new PropertyValueFactory<>("Email"));
            UserGenderCol.setCellValueFactory(new PropertyValueFactory<>("Gender"));
            UserStatusCol.setCellValueFactory(new PropertyValueFactory<>("Status"));
            tvPersons.getItems().clear();
            for (User user : userList) {
                tvPersons.getItems().add(user);
            }
        }
        lbTitle.setText(title);
        lbTitle.setVisible(true);
        lbTitle.setManaged(true);
    }

    public void fillUserComboBox() throws IOException {
        String resp = GoRestClient.GET(null);
        List<User> userList = getAllUsersInList(resp);
        List<String> userIdsAndNames = new ArrayList<>();
        for (User user : userList) {
            userIdsAndNames.add("%s - %s".formatted(user.getId(), StripJsonFromQuotes(user.getName())));
        }
        cbSelectUser.setItems(FXCollections.observableList(userIdsAndNames));
        cbSelectUser.setVisible(true);
        cbSelectUser.setManaged(true);
    }

    private String StripJsonFromQuotes(String str) {
        return str.substring(1, str.length() - 1);
    }

    public void cbUserSelectAction(ActionEvent actionEvent) throws IOException {
        String id = (String) cbSelectUser.getValue();
        if (id != null) {
            id = id.substring(0, id.indexOf(" "));
            String resp = GoRestClient.GET(id);
            if (resp.equals("Hiba!")) {
                lbTitle.setText("Hiba!");
                lbTitle.setVisible(true);
                lbTitle.setManaged(true);
            } else {
                JsonObject jsonObject = new JsonParser().parse(resp).getAsJsonObject().get("data").getAsJsonObject();
                tfUserName.setText(StripJsonFromQuotes(String.valueOf(jsonObject.get("name"))));
                tfUserEmail.setText(StripJsonFromQuotes(String.valueOf(jsonObject.get("email"))));
                if (StripJsonFromQuotes(String.valueOf(jsonObject.get("gender"))).equals("male")) {
                    rbGenderMale.setSelected(true);
                } else {
                    rbGenderFemale.setSelected(true);
                }
                if (StripJsonFromQuotes(String.valueOf(jsonObject.get("status"))).equals("active")) {
                    rbStatusActive.setSelected(true);
                } else {
                    rbStatusInactive.setSelected(true);
                }
            }
        }
    }

    public void menuUpdateGoRestClick() throws IOException {
        DeleteElements();
        gpUser.setVisible(true);
        gpUser.setManaged(true);
        fillUserComboBox();
        btAddUser.setVisible(false);
        btAddUser.setManaged(false);
        btUpdateUser.setVisible(true);
        btUpdateUser.setManaged(true);
        btDeleteUser.setVisible(false);
        btDeleteUser.setManaged(false);
    }

    public void btUpdateUserClick() throws IOException {
        String id = (String) cbSelectUser.getValue();
        if (id != null) {
            id = id.substring(0, id.indexOf(" "));
            String resp = GoRestClient.GET(id);
            if (!resp.equals("Hiba!")) {
                JsonObject jsonObject = new JsonParser().parse(resp).getAsJsonObject().get("data").getAsJsonObject();
                User user = new User();
                user.setName(StripJsonFromQuotes(String.valueOf(jsonObject.get("name"))));
                user.setEmail(StripJsonFromQuotes(String.valueOf(jsonObject.get("email"))));
                user.setGender(StripJsonFromQuotes(String.valueOf(jsonObject.get("gender"))));
                user.setStatus(StripJsonFromQuotes(String.valueOf(jsonObject.get("status"))));

                String newName = tfUserName.getText();
                if (newName.equals("")) newName.equals(user.getName());
                String newEmail = tfUserEmail.getText();
                if (newEmail.equals("")) newEmail.equals(user.getEmail());
                String newGender;
                RadioButton selectedGender = (RadioButton) groupGenders.getSelectedToggle();
                if (selectedGender.getText().equals("male")) {
                    newGender = "male";
                } else {
                    newGender = "female";
                }
                String newStatus;
                RadioButton selectedStatus = (RadioButton) groupStatuses.getSelectedToggle();
                if (selectedStatus.getText().equals("active")) {
                    newStatus = "active";
                } else {
                    newStatus = "inactive";
                }
                String result = GoRestClient.PUT(id, newName, newGender, newEmail, newStatus);
                if (result.equals("Hiba!")) {
                    lbTitle.setText(resp);
                    lbTitle.setVisible(true);
                    lbTitle.setManaged(true);
                } else {
                    JsonObject jsonObject2 = new JsonParser().parse(result).getAsJsonObject().get("data").getAsJsonObject();

                    Alert successMsg = new Alert(Alert.AlertType.CONFIRMATION);
                    successMsg.setTitle("Sikeres módosítás");
                    successMsg.setHeaderText("Felhasználó sikeresen módosítva");
                    successMsg.setContentText("Név: %s\nEmail: %s\nNem: %s\nStátusz: %s"
                            .formatted(jsonObject2.get("name"), jsonObject.get("email"),
                                    jsonObject2.get("gender"), jsonObject2.get("status")
                            ));
                    successMsg.showAndWait();
                    menuReadGoRestClick();
                }
            }
        }
    }

    public void menuDeleteGoRestClick() throws IOException {
        DeleteElements();
        gpUser.setVisible(true);
        gpUser.setManaged(true);
        fillUserComboBox();
        btAddUser.setVisible(false);
        btAddUser.setManaged(false);
        btUpdateUser.setVisible(false);
        btUpdateUser.setManaged(false);
        btDeleteUser.setVisible(true);
        btDeleteUser.setManaged(true);
        tfUserName.setDisable(true);
        tfUserEmail.setDisable(true);
        rbGenderMale.setDisable(true);
        rbGenderFemale.setDisable(true);
        rbStatusActive.setDisable(true);
        rbStatusInactive.setDisable(true);
    }

    public void btDeleteUserClick() throws IOException {
        String id = (String) cbSelectUser.getValue();
        if (id != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Törlés megerősítése");
            alert.setHeaderText("A %s ID-val rendelkező felhasználó véglegesen törlésre kerül.".formatted(id));
            alert.setContentText("Biztosan folytatja?");
            Optional<ButtonType> result = alert.showAndWait();
            String msg;
            if (result.get() == ButtonType.OK) {
                if (GoRestClient.DELETE(id).equals("Hiba!")) {
                    lbTitle.setText("Hiba a törlés közben.");
                    lbTitle.setVisible(true);
                    lbTitle.setManaged(true);
                } else {
                    Alert successMsg = new Alert(Alert.AlertType.CONFIRMATION);
                    successMsg.setTitle("Sikeres törlés");
                    successMsg.setHeaderText(null);
                    successMsg.setContentText("Felhasználó sikeresen törölve");
                    successMsg.showAndWait();
                    menuReadGoRestClick();
                }
            } else {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Megszakítás");
                info.setHeaderText(null);
                info.setContentText("Törlés megszakítva");
                info.showAndWait();
            }
        }
    }


    @FXML
    public void l1_write() {
        lbFirst.setText("Blink no. " + c1);
    }

    @FXML
    public void l2_write() {
        lbSecond.setText("Blink no. " + c2);
    }

    @FXML
    public void menuParallelClick() throws IOException {
        DeleteElements();
        gpParallel.setVisible(true);
        gpParallel.setManaged(true);
        lbFirst.setVisible(true);
        lbSecond.setVisible(true);
        lbFirst.setText("Press the button!");
        lbSecond.setText("Press the button!");
        btStartblink.setVisible(true);
        btStartblink.setManaged(true);
        btStopblink.setVisible(true);
        btStopblink.setManaged(true);

    }

    private class Runner extends Thread {
        @FXML
        @Override
        public void run() {

            while (tFlag) {
                c1++;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        l1_write();
                    }
                });
                try {

                    Thread.sleep(1000);  // Wait two seconds between redraws.
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private class Runner2 extends Thread {
        @FXML
        @Override
        public void run() {

            while (tFlag) {
                c2++;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        l2_write();
                    }
                });
                try {

                    Thread.sleep(2000);  // Wait two seconds between redraws.
                } catch (InterruptedException e) {
                }
            }
        }
    }

    Runner runner = new Runner();
    Runner2 runner2 = new Runner2();

    @FXML
    void btStartblinkClick() {
        tFlag = true;
        // Set the signal before starting the thread!
        runner.start();
        runner2.start();

    }

    @FXML
    void btStopblinkClick() {

        tFlag = false;

    }


    @FXML
    public void menuStreamClick() throws IOException {
        DeleteElements();
        cbMnevek.getItems().removeAll();
        cbVnevek.getItems().removeAll();
        mlist.removeAll(mlist);
        gpStream.setVisible(true);
        gpStream.setManaged(true);
        cbMnevek.setVisible(true);
        cbMnevek.setManaged(true);
        btStartstr.setVisible(true);
        btStartstr.setManaged(true);


        List<String> mnevek = new ArrayList<>();

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/varosok","root",""); Statement stmt = con.createStatement();) {

            String SQL = "SELECT * FROM varos INNER JOIN lelekszam ON varos.id = lelekszam.varosid INNER JOIN megye ON varos.megyeid = megye.id";
            ResultSet rs = stmt.executeQuery(SQL);
            while (rs.next()) {
                Mindenegyben mindenegyben = new Mindenegyben();
                mindenegyben.setVarosid(Integer.parseInt((rs.getString("varos.id"))));
                mindenegyben.setEv((rs.getString("lelekszam.ev")));
                mindenegyben.setNo(Integer.parseInt(rs.getString("lelekszam.no")));
                mindenegyben.setOsszesen(Integer.parseInt(rs.getString("lelekszam.osszesen")));
                mindenegyben.setVarosnev(rs.getString("varos.nev"));
                mindenegyben.setMegyeid(Integer.parseInt((rs.getString("megye.id"))));
                mindenegyben.setMegyeijogu(Integer.parseInt(rs.getString("varos.megyeijogu")));
                mindenegyben.setMegyeszekhely(Integer.parseInt(rs.getString("varos.megyeszekhely")));
                mindenegyben.setMegyenev(rs.getString("megye.nev"));
                mlist.add(mindenegyben);
            }
            for (int i = 0; i < mlist.size(); i++) {
                if (!mnevek.contains(mlist.get(i).getMegyenev())){
                mnevek.add(mlist.get(i).getMegyenev());
                    System.out.println(mlist.get(i).getMegyenev());
                }
                cbMnevek.setItems(FXCollections.observableList(mnevek));
            }



        }
        // Handle any errors that may have occurred.
         catch (SQLException e) {
            throw new RuntimeException(e);
        }


}
    @FXML
    public void cbMnevekChanged() throws IOException {
        cbVnevek.setVisible(true);
        cbVnevek.setManaged(true);
        String m = cbMnevek.getValue().toString();
        List<String> varosnevek = new ArrayList<>();
        for (int i = 0; i < mlist.size(); i++) {
            String ml = mlist.get(i).getMegyenev();
            if (!varosnevek.contains(mlist.get(i).getVarosnev())){
                if (m.contains(ml)){varosnevek.add(mlist.get(i).getVarosnev());}

            }
            cbVnevek.setItems(FXCollections.observableList(varosnevek));
        }

    }
    @FXML
    public void cbVnevekChanged() throws IOException {


    }
    @FXML
    public void btStartstrClick() {
        DeleteElements();
        tvMinden.setVisible(true);
        tvMinden.setManaged(true);
        cbMnevek.getItems().removeAll();
        cbVnevek.getItems().removeAll();
        tvMinden.getColumns().removeAll(tvMinden.getColumns());
        mNev = new TableColumn<>("Megyenév");
        vNev = new TableColumn<>("Városnév");
        mSzekh = new TableColumn<>("Megyeszékhely");
        mJog = new TableColumn<>("Megyei jogú város");
        datum = new TableColumn<>("Dátum");
        lakosok = new TableColumn<>("Lakosok száma");
        nLakosok = new TableColumn<>("Női lakosok száma");
        fLakosok = new TableColumn<>("Férfi lakosok száma");
        tvMinden.getColumns().addAll(mNev, vNev, mSzekh, mJog, datum, lakosok, nLakosok, fLakosok);
        mNev.setCellValueFactory(new PropertyValueFactory<>("mnv"));
        vNev.setCellValueFactory(new PropertyValueFactory<>("vnev"));
        mSzekh.setCellValueFactory(new PropertyValueFactory<>("mszekh"));
        mJog.setCellValueFactory(new PropertyValueFactory<>("mjog"));
        datum.setCellValueFactory(new PropertyValueFactory<>("datum"));
        lakosok.setCellValueFactory(new PropertyValueFactory<>("lakosok"));
        nLakosok.setCellValueFactory(new PropertyValueFactory<>("nlakosok"));
        fLakosok.setCellValueFactory(new PropertyValueFactory<>("flakosok"));
        tvMinden.getItems().clear();


        mlist.stream()
                .forEach(i -> {
                    if (i.getVarosnev().contentEquals(cbVnevek.getValue().toString())) {
                        String szkhely = "igen";
                        String jog = "igen";
                        MindenView mindenView = new MindenView();
                        if (i.getMegyeszekhely() == 0)
                        {
                            szkhely = "nem";
                        }if (i.getMegyeijogu() == 0)
                        {
                            jog = "nem";
                        }

                        mindenView.setMnev(i.getMegyenev());
                        mindenView.setVnev(i.getVarosnev());
                        mindenView.setMszekh(szkhely);
                        mindenView.setMjog(jog);
                        mindenView.setDatum(i.getEv().toString());
                        mindenView.setLakosok(String.valueOf(i.getOsszesen()));
                        mindenView.setNlakosok(String.valueOf(i.getNo()));
                        mindenView.setFlakosok(String.valueOf(i.getOsszesen()-i.getNo()));
                        tvMinden.getItems().add(mindenView);
                        System.out.println(mindenView);
                    }
                });
    }

    public void menudontfaClick() throws Exception {
        String fájlNév = "data/diabetes.arff";
        int classIndex=8;	// 20. oszlopot kell előre jelezni
        new MachineLearning(fájlNév, classIndex);
    }

    @FXML
    public void lML1_write(String a) {
        lbML1.setText(a);
    }
    @FXML
    public void lML2_write(String a) {
        lbML2.setText(a);
    }
    @FXML
    public void menutobbalgClick() throws Exception {
        DeleteElements();
        gpML1.setVisible(true);
        gpML1.setManaged(true);
        lML1_write("Loading...");
        String fájlNév = "data/diabetes.arff";
        int classIndex=8;	// 20. oszlopot kell előre jelezni
        String j48_cci =  new MachineLearningCrossValidation(fájlNév, classIndex, new J48()).getCci_pct();
        String SMO_cci =  new MachineLearningCrossValidation(fájlNév, classIndex, new SMO()).getCci_pct();
        String naive_cci =  new MachineLearningCrossValidation(fájlNév, classIndex, new NaiveBayes()).getCci_pct();
        IBk classifier = new IBk();
        // 10 legközelebbi szomszéd:
        classifier.setOptions(Utils.splitOptions("-K 10"));
        String k10_cci =  new MachineLearningCrossValidation(fájlNév, classIndex, classifier).getCci_pct();
        String rf_cci =  new MachineLearningCrossValidation(fájlNév, classIndex, new RandomForest()).getCci_pct();
        float cv[];
        cv = new float[5];
        cv[0] = Float.parseFloat(j48_cci);
        cv[1] = Float.parseFloat(SMO_cci);
        cv[2] = Float.parseFloat(naive_cci);
        cv[3] = Float.parseFloat(k10_cci);
        cv[4] = Float.parseFloat(rf_cci);
        System.out.println(j48_cci + "  " + SMO_cci + "  " + naive_cci + "  " + k10_cci + "  " + rf_cci);
        String best = "";
        float bestn = 0;
        int nb = 0;
        for (int i = 0; i < 5; i++)
        {
            if(cv[i] > bestn)
            {
                nb = i;
                bestn = cv[i];
            }
        }

        switch (nb) {
            case 0:
                best = "J 48";
                break;
            case 1:
                best = "SMO";;
                break;
            case 2:
                best = "NaiveBayes";;
                break;
            case 3:
                best = "-K 10";
                break;
            case 4:
                best = "Random Forest";
                break;

        }
        lML1_write("Best algorithm is: " + best);
        lML2_write("With CCI percent: " + bestn);
        System.out.println(best + "  "+ bestn);


    }
    @FXML
    public void menutobbalg2Click() throws Exception {
        DeleteElements();
        gpML2.setVisible(true);
        gpML2.setManaged(true);
        List<String> s = new ArrayList<>();
        s.add("J 48");
        s.add("SMO");
        s.add("NaiveBayes");
        s.add("-K 10");
        s.add("Random Forest");
        cbAlg.setItems(FXCollections.observableList(s));

    }
    @FXML //TP, TN, FP, FN, Correctly Classified Instances, Incorrectly Classified Instances
    public void btStartalgClick() throws Exception {
        taMlalg.setVisible(true);
        taMlalg.setManaged(true);
        String fájlNév = "data/diabetes.arff";
        int classIndex=8;
        String chosen = cbAlg.getValue().toString();
        String adat = "";
        switch (chosen) {
            case "J 48":
                adat =  new MLCV2(fájlNév, classIndex, new J48()).getAdat();
                taMlalg.setText(adat);
                break;
            case "SMO":
                adat =  new MLCV2(fájlNév, classIndex, new SMO()).getAdat();
                taMlalg.setText(adat);
                break;
            case "NaiveBayes":
                adat =  new MLCV2(fájlNév, classIndex, new NaiveBayes()).getAdat();
                taMlalg.setText(adat);
                break;
            case "-K 10":
                IBk classifier = new IBk();
                classifier.setOptions(Utils.splitOptions("-K 10"));
                adat =  new MLCV2(fájlNév, classIndex,  classifier).getAdat();
                taMlalg.setText(adat);
                break;
            case "Random Forest":
                adat =  new MLCV2(fájlNév, classIndex, new RandomForest()).getAdat();
                taMlalg.setText(adat);
                break;

        }
        taMlalg.setText(adat);


    }

}