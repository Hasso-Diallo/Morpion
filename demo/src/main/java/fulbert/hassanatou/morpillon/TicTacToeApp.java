package fulbert.hassanatou.morpillon;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Classe principale (point d'entrée) de l'application JavaFX TicTacToe.
 */
public class TicTacToeApp extends Application {
    private final TicTacToeModel model = TicTacToeModel.getInstance();

    @Override
    public void start(Stage primaryStage) {
        // --------------------------------------------------------------------
        // 1) Grille de jeu
        // --------------------------------------------------------------------
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER);

        // Remplir la grille avec des TicTacToeSquare
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                TicTacToeSquare square = new TicTacToeSquare(row, col);
                grid.add(square, col, row);
            }
        }

        // --------------------------------------------------------------------
        // 2) Zone d'affichage des scores et du nombre de cases libres
        // --------------------------------------------------------------------
        Label labelX = new Label();
        Label labelO = new Label();
        Label labelFree = new Label(); // cases libres

        // Liaison des textes
        labelX.textProperty().bind(Bindings.concat("Cases pour X : ", model.getScore(Owner.FIRST).asString()));
        labelO.textProperty().bind(Bindings.concat("Cases pour O : ", model.getScore(Owner.SECOND).asString()));

        // On affiche le nombre de cases libres SEULEMENT si le jeu n'est pas terminé
        labelFree.textProperty().bind(Bindings.concat("Cases libres : ", model.getFreeSquares().asString()));
        labelFree.visibleProperty().bind(model.gameOver().not());

        // Mise en forme : Joueur courant en cyan, l'autre en rouge
        model.turnProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == Owner.FIRST) {
                labelX.setStyle("-fx-background-color: cyan; -fx-padding: 5px;");
                labelO.setStyle("-fx-background-color: lightcoral; -fx-padding: 5px;");
            } else {
                labelX.setStyle("-fx-background-color: lightcoral; -fx-padding: 5px;");
                labelO.setStyle("-fx-background-color: cyan; -fx-padding: 5px;");
            }
        });
        // Initialisation du style
        if (model.turnProperty().get() == Owner.FIRST) {
            labelX.setStyle("-fx-background-color: cyan; -fx-padding: 5px;");
            labelO.setStyle("-fx-background-color: lightcoral; -fx-padding: 5px;");
        } else {
            labelX.setStyle("-fx-background-color: lightcoral; -fx-padding: 5px;");
            labelO.setStyle("-fx-background-color: cyan; -fx-padding: 5px;");
        }

        // Regroupe les labels dans un HBox
        HBox scoresBox = new HBox(20, labelX, labelO, labelFree);
        scoresBox.setAlignment(Pos.CENTER);

        // --------------------------------------------------------------------
        // 3) Label de fin de jeu (affiche le vainqueur ou "Match nul")
        // --------------------------------------------------------------------
        Label endGameLabel = new Label();
        endGameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: green;");
        endGameLabel.textProperty().bind(model.getEndOfGameMessage());
        // Visible seulement si le jeu est terminé
        endGameLabel.visibleProperty().bind(model.gameOver());

        // --------------------------------------------------------------------
        // 4) Bouton "Restart"
        // --------------------------------------------------------------------
        Button restartBtn = new Button("Restart");
        restartBtn.setOnAction(e -> {
            model.restart();
        });

        // --------------------------------------------------------------------
        // 5) Mise en page globale
        // --------------------------------------------------------------------
        VBox root = new VBox(15, scoresBox, grid, endGameLabel, restartBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Morpion");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

