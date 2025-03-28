package com.example.cardgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HighLow extends Activity {
    public static final String PREFS_NAME = "HighLowPrefs";
    public static final String LONG_STREAK_PREF = "longestStreak";

    private HighLowGame mGame;
    private TextView mViewCardsLeft;
    private TextView mViewCurStreak;
    private TextView mViewLongStreak;
    private TextView mViewResult;
    private ImageView mViewCard;
    private ImageView mCardBack;
    private Context mAppContext;
    private Button mLowChoice;
    private Button mHighChoice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = this;
        this.getWindow().setBackgroundDrawableResource(R.drawable.green_back);

        // Initialize Views
        mViewCard = (ImageView) findViewById(R.id.card_image);
        mCardBack = (ImageView) findViewById(R.id.card_back);
        mViewCardsLeft = (TextView) findViewById(R.id.cards_left);
        mViewCurStreak = (TextView) findViewById(R.id.cur_streak);
        mViewLongStreak = (TextView) findViewById(R.id.long_streak);
        mViewResult = (TextView) findViewById(R.id.result_view);
        mLowChoice = (Button) findViewById(R.id.low_choice);
        mHighChoice = (Button) findViewById(R.id.high_choice);
        Button newGame = (Button) findViewById(R.id.new_game);

        // Set click listeners
        mLowChoice.setOnClickListener(mChoiceHandler);
        mHighChoice.setOnClickListener(mChoiceHandler);
        newGame.setOnClickListener(mNewGameHandler);

        // Get longest streak from preferences
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        int longestStreak = settings.getInt(LONG_STREAK_PREF, 0);

        startGame(longestStreak);
    }

    private OnClickListener mChoiceHandler = new OnClickListener() {
        public void onClick(View v) {
            mViewResult.setText(mGame.evaluateUserGuess(v.getId()));
            flipCard();
        }
    };

    private OnClickListener mNewGameHandler = new OnClickListener() {
        public void onClick(View v) {
            startGame(mGame.getLongStreak());
        }
    };

    private void flipCard() {
        // Disable buttons during animation
        mLowChoice.setEnabled(false);
        mHighChoice.setEnabled(false);

        // Load the animations
        Animation flipOut = AnimationUtils.loadAnimation(this, R.anim.flip_out);
        Animation flipIn = AnimationUtils.loadAnimation(this, R.anim.flip_in);

        flipOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // When top card finishes flipping out, hide it
                mViewCard.setVisibility(View.INVISIBLE);

                // Set the new card image on the back card
                mCardBack.setImageResource(mGame.topCard().getCardDrawableId(mAppContext));

                // Start flipping in the back card
                mCardBack.startAnimation(flipIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        flipIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mCardBack.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation complete - swap the cards
                mViewCard.setImageResource(mGame.topCard().getCardDrawableId(mAppContext));
                mViewCard.setVisibility(View.VISIBLE);
                mCardBack.setVisibility(View.INVISIBLE);
                mCardBack.setImageResource(R.drawable.card_back);

                // Update game state
                mGame.dealCard();
                updateStats();

                // Re-enable buttons
                mLowChoice.setEnabled(true);
                mHighChoice.setEnabled(true);

                if (!mGame.getGameIsActive()) {
                    endGame();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // Start the animation
        mViewCard.startAnimation(flipOut);
    }

    private void updateStats() {
        mViewCardsLeft.setText(Integer.toString(mGame.getCardsLeft()));
        mViewCurStreak.setText(Integer.toString(mGame.getCurrentStreak()));
        mViewLongStreak.setText(Integer.toString(mGame.getLongStreak()));
    }

    private void endGame() {
        mViewCardsLeft.setText("0");
        mViewResult.setText("Game Over");
        mViewCard.setImageResource(R.drawable.card_back);
        mLowChoice.setEnabled(false);
        mHighChoice.setEnabled(false);

        // Stop button animations
        mLowChoice.clearAnimation();
        mHighChoice.clearAnimation();

        Button newGame = (Button) findViewById(R.id.new_game);
        newGame.setVisibility(View.VISIBLE);
    }

    private void startGame(int longestStreak) {

        mGame = new HighLowGame(1, longestStreak);
        // Start button animations
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
        mLowChoice.startAnimation(pulse);
        mHighChoice.startAnimation(pulse);


        // Initialize cards
        mViewCard.setImageResource(mGame.topCard().getCardDrawableId(mAppContext));
        mCardBack.setImageResource(R.drawable.card_back);
        mViewCard.setVisibility(View.VISIBLE);
        mCardBack.setVisibility(View.VISIBLE);

        // Reset rotations
        mViewCard.setRotationY(0f);
        mCardBack.setRotationY(-90f);

        updateStats();
        mLowChoice.setEnabled(true);
        mHighChoice.setEnabled(true);

        Button newGame = (Button) findViewById(R.id.new_game);
        newGame.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LONG_STREAK_PREF, mGame.getLongStreak());
        editor.apply();
    }
}