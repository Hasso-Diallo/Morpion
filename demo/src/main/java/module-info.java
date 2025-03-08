module fulbert.hassanatou.morpillon {
    requires javafx.controls;
    requires javafx.fxml;

    opens fulbert.hassanatou.morpillon to javafx.fxml;
    exports fulbert.hassanatou.morpillon;
}
