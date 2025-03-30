package itstep.learning.android_212;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    private final String bestScoreFilename = "best.score";
    private final Random random = new Random();
    private TextView tvScore;
    private TextView tvBestScore;
    private long score;
    private long bestScore;
    private final int N = 4;
    private final int[][] tiles = new int[N][N];
    private final TextView[][] tvTiles = new TextView[N][N];
    private Animation spawnAnimation;
    private Animation collapseAnimation;
    private GameState savedState = null;


    @SuppressLint({"ClickableViewAccessibility", "DiscouragedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        View mainLayout = findViewById(R.id.game_layout_main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvTiles[i][j] = findViewById(
                        getResources().getIdentifier(
                                "game_tv_tile_" + i + j,
                                "id",
                                getPackageName()
                        )
                );
            }
        }
        spawnAnimation = AnimationUtils.loadAnimation( this, R.anim.game_spawn );
        collapseAnimation = AnimationUtils.loadAnimation( this, R.anim.game_collapse );
        findViewById( R.id.game_btn_undo ).setOnClickListener( v -> undoGameState() );
        tvScore = findViewById( R.id.game_tv_score );
        tvBestScore = findViewById( R.id.game_tv_best );
        tvScore.setOnClickListener( v -> {
            if( moveLeft() ) {
                spawnTile();
                updateField();
            }
            else {
                Toast.makeText(GameActivity.this, "NO Left move", Toast.LENGTH_SHORT).show();
            }
        });
        tvBestScore.setOnClickListener( v -> {
            if( canMoveRight() ) {
                saveGameState();
                moveRight();
                spawnTile();
                updateField();
            }
            else {
                Toast.makeText(GameActivity.this, "NO move", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout gameField = findViewById( R.id.game_layout_field );
        /*
        На етапі onCreate активність ще не "зверстана" - розмітка завантажена, об'єкти
        створені, але реальні розміри ще не розраховані. Для того щоб виконати дії після
        готовності елемента йому передають задачі методом post
         */
        gameField.post( () -> {
            int windowWidth = this.getWindow().getDecorView().getWidth();
            // задаємо відступи (margin - частина розмірів, до вікна не належить)
            int fieldMargins = 20;
            // Замінюємо параметри шаблона (layout) для поля на нові
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    windowWidth - 2 * fieldMargins,
                    windowWidth - 2 * fieldMargins
            );
            params.setMargins( fieldMargins, fieldMargins, fieldMargins, fieldMargins );
            params.gravity = Gravity.CENTER;
            gameField.setLayoutParams( params );
        });
        gameField.setOnTouchListener( new OnSwipeListener( this ) {
            @Override
            public void onSwipeBottom() {
                Toast.makeText(GameActivity.this, "onSwipeBottom", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSwipeLeft() {
                if( moveLeft() ) {
                    spawnTile();
                    updateField();
                }
                else {
                    Toast.makeText(GameActivity.this, "NO Left move", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onSwipeRight() {
                if( canMoveRight() ) {
                    saveGameState();
                    moveRight();
                    spawnTile();
                    updateField();
                }
                else {
                    Toast.makeText(GameActivity.this, "NO move", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onSwipeTop() {
                Toast.makeText(GameActivity.this, "onSwipeTop", Toast.LENGTH_SHORT).show();
            }
        } );
        loadBestScore();
        startNewGame();
    }

    private void saveGameState() {
        savedState = new GameState(score, bestScore, new int[N][N]);
        for (int i = 0; i < N; i++) {
            System.arraycopy( tiles[i], 0, savedState.tiles[i], 0, N );
        }
    }

    private void undoGameState() {
        if( savedState == null ) {
            new AlertDialog.Builder(this, android.R.style.ThemeOverlay_Material_Dialog_Alert)
                    .setIcon( android.R.drawable.ic_dialog_alert )
                    .setTitle( "Дія неможлива" )
                    .setMessage( "Немає збереженого руху. Множинні UNDO у платній підписці")
                    .setPositiveButton( "Підписатись", (dlg, btn) -> {} )
                    .setNegativeButton( "Продовжити", (dlg, btn) -> {} )
                    .setNeutralButton( "Завершити", (dlg, btn) -> finish() )
                    .show();
            return;
        }

        for (int i = 0; i < N; i++) {
            System.arraycopy( savedState.tiles[i], 0, tiles[i], 0, N );
        }
        score = savedState.score;
        bestScore = savedState.bestScore;
        saveBestScore();
        savedState = null;
        updateField();
    }

    private void loadBestScore() {
        try( FileInputStream fis = openFileInput( bestScoreFilename );
             DataInputStream dis = new DataInputStream( fis )
        ) {
            bestScore = dis.readLong() ;
        }
        catch( IOException ex ) {
            bestScore = 0L;
            Log.w( "GameActivity::loadBestScore", "File read error: " + ex.getMessage() );
        }
    }

    private void saveBestScore() {
        try( FileOutputStream fos = openFileOutput( bestScoreFilename, Context.MODE_PRIVATE ) ;
             DataOutputStream dos = new DataOutputStream( fos )
        ) {
            dos.writeLong( bestScore );
        }
        catch( IOException ex ) {
            Log.w( "GameActivity::saveBestScore", "File save error: " + ex.getMessage() );
        }
    }

    private boolean canMoveRight() {
        for (int i = 0; i < N; i++) {
            for (int j = 1; j < N; j++) {
                if (tiles[i][j] != 0 && tiles[i][j - 1] == tiles[i][j]  ||
                    tiles[i][j] == 0 && tiles[i][j - 1] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean moveRight() {
//        [2002]        [0022]       [0004]          [0004]
//        [2202]  зсув  [0222]  пари [0204]  зсув    [0024]
//        [2222]  -->   [2222]  -->  [0404]  -->     [0044]
//        [2200]        [0022]       [0004]          [0004]
        boolean res = shiftRight();
        for (int i = 0; i < N; i++) {
            for (int j = N-1; j > 0; j--) {
                if( tiles[i][j] == tiles[i][j-1] && tiles[i][j] != 0 ) {
                    tiles[i][j] *= 2;
                    tiles[i][j-1] = 0;
                    res = true;
                    score += tiles[i][j];
                    tvTiles[i][j].setTag( collapseAnimation );
                }
            }
        }
        return shiftRight() || res;
    }

    private boolean shiftRight() {
        boolean res = false;
        for (int i = 0; i < N; i++) {
            for (int k = 1; k < N; k++) {
                for (int j = 1; j < N; j++) {
                    if (tiles[i][j] == 0 && tiles[i][j - 1] != 0) {
                        tiles[i][j] = tiles[i][j - 1];
                        tiles[i][j - 1] = 0;
                        res = true;
                        if( tvTiles[i][j - 1].getTag() != null ) {
                            tvTiles[i][j].setTag( tvTiles[i][j - 1].getTag() );
                            tvTiles[i][j - 1].setTag( null );
                        }
                    }
                }
            }
        }
        return res;
    }

    private boolean moveLeft() {
        boolean res = shiftLeft();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N - 1; j++) {
                if( tiles[i][j] == tiles[i][j+1] && tiles[i][j] != 0 ) {
                    tiles[i][j] *= 2;
                    tiles[i][j+1] = 0;
                    res = true;
                    score += tiles[i][j];
                    tvTiles[i][j].setTag( collapseAnimation );
                }
            }
        }
        return shiftLeft() || res;
    }

    private boolean shiftLeft() {
        boolean res = false;
        for (int i = 0; i < N; i++) {
            for (int k = 1; k < N; k++) {
                for (int j = 0; j < N - 1; j++) {
                    if (tiles[i][j] == 0 && tiles[i][j + 1] != 0) {
                        tiles[i][j] = tiles[i][j + 1];
                        tiles[i][j + 1] = 0;
                        res = true;
                        if( tvTiles[i][j + 1].getTag() != null ) {
                            tvTiles[i][j].setTag( tvTiles[i][j + 1].getTag() );
                            tvTiles[i][j + 1].setTag( null );
                        }
                    }
                }
            }
        }
        return res;
    }

    private void startNewGame() {
        score = 0L;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // tiles[i][j] = (int) Math.pow(2, i + j + 1);
                // if(tiles[i][j] > 64) tiles[i][j] = 0;
                tiles[i][j] = 0;
            }
        }
        spawnTile();
        spawnTile();
        updateField();
    }

    private boolean spawnTile() {
        // Поява нового значення: з імовірністю 1/10 - четвірка, 9/10 - двійка
        // У випадковому місці поля

        // Збираємо всі порожні клітинки
        List<Coords> coords = new ArrayList<>(N * N );
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if(tiles[i][j] == 0) {
                    coords.add( new Coords(i, j) );
                }
            }
        }
        if( coords.isEmpty() ) {
            return false;
        }
        Coords c = coords.get( random.nextInt( coords.size() ) ) ;
        tiles[c.i][c.j] = random.nextInt( 10 ) == 0 ? 4 : 2;
        tvTiles[c.i][c.j].setTag( spawnAnimation );
        return true;
    }

    @SuppressLint("DiscouragedApi")
    private void updateField() {
        tvScore.setText( getString( R.string.game_tv_score_tpl, scoreToString( score ) ) );
        if( score > bestScore ) {
            bestScore = score;
            // TODO: запустити анімацію tvBestScore символізуючи побиття рекорду
            saveBestScore();
        }
        tvBestScore.setText( getString( R.string.game_tv_best_tpl, scoreToString( bestScore ) ) );

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tvTiles[i][j].setText( String.valueOf( tiles[i][j] ) );

                tvTiles[i][j].getBackground().setColorFilter(  // ~ backgroundTint
                        getResources().getColor(
                                getResources().getIdentifier(
                                        "game_tv_tile_bg_" + tiles[i][j],
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        ),
                        PorterDuff.Mode.SRC_ATOP
                );

                tvTiles[i][j].setTextColor(
                        getResources().getColor(
                                getResources().getIdentifier(
                                        "game_tv_tile_fg_" + tiles[i][j],
                                        "color",
                                        getPackageName()
                                ),
                                getTheme()
                        )
                );
                if( tvTiles[i][j].getTag() instanceof Animation ) {
                    tvTiles[i][j].startAnimation( (Animation) tvTiles[i][j].getTag() );
                    tvTiles[i][j].setTag( null );
                }
            }
        }
    }

    private String scoreToString( long score ) {
        return String.valueOf( score );
    }

    private static class Coords {
        private final int i;
        private final int j;

        public Coords(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    private static class GameState {
        private final long score;
        private final long bestScore;
        private final int[][] tiles;

        private GameState(long score, long bestScore, int[][] tiles) {
            this.score = score;
            this.bestScore = bestScore;
            this.tiles = tiles;
        }
    }
}
/*
Д.З. Завершити роботу з проєктом 2048
До ДЗ додати скріншоти / відеозаписи.
 */