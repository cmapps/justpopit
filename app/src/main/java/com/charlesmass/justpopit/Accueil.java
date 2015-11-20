package com.charlesmass.justpopit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

/**
 * Created by charlesmass on 06/03/2015.
 */
public class Accueil extends Activity {
    static final public int DIFF_EASY = 1, DIFF_REGULAR = 2, DIFF_HARD = 3, DIFF_NIGHTMARE = 4;


    private TextView tvDifficulty = null, tvScores = null, tvScoresRush = null;
    private int diff = 1, highScore, highScoreRush;

    static public Context accueil_context;

    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//On cache la barre du haut



        //Code for banner view
        mAdView = (AdView) findViewById(R.id.ban);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        accueil_context = getApplicationContext();

        Display display = getWindowManager().getDefaultDisplay();//On obtient l'ecran via le gestionnaire de fenêtre
        Point size = new Point();
        display.getSize(size);//Taille de l'ecran
        int largeurEcran = size.x;
        int hauteurEcran = size.y;

        TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvTitle.setTextSize((float) largeurEcran / 15);//Titre un peu plus gros en finction de l'ecran
        tvTitle.setY((float) 0.05 * hauteurEcran);

        tvScores = (TextView)findViewById(R.id.tvScores);
        tvScores.setY((float) 0.75 * hauteurEcran);
        updateTvScores();

        tvScoresRush = (TextView)findViewById(R.id.tvScoresRush);
        tvScoresRush.setY((float) 0.81 * hauteurEcran);
        updateTvScoresRush();

        ImageButton bNew = (ImageButton) findViewById(R.id.bNew);
        bNew.setY((float) 0.25 * hauteurEcran);
        bNew.setX((float) 0.05 * largeurEcran);
        bNew.setLayoutParams(new RelativeLayout.LayoutParams(largeurEcran * 4 / 10, largeurEcran * 52 / 100));
        bNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Accueil.this, Classic.class);
                i.putExtra(getString(R.string.difficulty_key), diff);//On envoie la valeur de diff pour le nouveau jeu
                i.putExtra(getString(R.string.high_score_key), highScore);
                startActivity(i);//Lancement du jeu
            }
        });

        ImageButton bRush = (ImageButton)findViewById(R.id.bRush);
        bRush.setY((float) 0.25 * hauteurEcran);
        bRush.setX((float) 0.5 * largeurEcran);
        bRush.setLayoutParams(new RelativeLayout.LayoutParams(largeurEcran * 4 / 10, largeurEcran * 52 / 100));
        bRush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Accueil.this, Rush.class);
                i.putExtra(getString(R.string.difficulty_key), diff);//On envoie la valeur de diff pour le nouveau jeu
                i.putExtra(getString(R.string.high_score_key), highScoreRush);
                startActivity(i);//Lancement du rush
            }
        });

        tvDifficulty = (TextView)findViewById(R.id.tvDifficulty);
        tvDifficulty.setY((float) 0.6 * hauteurEcran);
        tvDifficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (diff) {
                    case DIFF_EASY://Easy -> Regular
                        tvDifficulty.setText(getString(R.string.tv_regular));
                        diff++;
                        updateTvScores();
                        updateTvScoresRush();
                        break;
                    case DIFF_REGULAR://Regular -> Hard
                        tvDifficulty.setText(getString(R.string.tv_hard));
                        diff++;
                        updateTvScores();
                        updateTvScoresRush();
                        break;
                    case DIFF_HARD://Hard -> Nightmare
                        tvDifficulty.setText(getString(R.string.tv_nightmare));
                        diff++;
                        updateTvScores();
                        updateTvScoresRush();
                        break;
                    case DIFF_NIGHTMARE://Nightmare -> Easy
                        tvDifficulty.setText(getString(R.string.tv_easy));
                        diff = 1;
                        updateTvScores();
                        updateTvScoresRush();
                        break;
                }
            }
        });

        Button bInfo = (Button) findViewById(R.id.bInfo);
        //bInfo.setLayoutParams(new RelativeLayout.LayoutParams(largeurEcran/10, largeurEcran/15));
        bInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lvInfo = new ListView(Accueil.this);

                ArrayList<String> listInfo = new ArrayList<>();
                listInfo.add(0, getString(R.string.infos_appli_title));
                listInfo.add(1, getString(R.string.infos_rules_title));

                ArrayAdapter<String> adap = new ArrayAdapter<>(Accueil.this, android.R.layout.simple_list_item_1, listInfo);//On crée un adaptateur à partir de l'arraylist
                lvInfo.setAdapter(adap);//L'adaptateur permet de concilier la listview avec l'arraylist
                lvInfo.setBackgroundColor(0x0050cc);//Bleu un peu plus foncé

                lvInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {//Réagit en fonction d el'item sélectionné
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (id == 0) {//Premier item sélectionné dans la listview
                            final TextView tvInfos = (TextView)findViewById(R.id.tvInfos);

                            final RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlMain);//On chope le papa
                            final LinearLayout ll = new LinearLayout(Accueil.this);

                            rl.removeView(tvInfos);//On lui donne un nouveau parent donc faut que le parent d'avant la tej
                            ll.addView(tvInfos);//On la met dans un nouveau layout pour etre capable de la virer de ce layout apres

                            tvInfos.setVisibility(View.VISIBLE);//Elle etait invisible avant
                            tvInfos.setMovementMethod(new ScrollingMovementMethod());//On rend la TextView scrollable
                            tvInfos.setPadding(10, 10, 10, 10);//On éloigne le texte des bords
                            tvInfos.setAutoLinkMask(Linkify.WEB_URLS);

                            AlertDialog.Builder adb = new AlertDialog.Builder(Accueil.this);
                            adb.setTitle(getString(R.string.infos_appli_title));
                            adb.setView(ll);//On met la textview dans l'alertdialog
                            adb.setCancelable(true);
                            adb.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {//Lorsque l'utilisateur vire la fenetre, on remet la textview dans le layout
                                    ll.removeAllViews();
                                    tvInfos.setVisibility(View.INVISIBLE);
                                    rl.addView(tvInfos);
                                }
                            });
                            adb.show();//Et bingo

                        } else if (id == 1) {//Deuxieme item sélectionné
                            AlertDialog.Builder adb = new AlertDialog.Builder(Accueil.this);
                            adb.setTitle(getString(R.string.infos_rules_title));//Titre

                            final TextView tvRules = (TextView)findViewById(R.id.tvInfoRules);//On prend la textview a laquelle on a deja appliqué le txt
                            final RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlMain);//On chope le papa
                            final LinearLayout ll = new LinearLayout(Accueil.this);

                            rl.removeView(tvRules);//On lui donne un nouveau parent donc faut que le parent d'avant la tej
                            ll.addView(tvRules);//On la met dans un nouveau layout pour etre capable de la virer de ce layout apres

                            tvRules.setVisibility(View.VISIBLE);//Elle etait invisible avant
                            tvRules.setMovementMethod(new ScrollingMovementMethod());//On rend la TextView scrollable
                            tvRules.setPadding(10, 10, 10, 10);//On éloigne le texte des bords

                            adb.setView(ll);//On met la textview dans l'alertdialog
                            adb.setCancelable(true);
                            adb.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {//Lorsque l'utilisateur vire la fenetre, on remet la textview dans le layout
                                    ll.removeAllViews();
                                    tvRules.setVisibility(View.INVISIBLE);
                                    rl.addView(tvRules);
                                }
                            });
                            adb.show();//Et bingo
                        }
                    }
                });
                AlertDialog.Builder adb = new AlertDialog.Builder(Accueil.this);
                adb.setTitle("Informations");
                adb.setView(lvInfo);//On met la listview dans la boite de dialogue
                adb.setCancelable(true);
                adb.show();
            }
        });
    }

    @Override
    protected void onStart(){//Dès qu'on revient sur l'activité, on actualise le textView de scores
        super.onStart();
        updateTvScores();
        updateTvScoresRush();

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void updateTvScores(){
        highScore = writeScore();
        tvScores.setText("High score : " + highScore + " pops");
    }

    public void updateTvScoresRush(){
        highScoreRush = writeScoreRush();
        tvScoresRush.setText("High score (rush mode) : " + highScoreRush + " pops");
    }

    private int writeScore(){//Recuperation des scores dans les ressources partagées
        int max = 0;
        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(this);//On entre dans les ressources partagées

        switch (diff){
            case DIFF_EASY:
                max = sharedP.getInt(getString(R.string.shared_scores_easy), 0);//clé easy
                break;
            case DIFF_REGULAR:
                max = sharedP.getInt(getString(R.string.shared_scores_regular), 0);//clé regular
                break;
            case DIFF_HARD:
                max = sharedP.getInt(getString(R.string.shared_scores_hard), 0);//clé hard
                break;
            case DIFF_NIGHTMARE:
                max = sharedP.getInt(getString(R.string.shared_scores_nightmare), 0);//clé nightmare
                break;
        }
        highScore = max;
        return max;
    }

    private int writeScoreRush(){
        int max = 0;
        SharedPreferences sharedP = PreferenceManager.getDefaultSharedPreferences(this);//On entre dans les ressources partagées

        switch (diff){
            case DIFF_EASY:
                max = sharedP.getInt(getString(R.string.shared_scores_easy_rush), 0);//clé easy rush
                break;
            case DIFF_REGULAR:
                max = sharedP.getInt(getString(R.string.shared_scores_regular_rush), 0);//clé regular rush
                break;
            case DIFF_HARD:
                max = sharedP.getInt(getString(R.string.shared_scores_hard_rush), 0);//clé hard rush
                break;
            case DIFF_NIGHTMARE:
                max = sharedP.getInt(getString(R.string.shared_scores_nightmare_rush), 0);//clé nightmare rush
                break;
        }
        highScoreRush = max;
        return max;
    }

}
