package myGame;

import java.awt.Point;

public enum GameState
{
    MAIN_MENU,
    CLICK_WAIT,
    NOW_CLICKED,
    BALL_FLYING,
    BLOCK_DOWN,
    GAMEOVER;

    public Point mousePos;
    public boolean keyPressed_space = false;
}
