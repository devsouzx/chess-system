package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class ChessMatch {
  private Integer turn;
  private Color currentPlayer;
  private Board board;

  List<Piece> piecesOnTheBoard = new ArrayList<>();
  List<Piece> capturedPieces = new ArrayList<>();

  public ChessMatch() {
    board = new Board(8, 8);
    turn = 1;
    currentPlayer = Color.BLUE;
    initialSetup();
  }

  public Integer getTurn() {
    return turn;
  }

  public Color getCurrentPlayer() {
    return currentPlayer;
  }

  public ChessPiece[][] getPieces() {
    ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
    for (int i = 0; i < board.getRows(); i++) {
      for (int j = 0; j < board.getColumns(); j++) {
        mat[i][j] = (ChessPiece) board.piece(i, j);
      }
    }
    return mat;
  }

  public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
    Position source = sourcePosition.toPosition();
    Position target = targetPosition.toPosition();
    validateSourcePosition(source);
    validateTargetPosition(source, target);
    Piece capturedPiece = makeMove(source, target);
    nextTurn();
    return (ChessPiece) capturedPiece;
  }

  public boolean[][] possibleMoves(ChessPosition sourcePosition) {
    Position position = sourcePosition.toPosition();
    validateSourcePosition(position);
    return board.piece(position).possibleMoves();
  }

  private void validateSourcePosition(Position position) {
    if (!board.thereIsAPiece(position)) {
      throw new ChessException("There is no piece on source position!");
    }
    if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
      throw new ChessException("The chosen piece is not yours");
    }
    if (!board.piece(position).isThereAnyPossibleMove()) {
      throw new ChessException("There is no possible moves for the chosen piece");
    }
  }

  private void validateTargetPosition(Position source, Position target) {
    if (!board.piece(source).possibleMove(target)) {
      throw new ChessException("The chosen piece cannot move to target position");
    }
  }

  private void nextTurn() {
    turn++;
    currentPlayer = (currentPlayer == Color.BLUE) ? Color.PURPLE : Color.BLUE;
  }

  private Piece makeMove(Position source, Position target) {
    Piece p = board.removePiece(source);
    Piece capturedPiece = board.removePiece(target);
    board.placePiece(p, target);
    if (capturedPiece != null) {
      piecesOnTheBoard.remove(capturedPiece);
      capturedPieces.add(capturedPiece);
    }
    return capturedPiece;
  }

  private void placeNewPiece(char column, int row, ChessPiece piece) {
    board.placePiece(piece, new ChessPosition(column, row).toPosition());
    piecesOnTheBoard.add(piece);
  }

  private void initialSetup() {
    placeNewPiece('c', 1, new Rook(board, Color.BLUE));
    placeNewPiece('c', 2, new Rook(board, Color.BLUE));
    placeNewPiece('d', 2, new Rook(board, Color.BLUE));
    placeNewPiece('e', 2, new Rook(board, Color.BLUE));
    placeNewPiece('e', 1, new Rook(board, Color.BLUE));
    placeNewPiece('d', 1, new King(board, Color.BLUE));

    placeNewPiece('c', 7, new Rook(board, Color.PURPLE));
    placeNewPiece('c', 8, new Rook(board, Color.PURPLE));
    placeNewPiece('d', 7, new Rook(board, Color.PURPLE));
    placeNewPiece('e', 7, new Rook(board, Color.PURPLE));
    placeNewPiece('e', 8, new Rook(board, Color.PURPLE));
    placeNewPiece('d', 8, new King(board, Color.PURPLE));
  }
}
