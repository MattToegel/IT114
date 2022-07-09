package LifeForLife.common;

import java.io.Serializable;

/**
 * Created as a basic wrapper for a point.
 * The Point class could have unintended oddeties
 */
public class Vector2 implements Serializable{
    public static final long serialVersionUID = 5L;
    public int x, y;
    public Vector2(int x, int y){
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString(){
        return String.format("{%s,%s}", x,y);
    }
}
