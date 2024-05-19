package boardgame;

import chess.ChessPiece;

public class Board {
  private Integer rows;
  private Integer columns;
  private Piece[][] pieces;

  public Board(Integer columns, Integer rows) {
    if (rows < 1 || columns < 1) {
      throw new BoardException("Error creating board: there must be a least 1 row and 1 column");
    }
    this.columns = columns;
    this.rows = rows;
    pieces = new Piece[rows][columns];
  }

  public Integer getRows() {
    return rows;
  }

  public Integer getColumns() {
    return columns;
  }

  public Piece piece(int row, int column) {
    if (!positionExists(row, column)) {
      throw new BoardException("Position not on the board");
    }
    return pieces[row][column];
  }

  public Piece piece(Position position) {
    if (!positionExists(position.getRow(), position.getColumn())) {
      throw new BoardException("Position not on the board");
    }
    return pieces[position.getRow()][position.getColumn()];
  }

  public void placePiece(Piece piece, Position position) {
    if (thereIsAPiece(position)) {
      throw new BoardException("There is already a piece on position " + position);
    }
    pieces[position.getRow()][position.getColumn()] = piece;
    piece.position = position;
  }

  public boolean positionExists(int row, int column) {
    return row >= 0 && row < rows && column >= 0 && column < columns;
  }

  public boolean positionExists(Position position) {
    return positionExists(position.getRow(), position.getColumn());
  }

  public boolean thereIsAPiece(Position position) {
    if (!positionExists(position.getRow(), position.getColumn())) {
      throw new BoardException("Position not on the board");
    }
    return piece(position) != null;
  }
}
