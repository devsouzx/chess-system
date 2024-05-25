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
  private boolean check;
  private boolean checkMate;
  private ChessPiece enPassantVulnerable;
  private ChessPiece promoted;

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

  public boolean getCheck() {
    return check;
  }

  public boolean getCheckMate() {
    return checkMate;
  }

  public ChessPiece getEnPassantVulnerable() {
    return enPassantVulnerable;
  }

  public ChessPiece getPromoted() {
    return promoted;
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

    if (testCheck(currentPlayer)) {
      undoMove(source, target, capturedPiece);
      throw new ChessException("You cant put yourself in check");
    }

    ChessPiece movedPiece = (ChessPiece) board.piece(target);

    // special move promoted
    promoted = null;
    if (movedPiece instanceof Pawn) {
      if ((movedPiece.getColor() == Color.BLUE && target.getRow() == 0) || (movedPiece.getColor() == Color.PURPLE && target.getRow() == 7)) {
        promoted = (ChessPiece)board.piece(target);
        promoted = replacePromotedPiece("Q");
      }
    }

    check = testCheck(opponent(currentPlayer));

    if (testCheckMate(opponent(currentPlayer))) {
      checkMate = true;
    } else {
      nextTurn();
    }

    // special move en passant
    if (movedPiece instanceof Pawn && (target.getRow() == source.getColumn() - 2 || target.getRow() == source.getColumn() + 2)) {
      enPassantVulnerable = movedPiece;
    } else {
      enPassantVulnerable = null;
    }

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

  private Color opponent(Color color) {
    return (color == Color.BLUE) ? Color.PURPLE : Color.BLUE;
  }

  private ChessPiece king(Color color) {
    List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
    for (Piece p : list) {
      if (p instanceof King) {
        return (ChessPiece) p;
      }
    }
    throw new IllegalStateException("There is no " + color + " king on the board");
  }

  public ChessPiece replacePromotedPiece(String type) {
    if (promoted == null) {
      throw new IllegalStateException("There is no piece to be promoted");
    }
    if (!type.equals("B") && !type.equals("N") && !type.equals("R") & !type.equals("Q")) {
      return promoted;
    }
    Position pos = promoted.getChessPosition().toPosition();
    Piece p = board.removePiece(pos);
    piecesOnTheBoard.remove(p);
    ChessPiece newPiece = newPiece(type, promoted.getColor());
    board.placePiece(newPiece, pos);
    piecesOnTheBoard.add(newPiece);
    return newPiece;
  }

  private ChessPiece newPiece(String type, Color color) {
    if (type.equals("B")) return new Bishop(board, color);
    if (type.equals("N")) return new Knight(board, color);
    if (type.equals("Q")) return new Queen(board, color);
    return new Rook(board, color);
  }

  private Piece makeMove(Position source, Position target) {
    ChessPiece p =(ChessPiece) board.removePiece(source);
    p.increaseMoveCount();
    Piece capturedPiece = board.removePiece(target);
    board.placePiece(p, target);

    if (capturedPiece != null) {
      piecesOnTheBoard.remove(capturedPiece);
      capturedPieces.add(capturedPiece);
    }

    // special move king side rook
    if (p instanceof  King && target.getColumn() == source.getColumn() + 2) {
      Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
      Position targetT = new Position(source.getRow(), source.getColumn() + 1);
      ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
      board.placePiece(rook, targetT);
      rook.increaseMoveCount();
    }

    // special move queen side rook
    if (p instanceof  King && target.getColumn() == source.getColumn() - 2) {
      Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
      Position targetT = new Position(source.getRow(), source.getColumn() - 1);
      ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
      board.placePiece(rook, targetT);
      rook.increaseMoveCount();
    }

    // special move en passant
    if (p instanceof Pawn) {
      if (source.getColumn() != target.getColumn() && capturedPiece == null) {
        Position pawnPosition;
        if (p.getColor() == Color.BLUE) {
          pawnPosition = new Position(target.getRow() + 1, target.getColumn());
        } else {
          pawnPosition = new Position(target.getRow() - 1, target.getColumn());
        }
        capturedPiece = board.removePiece(pawnPosition);
        capturedPieces.add(capturedPiece);
        piecesOnTheBoard.remove(capturedPiece);
      }
    }

    return capturedPiece;
  }

  private void undoMove(Position source, Position target, Piece capturedPiece) {
    ChessPiece p = (ChessPiece) board.removePiece(target);
    p.decreaseMoveCount();
    board.placePiece(p, source);

    if (capturedPiece != null) {
      board.placePiece(capturedPiece, target);
      capturedPieces.remove(capturedPiece);
      piecesOnTheBoard.add(capturedPiece);
    }

    // special move king side rook
    if (p instanceof  King && target.getColumn() == source.getColumn() + 2) {
      Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
      Position targetT = new Position(source.getRow(), source.getColumn() + 1);
      ChessPiece rook = (ChessPiece) board.removePiece(targetT);
      board.placePiece(rook, sourceT);
      rook.decreaseMoveCount();
    }

    // special move queen side rook
    if (p instanceof  King && target.getColumn() == source.getColumn() - 2) {
      Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
      Position targetT = new Position(source.getRow(), source.getColumn() - 1);
      ChessPiece rook = (ChessPiece) board.removePiece(targetT);
      board.placePiece(rook, sourceT);
      rook.decreaseMoveCount();
    }

    // special move en passant
    if (p instanceof Pawn) {
      if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
        ChessPiece pawn = (ChessPiece) board.removePiece(target);
        Position pawnPosition;
        if (p.getColor() == Color.BLUE) {
          pawnPosition = new Position(3, target.getColumn());
        } else {
          pawnPosition = new Position(4, target.getColumn());
        }
        board.placePiece(pawn, pawnPosition);
      }
    }
  }

  private boolean testCheck(Color color) {
    Position kingPosition = king(color).getChessPosition().toPosition();
    List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == opponent(color)).toList();
    for (Piece p : opponentPieces) {
      boolean[][] mat = p.possibleMoves();
      if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
        return true;
      }
    }
    return false;
  }

  private boolean testCheckMate(Color color) {
    if (!testCheck(color)) {
      return false;
    }
    List<Piece> pieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
    for (Piece p : pieces) {
      boolean[][] mat = p.possibleMoves();
      for (int i = 0; i < board.getRows(); i++) {
        for (int j = 0; i < board.getColumns(); i++) {
          if (mat[i][j]) {
            Position source = ((ChessPiece) p).getChessPosition().toPosition();
            Position target = new Position(i, j);
            Piece capturedPiece = makeMove(source, target);
            boolean testCheck = testCheck(color);
            undoMove(source, target, capturedPiece);
            if (!testCheck) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  private void placeNewPiece(char column, int row, ChessPiece piece) {
    board.placePiece(piece, new ChessPosition(column, row).toPosition());
    piecesOnTheBoard.add(piece);
  }

  private void initialSetup() {
    placeNewPiece('d', 1, new Queen(board, Color.BLUE));
    placeNewPiece('c', 1, new Bishop(board, Color.BLUE));
    placeNewPiece('f', 1, new Bishop(board, Color.BLUE));
    placeNewPiece('b', 1, new Knight(board, Color.BLUE));
    placeNewPiece('g', 1, new Knight(board, Color.BLUE));
    placeNewPiece('a', 1, new Rook(board, Color.BLUE));
    placeNewPiece('e', 1, new King(board, Color.BLUE, this));
    placeNewPiece('h', 1, new Rook(board, Color.BLUE));
    placeNewPiece('a', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('b', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('c', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('d', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('e', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('f', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('g', 2, new Pawn(board, Color.BLUE, this));
    placeNewPiece('h', 2, new Pawn(board, Color.BLUE, this));

    placeNewPiece('d', 8, new Queen(board, Color.PURPLE));
    placeNewPiece('b', 8, new Knight(board, Color.PURPLE));
    placeNewPiece('g', 8, new Knight(board, Color.PURPLE));
    placeNewPiece('c', 8, new Bishop(board, Color.PURPLE));
    placeNewPiece('f', 8, new Bishop(board, Color.PURPLE));
    placeNewPiece('a', 8, new Rook(board, Color.PURPLE));
    placeNewPiece('e', 8, new King(board, Color.PURPLE, this));
    placeNewPiece('h', 8, new Rook(board, Color.PURPLE));
    placeNewPiece('a', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('b', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('c', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('d', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('e', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('f', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('g', 7, new Pawn(board, Color.PURPLE, this));
    placeNewPiece('h', 7, new Pawn(board, Color.PURPLE, this));
  }
}
