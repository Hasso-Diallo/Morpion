package fulbert.hassanatou.morpillon;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

/**
 * Représente une case (cellule) du jeu TicTacToe.
 * Utilise seulement 3 écouteurs : clic, entrée et sortie de souris.
 */
public class TicTacToeSquare extends TextField {

    private static final TicTacToeModel model = TicTacToeModel.getInstance();
    private final int row;
    private final int column;

    /**
     * Constructeur de la case, avec sa position (row, column).
     */
    public TicTacToeSquare(final int row, final int column) {
        this.row = row;
        this.column = column;

        // Centrer le texte, ajuster la taille de la police
        setFont(Font.font(18));
        setEditable(false);
        setFocusTraversable(false);
        setStyle("-fx-alignment: center; -fx-background-color: white;");

        // 1) setOnMouseClicked : jouer si le coup est légal
        setOnMouseClicked(this::handleClick);

        // 2) setOnMouseEntered : survol -> fond vert si légal, sinon rouge
        setOnMouseEntered(this::handleMouseEntered);

        // 3) setOnMouseExited : quand la souris sort -> blanc si pas de game over, sinon rouge
        setOnMouseExited(this::handleMouseExited);

        // Liaison du texte affiché : "X" pour FIRST, "O" pour SECOND, sinon vide
        textProperty().bind(Bindings.createStringBinding(() -> {
            Owner occupant = model.getSquare(row, column).get();
            if (occupant == Owner.FIRST) return "X";
            if (occupant == Owner.SECOND) return "O";
            return "";
        }, model.getSquare(row, column)));

        // Liaison pour agrandir la police si la case fait partie d'une combinaison gagnante
        styleProperty().bind(Bindings.createStringBinding(() -> {
            boolean isWinning = model.getWinningSquare(row, column).get();
            // Couleur de fond : si la partie est finie et qu'on n'est pas survolé, la case sera rouge dans l'event "exit"
            // Sinon, on reste en blanc par défaut.
            // On agrandit la police si c'est une case gagnante.
            if (isWinning) {
                return "-fx-font-size: 28px; -fx-alignment: center;";
            } else {
                return "-fx-font-size: 18px; -fx-alignment: center;";
            }
        }, model.getWinningSquare(row, column)));
    }

    /**
     * Gère le clic : on tente de jouer dans cette case.
     */
    private void handleClick(MouseEvent e) {
        model.play(row, column);
    }

    /**
     * Gère l'entrée de la souris : fond vert si on peut jouer, rouge sinon.
     */
    private void handleMouseEntered(MouseEvent e) {
        if (model.legalMove(row, column).get()) {
            setStyle(getStyle() + "-fx-background-color: lightgreen;");
        } else {
            setStyle(getStyle() + "-fx-background-color: lightcoral;");
        }
    }

    /**
     * Gère la sortie de la souris : si le jeu est fini, tout est rouge ;
     * sinon, on remet la couleur de base.
     */
    private void handleMouseExited(MouseEvent e) {
        if (model.gameOver().get()) {
            setStyle(getStyle() + "-fx-background-color: lightcoral;");
        } else {
            setStyle(getStyle() + "-fx-background-color: white;");
        }
    }

    // ------------------------------------------------------------------------
    // (Les méthodes ownerProperty() et colorProperty() pourraient exister
    //  si vous devez explicitement les référencer, mais elles ne sont pas
    //  forcément nécessaires dans cette solution concrète.)
    // ------------------------------------------------------------------------
    public ObjectProperty<Owner> ownerProperty() {
        return model.getSquare(row, column);
    }

    public BooleanProperty colorProperty() {
        return model.getWinningSquare(row, column);
    }
}


