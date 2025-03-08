package fulbert.hassanatou.morpillon;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

/**
 * Modèle du jeu TicTacToe : gère l'état du plateau, le tour courant,
 * la détection de victoire, et expose des propriétés pour le binding.
 */
public class TicTacToeModel {
    /**
     * Taille du plateau (pour être extensible).
     */
    private static final int BOARD_WIDTH = 3;
    private static final int BOARD_HEIGHT = 3;

    /**
     * Nombre de pièces alignées pour gagner (idem).
     */
    private static final int WINNING_COUNT = 3;

    /**
     * Joueur courant (commence par FIRST).
     */
    private final ObjectProperty<Owner> turn = new SimpleObjectProperty<>(Owner.FIRST);

    /**
     * Vainqueur du jeu, NONE si pas encore déterminé.
     */
    private final ObjectProperty<Owner> winner = new SimpleObjectProperty<>(Owner.NONE);

    /**
     * Plateau de jeu : chaque case est une propriété d'Owner (NONE, FIRST, SECOND).
     */
    private final ObjectProperty<Owner>[][] board;

    /**
     * Tableau indiquant si une case fait partie d'une combinaison gagnante.
     */
    private final BooleanProperty[][] winningBoard;

    /**
     * Constructeur privé (Singleton).
     */
    private TicTacToeModel() {
        board = new ObjectProperty[BOARD_HEIGHT][BOARD_WIDTH];
        winningBoard = new BooleanProperty[BOARD_HEIGHT][BOARD_WIDTH];
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col] = new SimpleObjectProperty<>(Owner.NONE);
                winningBoard[row][col] = new SimpleBooleanProperty(false);
            }
        }
    }

    /**
     * Holder pour le Singleton.
     */
    private static class TicTacToeModelHolder {
        private static final TicTacToeModel INSTANCE = new TicTacToeModel();
    }

    /**
     * @return la seule instance possible du jeu (Singleton).
     */
    public static TicTacToeModel getInstance() {
        return TicTacToeModelHolder.INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Propriétés et accès aux cases
    // -----------------------------------------------------------------------

    /**
     * @return la propriété du joueur courant.
     */
    public final ObjectProperty<Owner> turnProperty() {
        return turn;
    }

    /**
     * @return la propriété indiquant le vainqueur du jeu.
     */
    public final ObjectProperty<Owner> winnerProperty() {
        return winner;
    }

    /**
     * @return la propriété occupant la case (row, column).
     */
    public final ObjectProperty<Owner> getSquare(int row, int column) {
        return board[row][column];
    }

    /**
     * @return la propriété indiquant si la case (row, column) fait partie d'une combinaison gagnante.
     */
    public final BooleanProperty getWinningSquare(int row, int column) {
        return winningBoard[row][column];
    }

    /**
     * Remet le jeu à zéro : vide le plateau, enlève tout vainqueur, remet le tour à FIRST.
     */
    public void restart() {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                board[row][col].set(Owner.NONE);
                winningBoard[row][col].set(false);
            }
        }
        winner.set(Owner.NONE);
        turn.set(Owner.FIRST);
    }

    /**
     * @return true si la case (row, column) est valide et vide.
     */
    public boolean validSquare(int row, int column) {
        // On peut jouer si la case est NONE.
        return board[row][column].get() == Owner.NONE;
    }

    /**
     * Passe au joueur suivant (FIRST -> SECOND, SECOND -> FIRST).
     */
    public void nextPlayer() {
        turn.set(turn.get().opposite());
    }

    /**
     * Définit le vainqueur.
     */
    public void setWinner(Owner w) {
        winner.set(w);
    }

    // -----------------------------------------------------------------------
    // Logique de jeu
    // -----------------------------------------------------------------------

    /**
     * @return un binding qui indique si on peut jouer dans (row, col) :
     *         la case doit être libre et le jeu pas terminé.
     */
    public BooleanBinding legalMove(int row, int column) {
        return new BooleanBinding() {
            {
                super.bind(board[row][column], winner, gameOver());
            }
            @Override
            protected boolean computeValue() {
                return board[row][column].get() == Owner.NONE && !gameOver().get();
            }
        };
    }

    /**
     * Joue dans la case (row, column) quand c’est possible et vérifie si cela provoque une victoire.
     */
    public void play(int row, int column) {
        if (legalMove(row, column).get()) {
            board[row][column].set(turn.get());
            checkWinner(row, column);
            if (!gameOver().get()) {
                nextPlayer();
            }
        }
    }

    /**
     * Vérifie si le dernier coup en (row, col) donne un gagnant. Met à jour winningBoard si besoin.
     */
    private void checkWinner(int row, int col) {
        Owner current = board[row][col].get();
        if (current == Owner.NONE) return;

        // Remet à faux toutes les cases gagnantes avant un nouveau test
        for (int r = 0; r < BOARD_HEIGHT; r++) {
            for (int c = 0; c < BOARD_WIDTH; c++) {
                winningBoard[r][c].set(false);
            }
        }

        // Vérifie la ligne
        if (checkLine(row, current)) {
            highlightLine(row);
            winner.set(current);
            return;
        }
        // Vérifie la colonne
        if (checkColumn(col, current)) {
            highlightColumn(col);
            winner.set(current);
            return;
        }
        // Vérifie diagonale principale
        if (row == col && checkMainDiagonal(current)) {
            highlightMainDiagonal();
            winner.set(current);
            return;
        }
        // Vérifie diagonale secondaire
        if (row + col == BOARD_WIDTH - 1 && checkAntiDiagonal(current)) {
            highlightAntiDiagonal();
            winner.set(current);
        }
    }

    /**
     * Vérifie si la ligne `row` est entièrement occupée par `owner`.
     */
    private boolean checkLine(int row, Owner owner) {
        for (int col = 0; col < BOARD_WIDTH; col++) {
            if (board[row][col].get() != owner) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si la colonne `col` est entièrement occupée par `owner`.
     */
    private boolean checkColumn(int col, Owner owner) {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            if (board[row][col].get() != owner) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si la diagonale principale est entièrement occupée par `owner`.
     */
    private boolean checkMainDiagonal(Owner owner) {
        for (int i = 0; i < WINNING_COUNT; i++) {
            if (board[i][i].get() != owner) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si la diagonale secondaire est entièrement occupée par `owner`.
     */
    private boolean checkAntiDiagonal(Owner owner) {
        for (int i = 0; i < WINNING_COUNT; i++) {
            if (board[i][BOARD_WIDTH - 1 - i].get() != owner) {
                return false;
            }
        }
        return true;
    }

    /**
     * Met en évidence la ligne `row` comme gagnante.
     */
    private void highlightLine(int row) {
        for (int col = 0; col < BOARD_WIDTH; col++) {
            winningBoard[row][col].set(true);
        }
    }

    /**
     * Met en évidence la colonne `col` comme gagnante.
     */
    private void highlightColumn(int col) {
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            winningBoard[row][col].set(true);
        }
    }

    /**
     * Met en évidence la diagonale principale comme gagnante.
     */
    private void highlightMainDiagonal() {
        for (int i = 0; i < WINNING_COUNT; i++) {
            winningBoard[i][i].set(true);
        }
    }

    /**
     * Met en évidence la diagonale secondaire comme gagnante.
     */
    private void highlightAntiDiagonal() {
        for (int i = 0; i < WINNING_COUNT; i++) {
            winningBoard[i][BOARD_WIDTH - 1 - i].set(true);
        }
    }

    // -----------------------------------------------------------------------
    // Bindings pour l'affichage
    // -----------------------------------------------------------------------

    /**
     * @return un binding qui renvoie le nombre de cases occupées par le joueur `owner`.
     */
    public NumberExpression getScore(Owner owner) {
        // On recalcule le score dès qu'une case change
        return Bindings.createIntegerBinding(() -> {
            int count = 0;
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    if (board[row][col].get() == owner) {
                        count++;
                    }
                }
            }
            return count;
        }, allBoardProperties());
    }

    /**
     * @return un binding qui renvoie le nombre de cases libres (NONE).
     */
    public NumberExpression getFreeSquares() {
        return Bindings.createIntegerBinding(() -> {
            int count = 0;
            for (int row = 0; row < BOARD_HEIGHT; row++) {
                for (int col = 0; col < BOARD_WIDTH; col++) {
                    if (board[row][col].get() == Owner.NONE) {
                        count++;
                    }
                }
            }
            return count;
        }, allBoardProperties());
    }

    /**
     * @return un tableau d'ObservableValue pour toutes les cases du plateau,
     *         afin que Bindings.createXxxBinding(...) réagisse à tout changement.
     */
    private ObservableValue<?>[] allBoardProperties() {
        // Collecte toutes les propriétés occupant le board pour recalculer le binding
        ObservableValue<?>[] props = new ObservableValue[BOARD_HEIGHT * BOARD_WIDTH];
        int index = 0;
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                props[index++] = board[row][col];
            }
        }
        return props;
    }

    /**
     * @return un binding booléen qui indique si le jeu est terminé :
     *         soit un vainqueur est déterminé, soit il n'y a plus de cases libres.
     */
    public BooleanBinding gameOver() {
        return new BooleanBinding() {
            {
                super.bind(winner, getFreeSquares());
            }
            @Override
            protected boolean computeValue() {
                return winner.get() != Owner.NONE || getFreeSquares().intValue() == 0;
            }
        };
    }

    /**
     * @return une expression de texte indiquant le résultat final (vainqueur ou match nul),
     *         uniquement valide quand le jeu est terminé.
     */
    public final StringExpression getEndOfGameMessage() {
        return Bindings.createStringBinding(() -> {
            // Si le jeu n'est pas terminé, on n'affiche rien
            if (!gameOver().get()) {
                return "";
            }
            // Si un vainqueur est défini
            if (winner.get() != Owner.NONE) {
                return (winner.get() == Owner.FIRST ? "Le gagnant est X !" : "Le gagnant est O !");
            }
            // Sinon, c'est un match nul
            return "Match nul !";
        }, winner, gameOver());
    }
}
