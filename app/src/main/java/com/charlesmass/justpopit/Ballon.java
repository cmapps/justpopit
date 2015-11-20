package com.charlesmass.justpopit;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.RelativeLayout;


import java.io.IOException;
import java.util.Random;

/**
 * Created by charlesmass on 06/03/2015.
 */
public class Ballon extends ImageButton {
    static final public int KIND_WHITE = 0, KIND_YELLOW = 1, KIND_RED = 2, KIND_BLUE = 3;
    static final private int PREMIERE_IMAGE = 0, DEUXIEME_IMAGE = 1, TROISIEME_IMAGE = 2;
    private SoundPool spPlayer = null;
    private int kind, compteurPhoto;//0 blanc 1 jaune 2 rouge
    private boolean lateralDroite = true, isTouchable = true;//True si son deplacement latéral est vers la droite. False sinon

    int explosionId = 0;
    int largeur = 0, hauteur = 0;

    public Ballon(Context context) {//Evite que l'inspection fasse des siennes mais complètement inutile ici
        super(context);
    }

    public Ballon(Context context, AttributeSet attrs) {//idem
        super(context, attrs);
    }

    public Ballon(Context context, int kind, int largeur) {
        super(context);
        this.kind = kind;

        this.largeur = largeur;
        this.hauteur = (largeur * 131) / 100;

        Random r = new Random();
        int quelLateral = r.nextInt(2);
        lateralDroite = quelLateral == 0;//Simplification d'un if else
        setImage();
        setSpPlayer();
    }

    public void setTouchable(boolean b){
        isTouchable = b;
    }

    public boolean isTouchable(){
        return isTouchable;
    }

    public boolean isLateralDroite(){
        return lateralDroite;
    }

    public void reverseLateralDroite(){
        lateralDroite = !lateralDroite;
    }

    public int getKind(){
        return this.kind;
    }

    public void setSpPlayer(){
        spPlayer = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    }

    /*public void setImage(){
        switch(this.kind){
            case KIND_WHITE:
                this.setBackgroundResource(R.drawable.ballon_blanc_2);//Ballon plus gros
                break;
            case KIND_YELLOW:
                this.setBackgroundResource(R.drawable.ballon_jaune);
                break;
            case KIND_RED:
                this.setBackgroundResource(R.drawable.ballon_rouge);
                break;
            case KIND_BLUE:
                this.setBackgroundResource(R.drawable.ballon_bleu);
                break;
        }
    }*/

    public void setImage(){
        switch(this.kind){
            case KIND_WHITE:
                this.setBackgroundResource(R.drawable.bblanc);//Ballon plus gros
                largeur *= 1.2;
                hauteur *= 1.2;
                break;
            case KIND_YELLOW:
                this.setBackgroundResource(R.drawable.bjaune);
                break;
            case KIND_RED:
                this.setBackgroundResource(R.drawable.brouge);
                break;
            case KIND_BLUE:
                this.setBackgroundResource(R.drawable.bbleu);
                largeur *= 1.2;
                hauteur *= 1.2;
                break;
        }
        this.setLayoutParams(new RelativeLayout.LayoutParams(largeur, hauteur));
    }

    private void jouerSon(){

    }

    public void crevure(){//Animation du "pop" avec trois images
        compteurPhoto = 0;

        //mp.start();
        final android.os.Handler h = new android.os.Handler();

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                switch(compteurPhoto){
                    case PREMIERE_IMAGE:
                     //   final int id = spPlayer.load(getContext(), R.raw.paf, 1);
                      //  spPlayer.play(id, 1/*Volume gauche*/, 1/*volume droite*/, 1/*Priorité*/, 0/*loop (repetition)*/, 1/*vitesse*/);
                      /*  spPlayer.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                            @Override
                            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                                spPlayer.play(id, 1, 1, 1, 0, 1);
                            }
                        });*/
                        Ballon.this.setBackgroundResource(R.drawable.pop0);
                        compteurPhoto++;
                        h.postDelayed(this, 150);
                        break;
                    case DEUXIEME_IMAGE:
                        Ballon.this.setBackgroundResource(R.drawable.pop1);
                        compteurPhoto++;
                        h.postDelayed(this, 150);
                        break;
                    case TROISIEME_IMAGE:
                        Ballon.this.setBackgroundResource(R.drawable.pop2);
                        compteurPhoto = 0;
                        break;
                }
            }
        };
        h.post(r);
    }
}
