package com.charlesmass.justpopit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by charlesmass on 09/03/2015.
 */
public class Rush extends Activity {
    static final private int MAX_VIES = 3;//Vitesse au depart
    static private float SPEED_BEGINNING;

    private int diff = 1, highScore = 0, compteurPops = 0, hauteurEcran = 0, largeurEcran = 0, compteurIndex = 0, indexTableau = 0, nbVies = MAX_VIES, largeurBallon = 0;
    private float speed;
    private boolean cestPerdu = false, afficherLaFenetreDeDialogueDeDefaite = true, pause = false;

    private RelativeLayout rlMain = null;
    private ImageView ivFirstHeart = null, ivSecondHeart = null, ivThirdHeart = null;
    private TextView tvNbPops = null, tvHighScore = null, tvPourLaPause = null;
    private ImageButton ibSoundOnOff = null;
    private View vFront = null;

    private Random r = null;
    private Handler handler = null;

    private ArrayList<Boolean> thisOneStillBreathe = null;
    private ArrayList<Ballon> presentsOnScreen = null;

    private boolean soundOn = false;

    private int idSonPaf = 0;
    private SoundPool spPlayer = null;


    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_rush);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//On cache la barre du haut

        Display display = getWindowManager().getDefaultDisplay();//On obtient l'ecran via le gestionnaire de fenêtre
        Point size = new Point();
        display.getSize(size);//Taille de l'ecran
        hauteurEcran = size.y;
        largeurEcran = size.x;

        largeurBallon = largeurEcran/11;


        SPEED_BEGINNING = (float) 0.0025 * hauteurEcran;//La vitesse depend de la hauteur de l'ecran (legerement superieure a celle du Classic)
        speed = SPEED_BEGINNING;

        //Chargement des sons et récuperation de leurs identifiants
        spPlayer = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        idSonPaf = spPlayer.load(Rush.this, R.raw.pop, 1);

        thisOneStillBreathe = new ArrayList<>();//Vrai si le bouton avec cet index est encore présent

        diff = this.getIntent().getIntExtra(getString(R.string.difficulty_key), 1);//On récupère la diff
        highScore = this.getIntent().getIntExtra(getString(R.string.high_score_key), 0);//On récupère le record actuel

        //Sons
        ibSoundOnOff = (ImageButton)findViewById(R.id.ibSound);
        ibSoundOnOff.setVisibility(View.INVISIBLE);
        //Vies
        ivFirstHeart = (ImageView)findViewById(R.id.ivFirstHeart);
        ivSecondHeart = (ImageView)findViewById(R.id.ivSecondHeart);
        ivThirdHeart = (ImageView)findViewById(R.id.ivThirdHeart);
        //Layout parent
        rlMain = (RelativeLayout)findViewById(R.id.rlMain);
        //Scores
        tvNbPops = (TextView)findViewById(R.id.tvNbPops);
        tvHighScore = (TextView)findViewById(R.id.tvHighScore);
        //"Pause"
        tvPourLaPause = (TextView)findViewById(R.id.tvPourLaPause);
        tvPourLaPause.setVisibility(View.INVISIBLE);
        presentsOnScreen = new ArrayList<>();
        //Vue sur laquelle l'utilisateur pose son doigt
        vFront = new View(this);
        vFront.setLayoutParams(new ActionBar.LayoutParams(this.largeurEcran, this.hauteurEcran));
        rlMain.addView(vFront);

        vFront.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX(), y = event.getY();
                if(!pause){
                    for(int i = 0 ; i < presentsOnScreen.size() ; i++){
                        final Ballon b = presentsOnScreen.get(i);
                        if(x >= b.getX() - 10 && x <= b.getX() + 10 + b.getWidth() && y >= b.getY() - 10 && y <= b.getY() + 10 + b.getHeight()){//EDIT(1.2.2) on le rend moins précis (plus facile)
                            if(soundOn)
                                spPlayer.play(idSonPaf, 1, 1, 1, 0, 1);
                            b.crevure();//Animation d'eclatement
                            presentsOnScreen.remove(i);
                            thisOneStillBreathe.set(b.getId(), false);//On rajoute l'entier pour correspondre à l'index de moveDown
                            indexTableau--;//Sinon le tableau sera plus petit que le nouvel index quand on créera un nouveau bouton
                            compteurPops++;//Plus un pop
                            if(highScore < compteurPops){
                                highScore = compteurPops;//Si l'highscore est dépassé, il augmente
                            }
                            updateTVNbPops();//Actualisation des scores
                            updateTvHighScore();
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rlMain.removeView(b);//On retarde la destruction du bouton pour afficher l'eclatement
                                }
                            }, 1000);
                        }
                    }
                    return true;
                }else{
                    if(x > largeurEcran - 30 && y > hauteurEcran - 30){//Si le bonhomme essaie de toucher pause, quoi!
                        pause = false;//Si un ballon est sur le menu pause, on peut plus y toucher donc on a le seum
                        tvPourLaPause.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
            }
        });

      /*  rlMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX(), y = event.getY();
                if(!pause){
                    for(int i = 0 ; i < presentsOnScreen.size() ; i++){
                        final Ballon b = presentsOnScreen.get(i);
                        if(x >= b.getX() - 10 && x <= b.getX() + 10 + b.getWidth() && y >= b.getY() - 10 && y <= b.getY() + 10 + b.getHeight()){//EDIT(1.2.2) on le rend moins précis (plus facile)
                            if(soundOn)
                                spPlayer.play(idSonPaf, 1, 1, 1, 0, 1);
                            b.crevure();//Animation d'eclatement
                            presentsOnScreen.remove(i);
                            thisOneStillBreathe.set(b.getId(), false);//On rajoute l'entier pour correspondre à l'index de moveDown
                            indexTableau--;//Sinon le tableau sera plus petit que le nouvel index quand on créera un nouveau bouton
                            compteurPops++;//Plus un pop
                            if(highScore < compteurPops){
                                highScore = compteurPops;//Si l'highscore est dépassé, il augmente
                            }
                            updateTVNbPops();//Actualisation des scores
                            updateTvHighScore();
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rlMain.removeView(b);//On retarde la destruction du bouton pour afficher l'eclatement
                                }
                            }, 1000);
                        }
                    }
                    return true;
                }else{
                    if(x > largeurEcran - 30 && y > hauteurEcran - 30){//Si le bonhomme essaie de toucher pause, quoi!
                        pause = false;//Si un ballon est sur le menu pause, on peut plus y toucher donc on a le seum
                        tvPourLaPause.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
            }
        });*/

        updateTvHighScore();//On met tout de suite le record actuel

        r = new Random();//Generateur de nombres aleatoires (ne pas utiliser pour sécuriser quoi que ce soit)

        final ImageButton ibPause = (ImageButton)findViewById(R.id.ibPauseRush);
        ibPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = !pause;
                if (pause) {
                    ibPause.bringToFront();
                    tvPourLaPause.setVisibility(View.VISIBLE);
                    tvPourLaPause.bringToFront();//On la fout devant
                    ibSoundOnOff.setVisibility(View.VISIBLE);
                    ibSoundOnOff.bringToFront();//idem
                    ibSoundOnOff.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            soundOn = !soundOn;
                            if (soundOn)
                                ibSoundOnOff.setBackgroundResource(R.drawable.son_on_mini);
                            else
                                ibSoundOnOff.setBackgroundResource(R.drawable.son_off_mini);
                        }
                    });
                } else {
                    tvPourLaPause.setVisibility(View.INVISIBLE);
                    ibSoundOnOff.setVisibility(View.INVISIBLE);
                }
            }
        });

        Runnable runnable = new Runnable() {
            int compteurSpeed = 0;

            @Override
            public void run() {
                if(!pause){
                    if(compteurSpeed > 10 / diff){//Plus la diff est grande, plus la vitesse augmente vite
                        speedUp();//On augmente la vitesse
                        compteurSpeed = 0;//Et on remet à 0 pour augmenter la vitesse plus tard
                    }
                    compteurSpeed++;//On incremente à chaque tour pour augmenter la difficulte ensuite

                    final Ballon b = new Ballon(Rush.this, Ballon.KIND_WHITE, largeurBallon);
                    presentsOnScreen.add(indexTableau, b);
                    indexTableau++;
                    //Placement
                    b.setX(-15 + r.nextInt(100) * (largeurEcran - 30) / 99);//Pourcentage aléatoire pour placer le ballon sur l'horizontale EDIT(1.1.2) : correction (c'etait trop a droite)
                    b.setY(-130);//On ne doit pas voir le bouton à sa création

                    b.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(!pause){
                                if(b.isTouchable()){
                                    b.setTouchable(false);//On peut le toucher qu'une fois
                                    b.crevure();
                                    presentsOnScreen.remove(b);
                                    thisOneStillBreathe.set(b.getId(), false);//On rajoute l'entier pour correspondre à l'index de moveDown
                                    indexTableau--;//Sinon le tableau sera plus petit que le nouvel index quand on créera un nouveau bouton
                                    compteurPops++;//Plus un pop
                                    if(highScore < compteurPops){
                                        highScore = compteurPops;//Si l'highscore est dépassé, il augmente
                                    }
                                    updateTVNbPops();//Actualisation des scores
                                    updateTvHighScore();
                                    Handler h = new Handler();
                                    h.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            rlMain.removeView(b);//On retarde la destruction du bouton pour afficher l'eclatement
                                        }
                                    }, 1000);
                                }
                            }
                            return true;
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rlMain.addView(b);//On ne peut ajouter une vue que dans la phase d'interface
                            rlMain.bringChildToFront(vFront);
                            rlMain.bringChildToFront(ibPause);
                            b.setId(compteurIndex);
                            thisOneStillBreathe.add(compteurIndex, true);//This one began breathing
                        }
                    });
                    moveDown(b, compteurIndex);
                    compteurIndex++;//Augmente à chaque nouveau bouton

                    if(cestPerdu && afficherLaFenetreDeDialogueDeDefaite){
                        writeScore();//On actualise les records
                        LayoutInflater li =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout layout = (LinearLayout) li.inflate(getResources().getLayout(R.layout.popup_gameover), null);

                        Button bBack = (Button) layout.findViewById(R.id.bBack);
                        bBack.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Rush.this.finish();
                            }
                        });
                        TextView tv = (TextView) layout.findViewById(R.id.tvYouLost);
                        tv.setText("You lost with " + compteurPops + " pops !");
                        PopupWindow popUp = new PopupWindow(layout, 500, 500, true);
                        popUp.setTouchable(true);
                        popUp.setFocusable(false);
                        popUp.setOutsideTouchable(false);
                        popUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
                        popUp.update(0, 0, popUp.getWidth(), popUp.getHeight());


                        /*AlertDialog.Builder adb = new AlertDialog.Builder(Rush.this);
                        adb.setTitle("Game Over");
                        adb.setMessage("You lost with " + compteurPops + " pops !");
                        adb.setCancelable(false);//On veut pas qu'il fasse autre chose que "ok"
                        adb.setPositiveButton(getString(R.string.bouton_defaite), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Rush.this.finish();//On enleve la page de jeu quand il clique sur ok
                            }
                        });
                        adb.show();*/
                        afficherLaFenetreDeDialogueDeDefaite = false;//Voir OnKeyDown tout en bas
                    }else{
                        if(!cestPerdu){
                            handler.postDelayed(this, (long)(150/speed) + r.nextInt(100));//On relance le runnable tant que la partie n'est pas terminée
                        }
                    }//Plus la vitesse est grande, plus les ballons apparaissent vite (*copaiiiiin*)
                }else{
                    if(!cestPerdu) {
                        handler.post(this);//On continue jusqu'à ce que la pause s'arrete
                    }
                }
            }
        };

        handler = new Handler();
        //QUESTION : Lorsque le runnable est lancé avec un delai de 500 ms, le ballon apparait au milieu de l'écran...???
        handler.postDelayed(runnable, 1000);//On lance le runnable dans un thread séparé


    }

    private void moveDown(final Ballon ib, final int index){
        final Handler h = new Handler();

        Runnable r = new Runnable() {
            int compteur = 0;//Compteur de hauteur

            @Override
            public void run() {
                if(!pause){
                    //Tout d'abord, si un ballon a atteint l'arrivée, inutile d'aller plus loin :
                    if(cestPerdu){//Si un ballon est arrivé
                        ViewGroup lay = (ViewGroup)ib.getParent();//On chope le parent
                        lay.removeView(ib);//Et on vire le bouton
                    }else{//Si aucun ballon n'a atteint l'arrivée
                        //On retranche le nombre de ballons deja éclatés. Sinon, lorsque j'eclate un ballon, L'index du dernier arrivé n'existe plus
                        if(thisOneStillBreathe.get(index)){//Si l'utilisateur n'a pas touche le ballon

                            if(ib.isLateralDroite()){//Si latéral droite
                                ib.setX(ib.getX() + speed);//Deplacement a droite
                                if(ib.getX() > largeurEcran - 35)//Si valeur limite a droite, on change de deplacement latéral
                                    ib.reverseLateralDroite();
                            }else{//Si lateral gauche
                                ib.setX(ib.getX() - speed);//Deplacement a gauche
                                if(ib.getX() < -15)//Si valeur limite a gauche
                                    ib.reverseLateralDroite();
                            }

                            ib.setY(ib.getY() + speed * (hauteurEcran / 800));//On descend en fonction de la vitesse choisie
                            if(compteur < hauteurEcran + 30){//Tant que j'ai pas atteint le bas EDIT(1.1.2) on a changé 50 en 30 : c'etait trop en bas
                                // h.postDelayed(this, 10);//On descend d'1px dans 10ms
                                h.post(this);//On relance le run
                                compteur += speed;//Le compteur doit augmenter en meme temps que la descente du ballon
                            }else{//Si je suis en bas
                                rlMain.removeView(ib);//Normalement inutile (prévient les bugs)
                                nbVies--;//Bim une vie en moins
                                updateVies();//Affichage des vies et gestion de la defaite
                            }
                        }
                    }
                }else{
                    h.post(this);
                }
            }
        };
        h.postDelayed(r, 1000);//Premier lancement du runnable
    }

    private void writeScore(){

        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(this);
        int scoreFich;
        switch(diff){
            case Accueil.DIFF_EASY:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_easy_rush), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_easy_rush));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_easy_rush), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
            case Accueil.DIFF_REGULAR:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_regular_rush), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_regular_rush));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_regular_rush), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
            case Accueil.DIFF_HARD:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_hard_rush), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_hard_rush));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_hard_rush), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
            case Accueil.DIFF_NIGHTMARE:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_nightmare_rush), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_nightmare_rush));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_nightmare_rush), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
        }
    }

    private void speedUp(){speed += 0.0005 * hauteurEcran;}

    private void updateTvHighScore(){tvHighScore.setText("High score : " + highScore);}

    private void updateTVNbPops(){tvNbPops.setText("Pops : " + compteurPops);}

    private void updateVies(){

        switch(nbVies){
            case 0:
                cestPerdu = true;
                ivFirstHeart.setVisibility(View.INVISIBLE);
                ivSecondHeart.setVisibility(View.INVISIBLE);
                ivThirdHeart.setVisibility(View.INVISIBLE);
                break;
            case 1:
                ivSecondHeart.setVisibility(View.INVISIBLE);
                ivThirdHeart.setVisibility(View.INVISIBLE);
                break;
            case 2:
                ivThirdHeart.setVisibility(View.INVISIBLE);
                break;
            case 3://Si on a popé un bleu
                ivFirstHeart.setVisibility(View.VISIBLE);
                ivSecondHeart.setVisibility(View.VISIBLE);
                ivThirdHeart.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {//Lorsque l'utilisateur a appuyé sur la touche retour
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            afficherLaFenetreDeDialogueDeDefaite = false;//Sinon, ça va poser souci gamin.
            Rush.this.finish();
        }
        return true;//Sinon, ça va aussi poser souci
    }
}
