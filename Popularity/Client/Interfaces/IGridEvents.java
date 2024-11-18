package Popularity.Client.Interfaces;

public interface IGridEvents extends IGameEvents{
    public void onUpdateOccupied(int x, int y, int count);
    public void onGridDimensions(int w, int h);

    public void onResetCells();
    public void onConfirmMove(int x, int y);
}
