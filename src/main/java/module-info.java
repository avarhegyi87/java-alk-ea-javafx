module com.example.cities {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires java.naming;
    requires java.sql;
    requires org.hibernate.orm.core;
    requires com.google.gson;

    opens com.example.cities to javafx.fxml;
    exports com.example.cities;
}