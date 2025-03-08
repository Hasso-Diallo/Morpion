package fulbert.hassanatou.morpillon;

/**
 * Enumération représentant l'occupant d'une case.
 */
public enum Owner {
    NONE, FIRST, SECOND;

    /**
     * Renvoie le joueur opposé.
     * Si le joueur actuel est FIRST, renvoie SECOND, et inversement.
     * Si aucun joueur n'occupe la case (NONE), renvoie NONE.
     */
    public Owner opposite() {
        return this == SECOND ? FIRST : this == FIRST ? SECOND : NONE;
    }
}
