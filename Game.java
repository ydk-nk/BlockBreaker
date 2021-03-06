package myGame;

import ydk.game.engine.BufferingRenderer;
import ydk.game.engine.GameEngine;
import ydk.game.engine.GamePanel;
import ydk.game.engine.GameProcess;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Game implements GameProcess
{
    public static final int WIDTH   = 720;      // ウィンドウサイズ
    public static final int HEIGHT  = 540;
    public static final int FLOOR_Y = HEIGHT - 30;  //床の座標
    public static final int STATUS_PANEL_X = 520;   //ステータスパネルの座標
    public static final URL url_menuMP3, url_mainGameMP3, url_explosion, url_coin;
    public static BufferedImage
            img_ball, img_block, img_bonusPanel, img_hexagonBack, img_floor, img_glossPanel,
            img_gameover, img_logo, img_1up;
    private static Cursor cursor_DEFAULT, cursor_MY_CROSS;
    private static final String RESOURCE = "/myGame/resources/";

    private final Component     screen;
    private final BlockManager  blockManager;
    private final BallManager   ballManager;
    private final ScoreRenderer scoreRenderer;
    private final SessionRenderer sessionRenderer;

    private GameState gameState;
    private static final int RUNCHECK_INTERVAL = 120;
    private int runChecker = RUNCHECK_INTERVAL;

    private MP3Player mp3Menu, mp3mainGame;

    static {    //最初に一度だけ実行: 画像,音声読み込み
        System.out.println("Game static init");
        try {
            img_ball        = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "ball.png"));
            img_block       = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "block-dark.png"));
            img_bonusPanel  = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "bonusPanel.png"));
            img_hexagonBack = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "hexagon-back.jpeg"));
            img_floor       = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "floor.png"));
            img_glossPanel  = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "gloss-panel.png"));
            img_gameover    = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "gameover.jpg"));
            img_logo        = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "logo.jpeg"));
            img_1up         = ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "1UP.png"));

            // マウスカーソル
            cursor_MY_CROSS = Toolkit.getDefaultToolkit().createCustomCursor(
                    ImageIO.read(Game.class.getResourceAsStream(RESOURCE + "cursor.png")),
                    new Point(10, 10),
                    "myCrossCursor"
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        cursor_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
        url_menuMP3     = Game.class.getResource(RESOURCE + "dance.MP3");
        url_mainGameMP3 = Game.class.getResource(RESOURCE + "digitalworld.MP3");
        url_explosion   = Game.class.getResource(RESOURCE + "explosion.MP3");
        url_coin        = Game.class.getResource(RESOURCE + "coin.MP3");
    }

    public static void main(String[] args)
    {
        new Game(new GamePanel(WIDTH, HEIGHT));
    }

    private Game(final BufferingRenderer renderer)
    {
        this.gameState = new GameState();
        this.gameState.state = GameState.State.CLICK_WAIT;
        this.screen = (Component) renderer;
        this.blockManager = new BlockManager(img_block);
        this.ballManager = new BallManager(img_ball, blockManager.getBlocks());
        this.scoreRenderer = new ScoreRenderer();
        this.sessionRenderer = new SessionRenderer();

        // 初期のカーソル
        this.screen.setCursor(cursor_DEFAULT);

        // マウスやキーのイベントリスナーの設定
        this.eventListenInit(this.screen);
        this.screen.requestFocus();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                GameEngine ge = new GameEngine(renderer, Game.this);
                ge.setWindowTitle("Block breaker");
                ge.setFps(68);
                ge.start();
            }
        });
    }

    @Override
    public void initialize()
    {
        gameState.init();
        ballManager.init();
        blockManager.init();
        scoreRenderer.init();
        sessionRenderer.init();
        System.out.println("Game#initialied()   state: " + gameState.state);

        mp3Menu = new MP3Player(url_menuMP3, true); //タイトル画面の時のBGMをループ再生(別スレッド)
    }

    @Override
    public void update()
    {
        this.gameState = ballManager.update(this.gameState);
        this.gameState = blockManager.update(this.gameState);
        this.gameState = scoreRenderer.update(this.gameState);

        //スscoreRenderer(テータスパネル描画)のターン数やボール数,スコアを更新
        this.scoreRenderer.setWaveCount(gameState.getWaveCount());
        this.scoreRenderer.setBallCount( ballManager.getBallCount() );
        this.scoreRenderer.setScore(gameState.getScore());
        // gameStateのボール数を更新
        this.gameState.setBallCount(ballManager.getBallCount());

        this.sessionRenderer.update(gameState);

        // もしGameOverでBGMがまだなっていたら止める
        if (gameState.state == GameState.State.GAMEOVER && mp3mainGame != null) {
            mp3mainGame.stop();
            mp3mainGame = null;
        }

        //デバック用
        if (--runChecker < 0) {
            runChecker = RUNCHECK_INTERVAL;
            System.out.println("[RUNNING] update()" + "\tstate: " + gameState.state);
        }
    }

    @Override
    public void render(Graphics2D g2d)
    {
        g2d.drawImage(img_hexagonBack, 0, 0, null);
        ballManager.draw(g2d);
        blockManager.draw(g2d);

        g2d.drawImage(img_floor, 0, FLOOR_Y, null);
        scoreRenderer.draw(g2d);

        switch (gameState.state) {
            case MAIN_MENU:
            case GAMEOVER:
            case RETURNABLE_TO_MENU:
                sessionRenderer.draw(g2d, gameState);
                break;
        }

        //デバック用
//        if (--runChecker < 0) {
//            runChecker = RUNCHECK_INTERVAL;
//            System.out.println("[RUNNING] render()");
//        }
    }

    private void eventListenInit(final Component screen)
    {
        this.screen.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent ev)
            {
                // CLICK_WAITの時にマウスの左ボタンがクリックされたら gameState を変更
                System.out.println("mouse: " + ev.getX() + ", " + ev.getY() + "   state: " + gameState);
                Game.this.screen.requestFocus();

                switch (gameState.state) {
                    // タイトル画面でクリックされたらBGMを変更し,gameStateを変える
                    case MAIN_MENU:
                        gameState.state = GameState.State.CLICK_WAIT;
                        if (mp3Menu != null) {
                            mp3Menu.stop();
                        }
                        mp3mainGame = new MP3Player(url_mainGameMP3, true);
                        break;
                    // クリック待ち状態でクリックされた
                    case CLICK_WAIT:
                        //ボタン,クリック位置の判定
                        if ( (ev.getButton() == MouseEvent.BUTTON1)
                                && ev.getY() < FLOOR_Y - (Ball.SIZE + 25)
                                && ev.getX() < STATUS_PANEL_X)
                        {
                            gameState.state = GameState.State.NOW_CLICKED;
                            gameState.mousePos.x = ev.getX();
                            gameState.mousePos.y = ev.getY();
                        }
                        break;

                    // GameOverの後
                    case RETURNABLE_TO_MENU:
                        gameState.state = GameState.State.MAIN_MENU;
                        initialize();
                        break;
                }
            }
        });

        // スペースキーが押されたか
        this.screen.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent ev)
            {
                switch (ev.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        if (gameState.state != GameState.State.CLICK_WAIT) {
                            gameState.keyPressed_space = true;
                        }
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent ev)
            {
                switch (ev.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        gameState.keyPressed_space = false;
                        break;
                    case KeyEvent.VK_SHIFT: //シフトキーが押されたらマウスカーソルの画像をトグル
                        if (screen.getCursor().equals(cursor_DEFAULT)) {
                            screen.setCursor(cursor_MY_CROSS);
                        } else {
                            screen.setCursor(cursor_DEFAULT);
                        }
                }
            }
        });
    }

}
