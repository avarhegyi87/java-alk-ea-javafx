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
    @FXML private GridPane gpAddCity, gpDeleteCity, gpUser;
    @FXML private TextField tfCityName, tfPopulation, tfWomen, tfUserName, tfUserEmail;
    @FXML private ComboBox cbCounty, cbDelCity, cbSelectUser;
    @FXML private ToggleGroup groupCountyCapital, groupCountyRights, groupStatuses, groupGenders;
    @FXML private RadioButton rbCountyCapitalYes, rbCountyCapitalNo;
    @FXML private RadioButton rbCountyRightsYes, rbCountyRightsNo;
    @FXML public RadioButton rbGenderMale, rbGenderFemale, rbStatusActive, rbStatusInactive;
    @FXML public Button btUpdateUser, btDeleteUser, btAddUser;
    @FXML private TableView tvCities, tvPersons;
    @FXML private TableColumn<City, String> IdCol;
    @FXML private TableColumn<City, String> NameCol;
    @FXML private TableColumn<County, String> CountyCol;
    @FXML private TableColumn<City, String> CountyCapitalCol;
    @FXML private TableColumn<City, String> CountyRightsCol;
    @FXML private TableColumn<City, String> MostRecentPopulationCol;
    @FXML private TableColumn<User, String> UserIdCol, UserNameCol, UserEmailCol, UserGenderCol, UserStatusCol;

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
        lbTitle.setVisible(false); lbTitle.setManaged(false);
        gpAddCity.setVisible(false); gpAddCity.setManaged(false);
        gpDeleteCity.setVisible(false); gpDeleteCity.setManaged(false);
        gpUser.setVisible(false); gpUser.setManaged(false);
        tfCityName.setText(""); tfPopulation.setText(""); tfWomen.setText("");
        tvCities.setVisible(false); tvCities.setManaged(false);
        tvPersons.setVisible(false); tvPersons.setManaged(false);
        tfUserName.setDisable(false); tfUserEmail.setDisable(false);
        tfUserName.setText(""); tfUserEmail.setText("");
        rbGenderMale.setDisable(false); rbGenderFemale.setDisable(false);
        rbStatusActive.setDisable(false); rbStatusInactive.setDisable(false);
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
        gpUser.setVisible(true); gpUser.setManaged(true);
        cbSelectUser.setVisible(false); cbSelectUser.setManaged(false);
        btAddUser.setVisible(true); btAddUser.setManaged(true);
        btUpdateUser.setVisible(false); btUpdateUser.setManaged(false);
        btDeleteUser.setVisible(false); btDeleteUser.setManaged(false);
    }
    public void btAddUserClick(ActionEvent actionEvent) throws IOException {
        String gender = ((RadioButton) groupGenders.getSelectedToggle()).getText();
        String status = ((RadioButton) groupStatuses.getSelectedToggle()).getText();
        String resp = GoRestClient.POST(tfUserName.getText(), gender, tfUserEmail.getText(), status);
        if (resp.equals("Hiba!")) {
            lbTitle.setText(resp); lbTitle.setVisible(true); lbTitle.setManaged(true);
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
        cbSelectUser.setVisible(true); cbSelectUser.setManaged(true);
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
                lbTitle.setVisible(true); lbTitle.setManaged(true);
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
        gpUser.setVisible(true); gpUser.setManaged(true);
        fillUserComboBox();
        btAddUser.setVisible(false); btAddUser.setManaged(false);
        btUpdateUser.setVisible(true); btUpdateUser.setManaged(true);
        btDeleteUser.setVisible(false); btDeleteUser.setManaged(false);
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
                    lbTitle.setText(resp); lbTitle.setVisible(true); lbTitle.setManaged(true);
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
        gpUser.setVisible(true); gpUser.setManaged(true);
        fillUserComboBox();
        btAddUser.setVisible(false); btAddUser.setManaged(false);
        btUpdateUser.setVisible(false); btUpdateUser.setManaged(false);
        btDeleteUser.setVisible(true); btDeleteUser.setManaged(true);
        tfUserName.setDisable(true); tfUserEmail.setDisable(true);
        rbGenderMale.setDisable(true); rbGenderFemale.setDisable(true);
        rbStatusActive.setDisable(true); rbStatusInactive.setDisable(true);
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
                    lbTitle.setText("Hiba a törlés közben."); lbTitle.setVisible(true); lbTitle.setManaged(true);
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
}