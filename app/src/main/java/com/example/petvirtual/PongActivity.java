package com.example.petvirtual;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

public class PongActivity extends AppCompatActivity {

    private PongView pongView;
    private FrameLayout frameLayout;
    private Button btnRestart, btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pong);

        frameLayout = findViewById(R.id.frameLayoutPong);

        // Criar a view do jogo
        pongView = new PongView(this);

        // Adicionar PongView ao FrameLayout
        frameLayout.addView(pongView);

        // Botões do layout
        btnRestart = findViewById(R.id.btnRestart);
        btnVoltar = findViewById(R.id.btnVoltar);

        btnRestart.setOnClickListener(v -> pongView.resetGame());
        btnVoltar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onPause() {
        super.onPause();
        pongView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pongView.resume();
    }

    // View do jogo
    class PongView extends SurfaceView implements Runnable {

        private Thread gameThread;
        private SurfaceHolder holder;
        private boolean isPlaying = true;

        private Paint paint;
        private RectF paddle;
        private float paddleX, paddleY, paddleWidth, paddleHeight;

        private float ballX, ballY, ballRadius;
        private float ballVelocityX = 12;
        private float ballVelocityY = 10;

        private int score = 0;

        public PongView(PongActivity context) {
            super(context);
            holder = getHolder();
            paint = new Paint();

            paddleWidth = 300;
            paddleHeight = 30;
            paddleX = 200;
            // NÃO inicializar paddleY aqui, pois getHeight() retorna 0 ainda!

            ballRadius = 50;
            ballX = 500;
            ballY = 500;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // Posicionar a raquete 150 pixels acima da base da view, quando tamanho for conhecido
            paddleY = h - 150;
            resetBall();
        }

        @Override
        public void run() {
            while (isPlaying) {
                if (!holder.getSurface().isValid()) continue;

                update();
                draw();

                try {
                    Thread.sleep(17); // ~60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void update() {
            // Atualiza a posição da bola
            ballX += ballVelocityX;
            ballY += ballVelocityY;

            // Rebater nas laterais (considerando o raio da bola).
            if (ballX - ballRadius < 0 || ballX + ballRadius > getWidth()) {
                ballVelocityX *= -1;
            }

            // Rebater no topo
            if (ballY - ballRadius < 0) {
                ballVelocityY *= -1;
            }

            // Rebater na raquete
            if (ballY + ballRadius >= paddleY &&
                    ballX >= paddleX &&
                    ballX <= paddleX + paddleWidth) {
                ballVelocityY *= -1;
                score++;
            }

            // Se a bola passar do fundo (perder)
            if (ballY - ballRadius > getHeight()) {
                // Resetar posição da bola e pontuação
                resetBall();
                score = 0;
            }
        }

        private void draw() {
            Canvas canvas = holder.lockCanvas();
            if (canvas == null) return;

            // Fundo branco
            canvas.drawColor(0xFFFFFFFF);

            // Desenhar a raquete
            paddle = new RectF(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight);
            paint.setColor(0xFF000000); // Preto
            canvas.drawRect(paddle, paint);

            // Desenhar a bola (aqui pode trocar para a imagem do pet depois)
            paint.setColor(0xFFFFC107); // Amarelo
            canvas.drawCircle(ballX, ballY, ballRadius, paint);

            // Desenhar pontuação
            paint.setColor(0xFF000000);
            paint.setTextSize(60);
            canvas.drawText("Pontos: " + score, 30, 80, paint);

            holder.unlockCanvasAndPost(canvas);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Movimentar a raquete horizontalmente com o toque
            paddleX = event.getX() - paddleWidth / 2;

            // Limitar raquete dentro da tela
            if (paddleX < 0) paddleX = 0;
            if (paddleX + paddleWidth > getWidth()) paddleX = getWidth() - paddleWidth;

            return true;
        }

        public void pause() {
            isPlaying = false;
            try {
                if (gameThread != null) {
                    gameThread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void resume() {
            isPlaying = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void resetGame() {
            resetBall();
            score = 0;
        }

        private void resetBall() {
            ballX = getWidth() / 2f;
            ballY = getHeight() / 2f;
            ballVelocityX = 12 * (Math.random() > 0.5 ? 1 : -1);
            ballVelocityY = 10;
        }
    }
}
