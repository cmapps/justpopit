package com.charlesmass.justpopit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

public class Classic extends Activity {
    static private float SPEED_BEGINNING;
    static final private int MAX_VIES = 3;//Vitesse au depart

    private int diff = 1, highScore = 0, compteurPops = 0, hauteurEcran = 0, largeurEcran = 0, nbVies = 3, largeurBallon = 0;
    private float speed;

    private boolean cestPerdu = false, afficherLaFenetreDeDialogueDeDefaite = true;

    private RelativeLayout rlMain = null;
    private ImageView ivFirstHeart = null, ivSecondHeart = null, ivThirdHeart = null;
    private TextView tvNbPops = null, tvHighScore = null, tvPourLaPause = null;
    private ImageButton ibSoundOnOff = null;

    private Random r = null;
    private Handler handler = null;
    private ArrayList<Boolean> thisOneStillBreathe = null;

    private boolean pause = false, soundOn = false;

    private int idSonPaf = 0, idSonBoum = 0;
    private SoundPool spPlayer = null;

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_game);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager. LayoutParams.FLAG_FULLSCREEN);//On cache la barre du haut


        Display display = getWindowManager().getDefaultDisplay();//On obtient l'ecran via le gestionnaire de fenêtre
        Point size = new Point();
        display.getSize(size);//Taille de l'ecran
        hauteurEcran = size.y;
        largeurEcran = size.x;

        largeurBallon = largeurEcran/11;

        SPEED_BEGINNING = (float) 0.002 * hauteurEcran;//La vitesse depend de la hauteur de l'ecran
        speed = SPEED_BEGINNING;//La vitesse depend de la hauteur de l'ecran

        //Chargement des sons et récuperation de leurs identifiants
        spPlayer = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        idSonPaf = spPlayer.load(Classic.this, R.raw.pop, 1);
        idSonBoum = spPlayer.load(Classic.this, R.raw.bipboum, 2);

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

        updateTvHighScore();//On met tout de suite le record actuel


            r = new Random();//Generateur de nombres aleatoires (ne pas utiliser pour sécuriser quoi que ce soit)

        final Runnable runnable = new Runnable() {
            int compteurSpeed = 0, compteurIndex = 0;

            @Override
            public void run() {
                if(!pause){
                    if(compteurSpeed > 10 / diff){//Plus la diff est grande, plus la vitesse augmente vite
                        speedUp();//On augmente la vitesse
                        compteurSpeed = 0;//Et on remet à 0 pour augmenter la vitesse plus tard
                    }
                    compteurSpeed++;//On incremente à chaque tour pour augmenter la difficulte ensuite

                    int kindNew = r.nextInt(50);//Si 0, reste 0. Si 1, reste 1, sinon, décrit ci-dessous
                    if(kindNew > 1 && kindNew < 10){
                        kindNew = Ballon.KIND_RED;
                    }else if(kindNew == 49){//1 chance sur 50
                        kindNew = Ballon.KIND_BLUE;
                    }else if(kindNew > 9){
                        kindNew = Ballon.KIND_WHITE;
                    }
                    final Ballon b = new Ballon(Classic.this, kindNew, largeurBallon);
                    //Placement
                    b.setX(-15 + r.nextInt(100) * (largeurEcran - 40) / 99);//Pourcentage aléatoire pour placer le ballon sur l'horizontale EDIT(1.1.2) : correction (c'etait trop a droite)
                    b.setY(-130);//On ne doit pas voir le bouton à sa création

                    switch(b.getKind()){
                        case Ballon.KIND_WHITE:
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!pause){
                                        b.setClickable(false);//Sinon, on peut encore cliquer dessus pendant la crevure
                                        thisOneStillBreathe.set(b.getId(), false);//This one no longer breathe *lol*
                                        compteurPops++;//Un nouveau pop!
                                        if(compteurPops > highScore) {//Si le nb de pops dépasse le record
                                            highScore++;//On incrémente le record en mm temps que le nombre de pops
                                            updateTvHighScore();
                                        }
                                        updateTVNbPops(compteurPops);//On affiche le nouveau nb de pops
                                        if(soundOn)
                                            spPlayer.play(idSonPaf, 1/*Volume gauche*/, 1/*volume droite*/, 1/*Priorité*/, 0/*loop (repetition)*/, 1/*vitesse*/);
                                        b.crevure();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                rlMain.removeView(b);//Si on touche un ballon, il disparait
                                            }
                                        }, 1000);
                                    }
                                }
                            });
                            break;
                        case Ballon.KIND_YELLOW://Ralentissement
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!pause){
                                        b.setClickable(false);//Sinon, on peut encore cliquer dessus pendant la crevure
                                        thisOneStillBreathe.set(b.getId(), false);//This one no longer breathe *lol*
                                        compteurPops++;//Un nouveau pop!
                                        if(compteurPops > highScore) {//Si le nb de pops dépasse le record
                                            highScore++;//On incrémente le record en mm temps que le nombre de pops
                                            updateTvHighScore();
                                        }
                                        speed = SPEED_BEGINNING;//Pouvoir des ballons jaunes
                                        updateTVNbPops(compteurPops);//On affiche le nouveau nb de pops
                                        if(soundOn)
                                            spPlayer.play(idSonPaf, 1/*Volume gauche*/, 1/*volume droite*/, 1/*Priorité*/, 0/*loop (repetition)*/, 1/*vitesse*/);
                                        b.crevure();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                rlMain.removeView(b);//Si on touche un ballon, il disparait
                                            }
                                        }, 1000);
                                    }
                                }
                            });
                            break;
                        case Ballon.KIND_RED://Bombe atomique
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!pause){
                                        b.setClickable(false);//Sinon, on peut encore cliquer dessus pendant la crevure
                                        if(soundOn)
                                            spPlayer.play(idSonBoum, 1/*Volume gauche*/, 1/*volume droite*/, 1/*Priorité*/, 0/*loop (repetition)*/, 1/*vitesse*/);
                                        b.crevure();
                                        afficherLaFenetreDeDialogueDeDefaite = false;
                                        cestPerdu = true;//Flemme de changer le nom de la variable
                                        writeScore();//Actualise les scores
                                        LayoutInflater li =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                        LinearLayout layout = (LinearLayout) li.inflate(getResources().getLayout(R.layout.popup_gameover), null);

                                        Button bBack = (Button) layout.findViewById(R.id.bBack);
                                        bBack.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Classic.this.finish();
                                            }
                                        });
                                        TextView tv = (TextView) layout.findViewById(R.id.tvYouLost);
                                        tv.setText("You just blew everything up!\nYou lost with " + compteurPops + " pops !");
                                        PopupWindow popUp = new PopupWindow(layout, 500, 500, true);
                                        popUp.setTouchable(true);
                                        popUp.setFocusable(false);
                                        popUp.setOutsideTouchable(false);
                                        popUp.showAtLocation(layout, Gravity.CENTER, 0, 0);
                                        popUp.update(0, 0, popUp.getWidth(), popUp.getHeight());
                                        /*
                                        AlertDialog.Builder adb = new AlertDialog.Builder(Classic.this);
                                        adb.setTitle("Game Over");
                                        adb.setMessage("You just blew everything up !\nYou lost with " + compteurPops + " pops !");
                                        adb.setCancelable(false);//On veut pas qu'il puisse cancel parce qu'on veut qu'il appuie sur le bouton
                                        adb.setPositiveButton(getString(R.string.bouton_defaite), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Classic.this.finish();//Le click sur le bouton va virer la page de jeu
                                            }
                                        });
                                        adb.show();*/
                                    }
                                }
                            });
                            break;
                        case Ballon.KIND_BLUE://Récupère des vies
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!pause){
                                        b.setClickable(false);//Sinon, on peut encore cliquer dessus pendant la crevure
                                        thisOneStillBreathe.set(b.getId(), false);//This one no longer breathe *lol*
                                            compteurPops++;//Un nouveau pop!
                                        if(compteurPops > highScore) {//Si le nb de pops dépasse le record
                                            highScore++;//On incrémente le record en mm temps que le nombre de pops
                                            updateTvHighScore();
                                        }
                                        if(nbVies != MAX_VIES){
                                            nbVies = MAX_VIES;//Les viex reviennent au max
                                            updateVies();//On actualise les images
                                        }
                                        updateTVNbPops(compteurPops);//On affiche le nouveau nb de pops
                                        if(soundOn)
                                            spPlayer.play(idSonPaf, 1/*Volume gauche*/, 1/*volume droite*/, 1/*Priorité*/, 0/*loop (repetition)*/, 1/*vitesse*/);
                                        b.crevure();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                rlMain.removeView(b);//Si on touche un ballon, il disparait
                                            }
                                        }, 1000);
                                    }
                                }
                            });
                            break;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rlMain.addView(b);//On ne peut ajouter une vue que dans la phase d'interface
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
                                Classic.this.finish();
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
                        /*AlertDialog.Builder adb = new AlertDialog.Builder(Classic.this);
                        adb.setTitle("Game Over");
                        adb.setMessage("You lost with " + compteurPops + " pops !");
                        adb.setCancelable(false);//On veut pas qu'il fasse autre chose que "ok"
                        adb.setPositiveButton(getString(R.string.bouton_defaite), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Classic.this.finish();//On enleve la page de jeu quand il clique sur ok
                            }
                        });
                        adb.show();*/
                    }else{
                        if(!cestPerdu) {//Tant qu'on a pas perdu
                            handler.postDelayed(this, (long) (800 / speed) + r.nextInt(300));//On relance le runnable tant que la partie n'est pas terminée
                        }
                    }//Plus la vitesse est grande, plus les ballons apparaissent vite (*copaiiiiin*)
                }else{
                    if(!cestPerdu){//Tant qu'on a pas perdu
                        handler.post(this);//On continue jusqu'a ce que la pause s'arrete
                    }

                }
            }
        };
        handler = new Handler();
        //QUESTION : Lorsque le runnable est lancé avec un delai de 500 ms, le ballon apparait au milieu de l'écran...???
        handler.postDelayed(runnable, 1000);//On lance le runnable dans un thread séparé

        ImageButton ibPauseClassic = (ImageButton) findViewById(R.id.ibPauseClassic);
        ibPauseClassic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause = !pause;
                if (pause) {
                    tvPourLaPause.setVisibility(View.VISIBLE);
                    ibSoundOnOff.setVisibility(View.VISIBLE);
                    ibSoundOnOff.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            soundOn = !soundOn;
                            if(soundOn)
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
    }


    private void moveDown(final Ballon ib, final int index){
        final Handler h = new Handler();

        Runnable r = new Runnable() {
            int compteur = 0;//Compteur de hauteur

            @Override
            public void run() {
                if(!pause){
                    //Tout d'abord, si un ballon a atteint l'arrivée, inutile d'aller plus loin :
                    if(cestPerdu){
                        ViewGroup lay = (ViewGroup)ib.getParent();
                        lay.removeView(ib);
                    }else{//Si aucun ballon n'a atteint l'arrivée
                        if(thisOneStillBreathe.get(index)){//Si l'utilisateur n'a pas touche le ballon

                            if(diff == Accueil.DIFF_NIGHTMARE){//En difficulté nightmare, les ballons peuvent se déplacer aléatoirement sur l'axe X
                                if(ib.isLateralDroite()){//Si latéral droite
                                    ib.setX(ib.getX() + speed);//Deplacement a droite
                                    if(ib.getX() > largeurEcran - 35)//Si valeur limite a droite, on change de deplacement latéral
                                        ib.reverseLateralDroite();
                                }else{//Si lateral gauche
                                    ib.setX(ib.getX() - speed);//Deplacement a gauche
                                    if(ib.getX() < -15)//Si valeur limite a gauche
                                        ib.reverseLateralDroite();
                                }
                            }

                            ib.setY(ib.getY() + speed * hauteurEcran / 800);//On descend en fonction de la vitesse choisie
                            if(compteur < hauteurEcran){//Tant que j'ai pas atteint le bas EDIT(1.1.2) on a changé 50 en 30 : c'etait trop en bas
                                // h.postDelayed(this, 10);//On descend d'1px dans 10ms
                                h.post(this);//On relance le run
                                compteur += speed;//Le compteur doit augmenter en meme temps que la descente du ballon
                            }else{//Si je suis en bas
                                rlMain.removeView(ib);//Normalement inutile (prévient les bugs)
                                if(ib.getKind() == Ballon.KIND_BLUE){//Si le ballon est bleu
                                    nbVies-=2;//Deux vies d'un coup
                                    updateVies();
                                }else if(ib.getKind() != Ballon.KIND_RED){//Blanc ou jaune
                                    nbVies--;//Il perd une vie
                                    updateVies();
                                }else{//Si le ballon est rouge, il ne fait pas perdre de vie en arrivant en bas
                                    compteurPops++;//Au contraire il fait plus 1 pop
                                    updateTVNbPops(compteurPops);
                                    if(compteurPops > highScore){
                                        highScore++;//On augmente en meme temps le record
                                        updateTvHighScore();
                                    }
                                }
                            }
                        }
                    }
                }else{
                    if(!cestPerdu){
                        h.post(this);//On continue juqu'à ce que la pause s'arrete
                    }
                }
            }
        };
        h.postDelayed(r, 10);//Premier lancement du runnable
    }

    private void speedUp(){
        speed += 0.0005 * hauteurEcran;
    }

    private void updateTVNbPops(int nb){
        if(nb == 1){
            tvNbPops.setText("Pop : " + nb);
        }else{
            tvNbPops.setText("Pops : " + nb);
        }
    }

    private void updateTvHighScore(){
        tvHighScore.setText("High score : " + highScore);
    }

    private void updateVies(){
        if(nbVies < 0){
            cestPerdu = true;
            ivFirstHeart.setVisibility(View.INVISIBLE);
            ivSecondHeart.setVisibility(View.INVISIBLE);
            ivThirdHeart.setVisibility(View.INVISIBLE);
        }else{
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
    }

    private void writeScore(){

        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(this);
        int scoreFich;
        switch(diff){
            case Accueil.DIFF_EASY:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_easy), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_easy));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_easy), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
            case Accueil.DIFF_REGULAR:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_regular), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_regular));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_regular), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
            case Accueil.DIFF_HARD:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_hard), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_hard));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_hard), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
            case Accueil.DIFF_NIGHTMARE:
                scoreFich = sharedP.getInt(getString(R.string.shared_scores_nightmare), 0);

                if(scoreFich < compteurPops){//Si le score précédent est inférieur au nouveau
                    SharedPreferences.Editor edit = sharedP.edit();
                    edit.remove(getString(R.string.shared_scores_nightmare));//ON supprime l'ancien
                    edit.putInt(getString(R.string.shared_scores_nightmare), compteurPops);//On place le nouveau via la même clé
                    edit.apply();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {//Lorsque l'utilisateur a appuyé sur la touche retour
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            afficherLaFenetreDeDialogueDeDefaite = false;//Sinon, ça va poser souci gamin.
            Classic.this.finish();
        }
        return true;//Sinon, ça va aussi poser souci
    }
}
