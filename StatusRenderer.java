package myGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class StatusRenderer
{
    private int ballCount;
    private int waveCount;
    private int score;

    private static final BufferedImage img = Game.img_glossPanel;
    private static final int WIDTH = (Game.WIDTH - Game.STATUS_PANEL_X) +10;
    private static final int HEIGHT = Game.HEIGHT + 40;
    private static final int BOTTOM_Y_STRING_WAVE = 30;
    private static final int BOTTOM_Y_STRING_BALL = 160;

    public StatusRenderer() {}

    public void init()
    {
        ballCount = 0;
        waveCount = 1;
        score = 0;
    }

    public void draw(Graphics2D g2d)
    {
        g2d.drawImage(img, Game.STATUS_PANEL_X, 0, WIDTH, HEIGHT, null);

        RenderingHints  defaultReeringHints = g2d.getRenderingHints();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);

        {
            g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
            g2d.drawString("WAVE:", Game.STATUS_PANEL_X + 20, BOTTOM_Y_STRING_WAVE);

            g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
            String S = String.valueOf(waveCount);
            FontMetrics metrics = g2d.getFontMetrics();
            Rectangle rect = metrics.getStringBounds(S, g2d).getBounds();
            final int x = Game.STATUS_PANEL_X + (WIDTH / 2) - (rect.width / 2);
            final int y = BOTTOM_Y_STRING_WAVE + metrics.getAscent() + 10;
            g2d.drawString(S, x, y);
        }
        {
            g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
            g2d.drawString("BALL  :", Game.STATUS_PANEL_X + 20, BOTTOM_Y_STRING_BALL);
            g2d.drawImage(Game.img_ball, Game.STATUS_PANEL_X + 76, BOTTOM_Y_STRING_BALL - Ball.SIZE, null);

            g2d.setFont(new Font(Font.MONOSPACED, Font.BOLD, 40));
            String S = String.valueOf(ballCount);
            FontMetrics metrics = g2d.getFontMetrics();
            Rectangle rect = metrics.getStringBounds(S, g2d).getBounds();
            final int x = Game.STATUS_PANEL_X + (WIDTH / 2) - (rect.width / 2);
            final int y = BOTTOM_Y_STRING_BALL + metrics.getAscent() + 10;
            g2d.drawString(S, x, y);
        }

        g2d.setRenderingHints(defaultReeringHints);
    }

    public int getBallCount()
    {
        return ballCount;
    }

    public void setBallCount(int ballCount)
    {
        this.ballCount = ballCount;
    }

    public int getWaveCount()
    {
        return waveCount;
    }

    public void setWaveCount(int waveCount)
    {
        this.waveCount = waveCount;
    }

    public void waveCountUp()
    {
        this.waveCount++;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public void addScore(int value)
    {
        this.score += value;
    }
}