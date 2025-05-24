package com.example.petvirtual;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Componentes da interface
    private Button feedButton, playButton, buttonMedicar;
    private ImageView petImage;
    private ProgressBar lifeBar;

    // Estados do pet
    private boolean isPetHappy = false;
    private boolean isTouchMoving = false;
    private boolean isPetDoente = false;
    private boolean isPetBravo = false;
    private boolean isChorando = false;

    // Vida do pet
    private int vida = 100;
    private final int VIDA_MAXIMA = 100;

    // Handlers para temporizadores
    private final Handler doencaHandler = new Handler();
    private final Handler idleHandler = new Handler();
    private final Handler vidaHandler = new Handler();

    // Runnables para eventos recorrentes
    private Runnable doencaRunnable;
    private Runnable idleRunnable;

    // Configurações de tempo e decaimento
    private static final int VIDA_NORMAL_DECRESCIMO = 1;
    private static final int VIDA_DOENTE_DECRESCIMO = 3;
    private static final int INTERVALO_VIDA = 10000; // 10 segundos
    private static final int TEMPO_OCIOSO = 15000;   // 15 segundos
    private static final int TEMPO_FICAR_DOENTE = 5 * 60 * 1000; // 5 minutos

    // Reduz vida ao longo do tempo
    private final Runnable vidaRunnable = new Runnable() {
        @Override
        public void run() {
            int decaimento = isPetDoente ? VIDA_DOENTE_DECRESCIMO : VIDA_NORMAL_DECRESCIMO;
            vida = Math.max(vida - decaimento, 0);
            lifeBar.setProgress(vida);
            verificarEstadoDeSaude();
            vidaHandler.postDelayed(this, INTERVALO_VIDA);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configura padding para a tela ocupar o espaço corretamente
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincula componentes da interface
        feedButton = findViewById(R.id.feedButton);
        playButton = findViewById(R.id.playButton);
        buttonMedicar = findViewById(R.id.buttonMedicar);
        petImage = findViewById(R.id.petImage);
        lifeBar = findViewById(R.id.lifeBar);
        lifeBar.setProgress(vida);

        // Botão de alimentar
        feedButton.setOnClickListener(v -> {
            if (isPetDoente) {
                Toast.makeText(this, "O pet está doente e não quer comer!", Toast.LENGTH_SHORT).show();
            } else {
                animarAlimentar();
                resetarTimerDoenca();
            }
            resetarInatividade();
        });

        // Botão de jogar (abre outra tela)
        playButton.setOnClickListener(v -> {
            abrirMenuMinigames();
            resetarInatividade();
        });

        // Botão de medicar
        buttonMedicar.setOnClickListener(v -> {
            if (isPetDoente) {
                isPetDoente = false;
                Toast.makeText(this, "Você medicou o pet e ele está saudável!", Toast.LENGTH_SHORT).show();
                atualizarImagemPet(R.drawable.pet);
                resetarTimerDoenca();
            } else {
                Toast.makeText(this, "O pet está saudável!", Toast.LENGTH_SHORT).show();
            }
            resetarInatividade();
        });

        // Eventos de toque no pet
        petImage.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isTouchMoving = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    isTouchMoving = true;
                    if (!isPetHappy && !isPetDoente) {
                        atualizarImagemPet(R.drawable.pet_feliz);
                        isPetHappy = true;
                        Toast.makeText(this, "Você fez carinho no pet!", Toast.LENGTH_SHORT).show();
                        runOnUiThread(() -> new Handler().postDelayed(() -> {
                            atualizarImagemPet(R.drawable.pet);
                            isPetHappy = false;
                        }, 1000));
                    }
                    resetarInatividade();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!isTouchMoving && !isPetDoente) {
                        vida = Math.max(vida - 10, 0);
                        lifeBar.setProgress(vida);
                        verificarEstadoDeSaude();

                        if (isChorando) {
                            iniciarAnimacaoChoro();
                        } else if (isPetBravo) {
                            iniciarAnimacaoChoro();
                            isPetBravo = false;
                        } else {
                            atualizarImagemPet(R.drawable.pet_bravo);
                            isPetBravo = true;
                            runOnUiThread(() -> new Handler().postDelayed(() -> atualizarImagemPet(R.drawable.pet), 2000));
                        }
                        resetarInatividade();
                    }
                    return true;
            }
            return false;
        });

        // Inicia timers principais
        iniciarTimerDoenca();
        iniciarMonitoramentoInatividade();
        vidaHandler.postDelayed(vidaRunnable, INTERVALO_VIDA);
    }

    // Atualiza a imagem do pet
    private void atualizarImagemPet(int resId) {
        runOnUiThread(() -> petImage.setImageResource(resId));
    }

    // Animações com múltiplas imagens
    private void animarImagensSequenciais(int[] imagens, int intervalo, int repeticoes) {
        Handler handler = new Handler();
        for (int i = 0; i < repeticoes; i++) {
            final int index = i;
            handler.postDelayed(() -> atualizarImagemPet(imagens[index % imagens.length]), i * intervalo);
        }
    }

    // Animação de choro
    private void iniciarAnimacaoChoro() {
        isChorando = true;
        final int[] imagens = {R.drawable.pet_triste1, R.drawable.pet_triste2};
        animarImagensSequenciais(imagens, 200, 8);
        new Handler().postDelayed(() -> {
            atualizarImagemPet(R.drawable.pet);
            isChorando = false;
            isPetBravo = false;
        }, 1600);
    }

    // Animação de pet doente (loop infinito enquanto doente)
    private void iniciarAnimacaoDoente() {
        final int[] imagens = {R.drawable.pet_doente1, R.drawable.pet_doente2};
        Runnable animacao = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (isPetDoente) {
                    atualizarImagemPet(imagens[index % imagens.length]);
                    index++;
                    new Handler().postDelayed(this, 500);
                }
            }
        };
        animacao.run();
    }

    // Animação de alimentação
    private void animarAlimentar() {
        boolean comidaPodre = (int) (Math.random() * 25) == 0;

        if (comidaPodre) {
            adoecerPet("A comida estava podre! O pet ficou doente!");
        } else {
            vida = Math.min(vida + 15, VIDA_MAXIMA);
            Toast.makeText(this, "Você alimentou o pet!", Toast.LENGTH_SHORT).show();
        }

        lifeBar.setProgress(vida);
        verificarEstadoDeSaude();

        int[] imagens = {
                R.drawable.pet_alimentando1,
                R.drawable.pet_alimentando2,
                R.drawable.pet_alimentando3
        };
        animarImagensSequenciais(imagens, 300, imagens.length * 2);
        new Handler().postDelayed(() -> atualizarImagemPet(R.drawable.pet), imagens.length * 2 * 300);
    }

    // Pet adoece
    private void adoecerPet(String motivo) {
        if (!isPetDoente) {
            isPetDoente = true;
            Toast.makeText(this, motivo, Toast.LENGTH_SHORT).show();
            iniciarAnimacaoDoente();
        }
    }

    // Inicia temporizador de doença por inatividade
    private void iniciarTimerDoenca() {
        doencaRunnable = () -> adoecerPet("O pet ficou doente! 😷");
        doencaHandler.postDelayed(doencaRunnable, TEMPO_FICAR_DOENTE);
    }

    // Reinicia o timer de doença
    private void resetarTimerDoenca() {
        doencaHandler.removeCallbacks(doencaRunnable);
        iniciarTimerDoenca();
    }

    // Inicia o monitoramento de inatividade (animações se o pet estiver entediado)
    private void iniciarMonitoramentoInatividade() {
        idleRunnable = () -> {
            if (!isPetDoente) {
                int[] imagens = {
                        R.drawable.pet_olhando_para_direita,
                        R.drawable.pet_olhando_para_esquerda,
                        R.drawable.pet_entediado
                };
                animarImagensSequenciais(imagens, 1000, imagens.length);
                new Handler().postDelayed(() -> atualizarImagemPet(R.drawable.pet), 3500);
            }
            idleHandler.postDelayed(idleRunnable, TEMPO_OCIOSO + 4000);
        };
        idleHandler.postDelayed(idleRunnable, TEMPO_OCIOSO);
    }

    // Reinicia o timer de inatividade
    private void resetarInatividade() {
        idleHandler.removeCallbacks(idleRunnable);
        idleHandler.postDelayed(idleRunnable, TEMPO_OCIOSO);
    }

    // Verifica se o pet deve adoecer por vida baixa
    private void verificarEstadoDeSaude() {
        if (vida < 75 && !isPetDoente) {
            if ((int)(Math.random() * 10) == 0) {
                adoecerPet("O pet adoeceu por estar fraco!");
            }
        }
    }

    // Abre a tela de minigames
    private void abrirMenuMinigames() {
        Intent intent = new Intent(this, MinigamesActivity.class);
        startActivity(intent);
    }

    // Remove todos os callbacks para evitar vazamento de memória
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove o runnable que reduz vida periodicamente
        vidaHandler.removeCallbacks(vidaRunnable);

        // Remove o runnable que cuida da doença do pet
        doencaHandler.removeCallbacks(doencaRunnable);

        // Remove o runnable que monitora inatividade
        idleHandler.removeCallbacks(idleRunnable);
    }
}

