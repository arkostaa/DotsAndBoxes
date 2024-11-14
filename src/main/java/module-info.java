module org.example.sticksgame {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.logging;

    opens org.example.sticksgame to javafx.fxml;
    exports org.example.sticksgame;
}