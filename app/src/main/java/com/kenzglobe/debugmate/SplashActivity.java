package com.kenzglobe.debugmate;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getActionBar() != null) getActionBar().hide();
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8B5CF6"));
        }

        // 1. Load your custom app fonts
        Typeface appFontBold = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Bold.ttf");
        Typeface appFontRegular = Typeface.createFromAsset(getAssets(), "fonts/SpaceMono-Regular.ttf");

        ImageView splashImage = findViewById(R.id.splash_image);
        TextView splashTitle = findViewById(R.id.splash_title);
        TextView splashSubtitle = findViewById(R.id.splash_subtitle);

        // Apply fonts
        splashTitle.setTypeface(appFontBold);
        splashSubtitle.setTypeface(appFontRegular);

        // 2. Logo Animation: Fade in and Scale up
        AnimationSet logoAnim = new AnimationSet(true);
        
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        
        ScaleAnimation scaleUp = new ScaleAnimation(
                0.5f, 1.0f, // Start X, End X
                0.5f, 1.0f, // Start Y, End Y
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(1000);

        logoAnim.addAnimation(fadeIn);
        logoAnim.addAnimation(scaleUp);
        splashImage.startAnimation(logoAnim);

        // 3. Title Animation: Slide up and Fade in (Delayed)
        AnimationSet titleAnim = new AnimationSet(true);
        titleAnim.setStartOffset(500); // Wait half a second before sliding up

        AlphaAnimation titleFade = new AlphaAnimation(0f, 1f);
        titleFade.setDuration(800);
        
        TranslateAnimation titleSlide = new TranslateAnimation(0, 0, 50f, 0);
        titleSlide.setDuration(800);

        titleAnim.addAnimation(titleFade);
        titleAnim.addAnimation(titleSlide);
        splashTitle.startAnimation(titleAnim);

        // 4. Subtitle Animation: Slide up and Fade in (Delayed more)
        AnimationSet subAnim = new AnimationSet(true);
        subAnim.setStartOffset(800); 

        AlphaAnimation subFade = new AlphaAnimation(0f, 1f);
        subFade.setDuration(800);
        
        TranslateAnimation subSlide = new TranslateAnimation(0, 0, 50f, 0);
        subSlide.setDuration(800);

        subAnim.addAnimation(subFade);
        subAnim.addAnimation(subSlide);
        splashSubtitle.startAnimation(subAnim);

        // 5. Launch MainActivity after 2.5 seconds (enough time for animations)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 2500);
    }
}