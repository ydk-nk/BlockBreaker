package myGame;

import ydk.game.sprite.Sprite;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.abs;

public class BallManager
{
    // static
    public static final Point DEFAULT_START_POS
            = new Point(Game.WIDTH / 2, Game.FLOOR_Y - Ball.SIZE);    //ボールの左上の座標
    private static BufferedImage img_ball;

    private final BallArrayList<Ball> balls;
    private Point preLaunchPos;
    private boolean anyLanded;

    private List<Block> blocks;

    public BallManager(BufferedImage src, List<Block> list)
    {
        img_ball = src;
        this.blocks = list;

        this.balls = new BallArrayList<>();
        this.preLaunchPos = DEFAULT_START_POS;
        this.init();
    }

    public void init()
    {
        balls.clear();
        for (int i = 0; i < 6; i++) {
            balls.add(new Ball(img_ball, DEFAULT_START_POS));
        }
    }

    public List<Ball> getBalls()
    {
        return this.balls;
    }

    public void setBlocks(List<Block> list)
    {
        this.blocks = list;
    }

    public List<Block> getBlocks()
    {
        return this.blocks;
    }

    public void draw(Graphics2D g2d)
    {
        Sprite.draw(balls, g2d);
    }


    public GameState update(GameState gameState)
    {
        switch (gameState)
        {
            case MAIN_MENU:
                break;

            case NOW_CLICKED:
                prepareLaunch(gameState);
                gameState = GameState.BALL_FLYING;
                break;

            case BALL_FLYING:
                gameState =  ballMove(gameState);
                break;

            case CLICK_WAIT:
            case BLOCK_DOWN:
                break;
        }

        return gameState;
    }

    //=========================================================================================================

    private boolean isPrepareLaunchPosision(Ball b)
    {
        int xdiff = abs((int)b.getX() - preLaunchPos.x);
        if (xdiff <= Ball.SPEED_ARRANGEMENT)
            b.setX(preLaunchPos.x);
        return (int)b.getX() == preLaunchPos.x && (int)b.getY() == preLaunchPos.y;
    }

    private void prepareLaunch(GameState gameState) //発射準備
    {
        System.out.println("prepareLaunch");
        this.anyLanded = false;
        /*マウスに向けて飛ぶように速度(向き)を算出 */
        //ボールの左上の座標
        final double nextX = gameState.mousePos.x - Ball.SIZE / 2;
        final double nextY = gameState.mousePos.y - Ball.SIZE / 2;

        // 角度計算
        final double rad = Math.atan2(nextY - preLaunchPos.y,
                nextX - preLaunchPos.x);

        final double vx = Ball.SPEED_FLY * Math.cos(rad); // x 方向の速度
        final double vy = Ball.SPEED_FLY * Math.sin(rad);

        for (int i = 0; i < balls.size(); i++) {
            Ball b = balls.get(i);
            b.setDelay(i * 8);
            b.setLanded(false);
            b.setVx(vx); //速度設定
            b.setVy(vy);
            b.setisPrepareLaunchPos(false);
        }
    }

    private GameState ballMove(GameState gameState)
    {
        boolean allisPreLaunchPos = true;

        // 更新
        for (Ball v : balls)
        {
            if (v.isLanded()) //地面についているとき
            {
                if (!anyLanded) { //まだ誰も着地していない時
                    preLaunchPos.x = (int)v.getX();
                    preLaunchPos.y = Game.FLOOR_Y - Ball.SIZE;
                    anyLanded = true;
                }

                v.setVy(0);
                v.setY(preLaunchPos.y);

                if (isPrepareLaunchPosision(v)) {
                    v.setVx(0); // 止める
                    v.setisPrepareLaunchPos(true);
                } else {
                    // TODO: 発射場所へ水平移動
                    if ((int)v.getX() < preLaunchPos.x) { //定位置よりも左にある
                        v.setVx(Ball.SPEED_ARRANGEMENT);
                    } else {
                        v.setVx(-(Ball.SPEED_ARRANGEMENT));
                    }
                }
            }
            else    //まだ飛んでいる時
            {
                // TODO: ブロックとの当たり判定
//                Iterator<Block> it = blocks.iterator();
//                while(it.hasNext())
//                {
//                    Block b = it.next();
//                    if (v.getBounds().collision(b.getBounds())) {
//                        onHitBlock(v, b);
//                        b.addDamage();
//                         ブロックとの衝突数は1つまで
//                        break;
//                    }
//                }
                colideJudge(v);
            }
            allisPreLaunchPos &= v.isPrepareLaunchPos();
            v.update(gameState.keyPressed_space ? 2.2 : 1.0);
        }

        if (allisPreLaunchPos) {
            gameState = GameState.CLICK_WAIT;
        }

        return gameState;
    }

    private void colideJudge(final Ball v)
    {
        RectBounds.CornerCollisionState corner = new RectBounds.CornerCollisionState();
        Iterator<Block> it = blocks.iterator();
        final RectBounds ballBounds = v.getBounds();

        while (it.hasNext()) {
            Block b = it.next();
            final RectBounds blockBounds = b.getBounds();
            if (ballBounds.collision(blockBounds)) {
                corner.orAll( RectBounds.getCornerCollisionState(ballBounds, blockBounds));
                b.addDamage();
            }
        }
        checkHitBlock(v, corner);
    }

    private void onHitBlock(Ball v, RectBounds.Location location)
    {
        System.out.println("ball location: " + location);
        switch (location) {
            case RIGHT:
            case LEFT:
                v.invertVx();
                break;
            case TOP:
            case BOTTOM:
                v.invertVy();
                break;

            case RIGHT_BOTTOM:
                if (v.getVx() > 0) v.invertVx();
                v.setVy(-1 * abs(v.getVy()));
                break;
            case LEFT_BOTTOM:
                if (v.getVx() < 0) v.invertVx();
                v.setVy(-1 * abs(v.getVy()));
                break;

            case RIGHT_TOP:
                if (v.getVx() > 0) v.invertVx();
                v.setVy(abs(v.getVy()));
                break;
            case LEFT_TOP:
                if (v.getVx() < 0) v.invertVx();
                v.setVy(abs(v.getVy()));
                break;
        }
        v.update(1);
    }

    private void checkHitBlock(Ball v, RectBounds.CornerCollisionState corner)
    {
        RectBounds.Location location = corner.whereCollisionAt();
        if (location != RectBounds.Location.NIL) {
            onHitBlock(v, location);
        }
    }

    private void onHitBlock(Ball v, Block block)
    {
        RectBounds.Location location = RectBounds.whereCollisionAt(v.getBounds(), block.getBounds());
        onHitBlock(v, location);
    }

    static private class BallArrayList<E> extends ArrayList<E>
    {
        public E frontElement()
        {
            return this.get(0);
        }

        public E backElement()
        {
            return this.get( this.size()-1 );
        }
    }
}
