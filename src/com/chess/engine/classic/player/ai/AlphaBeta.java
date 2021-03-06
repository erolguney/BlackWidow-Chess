package com.chess.engine.classic.player.ai;

import java.util.Comparator;
import java.util.List;

import com.chess.engine.classic.Alliance;
import com.chess.engine.classic.board.Board;
import com.chess.engine.classic.board.Board.MoveStatus;
import com.chess.engine.classic.board.Move;
import com.chess.engine.classic.board.MoveTransition;
import com.chess.engine.classic.player.Player;
import com.google.common.collect.Ordering;

public class AlphaBeta implements MoveStrategy {

    private final BoardEvaluator evaluator;
    private final MoveSorter moveSorter;
    private long boardsEvaluated;
    private long executionTime;
    private int quiescenceCount;
    private static final int MAX_QUIESCENCE = 2500;

    private enum MoveSorter {

        SORT {
            @Override
            List<Move> sort(final List<Move> moves) {
                return Ordering.from(ATTACKS_SORT).immutableSortedCopy(moves);
            }
        },
        EMPTY {
            @Override
            List<Move> sort(final List<Move> moves) {
                return moves;
            }
        };

        public static Comparator<Move> ATTACKS_SORT = new Comparator<Move>() {
            @Override
            public int compare(final Move move1, final Move move2) {
                return Boolean.compare(move2.isAttack(), move1.isAttack());
            }
        };

        abstract List<Move> sort(List<Move> moves);
    }

    public AlphaBeta() {
        this.evaluator = new SimpleBoardEvaluator();
        this.moveSorter = MoveSorter.SORT;
        this.boardsEvaluated = 0;
        this.quiescenceCount = 0;
    }

    @Override
    public String toString() {
        return "AlphaBeta";
    }

    @Override
    public long getNumBoardsEvaluated() {
        return this.boardsEvaluated;
    }

    @Override
    public Move execute(final Board board,
                        final int depth) {
        final long startTime = System.currentTimeMillis();
        final Player currentPlayer = board.currentPlayer();
        final Alliance alliance = currentPlayer.getAlliance();
        Move bestMove = Move.NULL_MOVE;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;
        System.out.println(board.currentPlayer() + " THINKING with depth = " + depth);
        for (final Move move : this.moveSorter.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.makeMove(move);
            this.quiescenceCount = 0;
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                final long candidateMoveStartTime = System.nanoTime();
                currentValue = alliance.isWhite() ?
                        min(moveTransition.getTransitionBoard(), depth - 1, highestSeenValue, lowestSeenValue) :
                        max(moveTransition.getTransitionBoard(), depth - 1, highestSeenValue, lowestSeenValue);
                final String quiescenceInfo = quiescenceCount > 0 ? " quiescenceCount = " +this.quiescenceCount : "";
                System.out.println("\t" + toString() + " analyzing move " + move + " (best move so far is:  " + bestMove
                                    + quiescenceInfo + " took " +calculateTimeTaken(candidateMoveStartTime, System.nanoTime()));
                if (alliance.isWhite() && currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                }
                else if (alliance.isBlack() && currentValue < lowestSeenValue) {
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }

        this.executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("%s SELECTS %s [#boards evaluated = %d, time taken = %d ms, rate = %.1f\n", board.currentPlayer(),
                bestMove, this.boardsEvaluated, this.executionTime, (1000 * ((double)this.boardsEvaluated/this.executionTime)));
        return bestMove;
    }

    public int max(final Board board,
                   final int depth,
                   final int highest,
                   final int lowest) {
        if (depth == 0 || isEndGame(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }
        int currentHighest = highest;
        for (final Move move : this.moveSorter.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                currentHighest = Math.max(currentHighest, min(moveTransition.getTransitionBoard(),
                                          calculateQuiescenceDepth(board, move, depth), currentHighest, lowest));
                if (currentHighest >= lowest) {
                    return lowest;
                }
            }
        }
        return currentHighest;
    }

    public int min(final Board board,
                   final int depth,
                   final int highest,
                   final int lowest) {
        if (depth == 0 || isEndGame(board)) {
            this.boardsEvaluated++;
            return this.evaluator.evaluate(board, depth);
        }
        int currentLowest = lowest;
        for (final Move move : this.moveSorter.sort((board.currentPlayer().getLegalMoves()))) {
            final MoveTransition moveTransition = board.makeMove(move);
            if (moveTransition.getMoveStatus() == MoveStatus.DONE) {
                currentLowest = Math.min(currentLowest, max(moveTransition.getTransitionBoard(),
                                         calculateQuiescenceDepth(board, move, depth), highest, currentLowest));
                if (currentLowest <= highest) {
                    return highest;
                }
            }
        }
        return currentLowest;
    }

    private int calculateQuiescenceDepth(final Board board,
                                         final Move move,
                                         final int depth) {
        if((move.isAttack() || board.currentPlayer().getOpponent().isInCheck()) &&
                depth == 1 && this.quiescenceCount < MAX_QUIESCENCE) {
            this.quiescenceCount++;
            return 2;
        }
        return depth - 1;
    }

    private static boolean isEndGame(final Board board) {
        return board.currentPlayer().isInCheckMate() || board.currentPlayer().getOpponent().isInCheckMate() ||
               board.currentPlayer().isInStaleMate() || board.currentPlayer().getOpponent().isInStaleMate();
    }

    private static String calculateTimeTaken(final long start, final long end) {
        final long timeTaken = (end - start) / 1000000;
        return timeTaken + " ms";
    }

}