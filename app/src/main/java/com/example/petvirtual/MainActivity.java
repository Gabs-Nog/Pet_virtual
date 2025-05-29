package com.example.petvirtual;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.CycleInterpolator;

public class MainActivity extends AppCompatActivity {

    // Estágio inicial do pet
    private boolean isOvo = true;
    private int aquecimentos = 0;
    private final int AQUECIMENTOS_PARA_ECLODIR = 5;

    // Componentes da interface
    private Button feedButton, playButton, buttonMedicar, buttonAquecer, buttonEclodir;
    private ImageView petImage, eggImage;
    private ProgressBar lifeBar;
    private LinearLayout buttonLayout;

    // Estados do pet
    private boolean isPetHappy = false;
    private boolean isTouchMoving = false;
    private boolean isPetDoente = false;
    private boolean isPetBravo = false;
    private boolean isChorando = false;
    private boolean isEggStage = true;

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

    // Decaimento
    private static final int VIDA_NORMAL_DECRESCIMO = 1;
    private static final int VIDA_DOENTE_DECRESCIMO = 3;
    private static final int INTERVALO_VIDA = 10000;
    private static final int TEMPO_OCIOSO = 15000;
    private static final int TEMPO_FICAR_DOENTE = 5 * 60 * 1000;

    // Sons
    private MediaPlayer somFeliz, somDoente, somChorar, somBravo, somAlimentar;

    // Vida decaindo
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
        setContentView(R.layout.activity_main);

        // Vincula componentes
        buttonLayout = findViewById(R.id.buttonLayout);
        feedButton = findViewById(R.id.feedButton);
        playButton = findViewById(R.id.playButton);
        buttonMedicar = findViewById(R.id.buttonMedicar);
        buttonAquecer = findViewById(R.id.buttonAquecer);
        buttonEclodir = findViewById(R.id.buttonEclodir);
        eggImage = findViewById(R.id.eggImage);
        petImage = findViewById(R.id.petImage);
        lifeBar = findViewById(R.id.lifeBar);

        // Carrega sons
        somFeliz = MediaPlayer.create(this, R.raw.som_feliz);
        somDoente = MediaPlayer.create(this, R.raw.som_doente);
        somChorar = MediaPlayer.create(this, R.raw.som_chorar);
        somBravo = MediaPlayer.create(this, R.raw.som_bravo);
        somAlimentar = MediaPlayer.create(this, R.raw.som_alimentar);

        // Inicialmente: só ovo visível
        eggImage.setVisibility(View.VISIBLE);

        // Vincula componentes da interface
        feedButton = findViewById(R.id.feedButton);
        playButton = findViewById(R.id.playButton);
        buttonMedicar = findViewById(R.id.buttonMedicar);
        petImage = findViewById(R.id.petImage);
        lifeBar = findViewById(R.id.lifeBar);
        eggImage = findViewById(R.id.eggImage);
        buttonAquecer = findViewById(R.id.buttonAquecer);
        buttonEclodir = findViewById(R.id.buttonEclodir);

        // Inicialmente, mostra o ovo e oculta os controles do pet
        petImage.setImageResource(R.drawable.ovo);
        petImage.setVisibility(View.GONE);
        lifeBar.setVisibility(View.GONE);
        feedButton.setVisibility(View.GONE);
        playButton.setVisibility(View.GONE);
        buttonMedicar.setVisibility(View.GONE);
        buttonEclodir.setVisibility(View.GONE);


        // Ajusta padding para sistema de barras (status/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botão aquecer ovo
        buttonAquecer.setOnClickListener(v -> {
            if (!isOvo) return;
            aquecimentos++;
            // Aqui você pode animar o ovo se quiser
        // Botão Aquecer (fase do ovo)
        buttonAquecer.setOnClickListener(v -> {
            if (!isOvo) return;

            aquecimentos++;
            animarAquecimentoOvo();

            if (aquecimentos >= AQUECIMENTOS_PARA_ECLODIR) {
                Toast.makeText(this, "O ovo está pronto para eclodir!", Toast.LENGTH_SHORT).show();
                buttonEclodir.setVisibility(View.VISIBLE);
                buttonAquecer.setEnabled(false);
            }
        });

        // Botão Eclodir (libera o pet)
        buttonEclodir.setOnClickListener(v -> {
            eggImage.setVisibility(View.GONE);
            buttonAquecer.setVisibility(View.GONE);
            buttonEclodir.setVisibility(View.GONE);
            petImage.setVisibility(View.VISIBLE);
            lifeBar.setVisibility(View.VISIBLE);
            feedButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
            buttonMedicar.setVisibility(View.VISIBLE);
            isEggStage = false;
            isOvo = false;
            iniciarTimerDoenca();
            iniciarMonitoramentoInatividade();
            vidaHandler.postDelayed(vidaRunnable, INTERVALO_VIDA);
        });

            if (aquecimentos >= AQUECIMENTOS_PARA_ECLODIR) {
                Toast.makeText(this, "O ovo está pronto para eclodir!", Toast.LENGTH_SHORT).show();
                buttonEclodir.setVisibility(View.VISIBLE);
                buttonAquecer.setEnabled(false);
            }
        });

        // Botão eclodir ovo
        buttonEclodir.setOnClickListener(v -> {
            eggImage.setVisibility(View.GONE);
            buttonAquecer.setVisibility(View.GONE);
            buttonEclodir.setVisibility(View.GONE);
            petImage.setVisibility(View.VISIBLE);
            lifeBar.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
            feedButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
            buttonMedicar.setVisibility(View.VISIBLE);
            isEggStage = false;
            isOvo = false;
            iniciarTimerDoenca();
            iniciarMonitoramentoInatividade();
            vidaHandler.postDelayed(vidaRunnable, INTERVALO_VIDA);
        });

        // Botão alimentar
        feedButton.setOnClickListener(v -> {
            if (isPetDoente) {
                Toast.makeText(this, "O pet está doente e não quer comer!", Toast.LENGTH_SHORT).show();
            } else {
                animarAlimentar();
                somAlimentar.start();
                resetarTimerDoenca();
            }
            resetarInatividade();
        });

        // Botão de jogar
>>>>>>> b22453416960a8a5dc6c6f96a61622fece235dd9
        playButton.setOnClickListener(v -> {
            abrirMenuMinigames();
            resetarInatividade();
        });

        // Botão medicar
        buttonMedicar.setOnClickListener(v -> {
            if (isPetDoente) {
                isPetDoente = false;
                Toast.makeText(this, "Você medicou o pet!", Toast.LENGTH_SHORT).show();
                atualizarImagemPet(R.drawable.pet);
                resetarTimerDoenca();
            } else {
                Toast.makeText(this, "O pet está saudável!", Toast.LENGTH_SHORT).show();
            }
            resetarInatividade();
        });

<<<<<<< HEAD
        // Interação ao tocar no pet (carinho / tapas)
=======
        // Interações de toque no pet
>>>>>>> b22453416960a8a5dc6c6f96a61622fece235dd9
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
                        somFeliz.start();
                        Toast.makeText(this, "Você fez carinho no pet!", Toast.LENGTH_SHORT).show();
                        
                        new Handler().postDelayed(() -> {
                            atualizarImagemPet(R.drawable.pet);
                            isPetHappy = false;
                        }, 1000);
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
                            somBravo.start();
                            isPetBravo = true;
                            new Handler().postDelayed(() -> atualizarImagemPet(R.drawable.pet), 2000);
                        }
                        resetarInatividade();
                    }
                    return true;
            }
            return false;
        });
    }

    // Animação de tremor do ovo
    private void animarAquecimentoOvo() {
        Animation tremor = new TranslateAnimation(-10, 10, 0, 0);
        tremor.setDuration(100);
        tremor.setInterpolator(new CycleInterpolator(5));
        eggImage.startAnimation(tremor);
    }

>>>>>>> b22453416960a8a5dc6c6f96a61622fece235dd9
    private void atualizarImagemPet(int resId) {
        runOnUiThread(() -> petImage.setImageResource(resId));
    }

    private void animarImagensSequenciais(int[] imagens, int intervalo, int repeticoes) {
        Handler handler = new Handler();
        for (int i = 0; i < repeticoes; i++) {
            final int index = i;
            handler.postDelayed(() -> atualizarImagemPet(imagens[index % imagens.length]), i * intervalo);
        }
    }

    private void iniciarAnimacaoChoro() {
        isChorando = true;
        somChorar.start();
        final int[] imagens = {R.drawable.pet_triste1, R.drawable.pet_triste2};
        animarImagensSequenciais(imagens, 200, 8);
        new Handler().postDelayed(() -> {
            atualizarImagemPet(R.drawable.pet);
            isChorando = false;
            isPetBravo = false;
        }, 1600);
    }

    private void iniciarAnimacaoDoente() {
        somDoente.start();
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

    private void adoecerPet(String motivo) {
        if (!isPetDoente) {
            isPetDoente = true;
            Toast.makeText(this, motivo, Toast.LENGTH_SHORT).show();
            iniciarAnimacaoDoente();
        }
    }

    private void iniciarTimerDoenca() {
        doencaRunnable = () -> adoecerPet("O pet ficou doente! 😷");
        doencaHandler.postDelayed(doencaRunnable, TEMPO_FICAR_DOENTE);
    }

    private void resetarTimerDoenca() {
        doencaHandler.removeCallbacks(doencaRunnable);
        iniciarTimerDoenca();
    }

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

    private void resetarInatividade() {
        idleHandler.removeCallbacks(idleRunnable);
        idleHandler.postDelayed(idleRunnable, TEMPO_OCIOSO);
    }

    private void verificarEstadoDeSaude() {
        if (vida < 75 && !isPetDoente) {
            if ((int) (Math.random() * 10) == 0) {
                adoecerPet("O pet adoeceu por estar fraco!");
            }
        }
    }

    private void abrirMenuMinigames() {
        Intent intent = new Intent(this, MinigamesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Liberar media players para evitar vazamento de memória
        if (somFeliz != null) somFeliz.release();
        if (somDoente != null) somDoente.release();
        if (somChorar != null) somChorar.release();
        if (somBravo != null) somBravo.release();
        if (somAlimentar != null) somAlimentar.release();
        vidaHandler.removeCallbacks(vidaRunnable);
        doencaHandler.removeCallbacks(doencaRunnable);
        idleHandler.removeCallbacks(idleRunnable);
    }
}
