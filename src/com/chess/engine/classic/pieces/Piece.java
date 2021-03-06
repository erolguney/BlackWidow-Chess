package com.chess.engine.classic.pieces;

import java.util.List;

import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Move;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final Alliance pieceAlliance;
    protected final int piecePosition;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    Piece(final PieceType type,
          final Alliance alliance,
          final int piecePosition,
          final boolean isFirstMove) {
        this.pieceType = type;
        this.piecePosition = piecePosition;
        this.pieceAlliance = alliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public Alliance getPieceAllegiance() {
        return this.pieceAlliance;
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public abstract int getPieceValue();

    public abstract int locationBonus();

    public abstract Piece movePiece(Move move);

    public boolean isKing() {
        return this.pieceType == PieceType.KING;
    }

    public boolean isPawn() {
        return this.pieceType == PieceType.PAWN;
    }

    public abstract List<Move> calculateLegalMoves(final Board board);

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }

        final Piece otherPiece = (Piece) other;

        return piecePosition == otherPiece.piecePosition && pieceType == otherPiece.pieceType &&
               pieceAlliance == otherPiece.pieceAlliance && isFirstMove == otherPiece.isFirstMove;

    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    private int computeHashCode() {
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    public enum PieceType {

        PAWN(100, "P"),
        KNIGHT(300, "N"),
        BISHOP(300, "B"),
        ROOK(500, "R"),
        QUEEN(900, "Q"),
        KING(10000, "K");

        private final int value;
        private final String pieceName;

        public int getPieceValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }

        PieceType(final int val, final String pieceName) {
            this.value = val;
            this.pieceName = pieceName;
        }

    }

}