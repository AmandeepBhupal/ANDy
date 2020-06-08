package com.andy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class introActivity extends AppCompatActivity {
    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private MaterialButton buttonOnboardingAction;
    private MaterialButton skipButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        layoutOnboardingIndicators=findViewById(R.id.layoutOnboardingIndicators);
        buttonOnboardingAction=findViewById(R.id.buttonOnboardingAction);
        skipButton=findViewById(R.id.buttonOnboardingSkip);
        setupOnboardoingItems();
        final ViewPager2 onboardingViewPager=findViewById(R.id.onboardingViewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);
        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);
        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });
        buttonOnboardingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                    onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
                }

                else{
                    Intent homeIntent = new Intent(introActivity.this, LoginActivity.class);
                    startActivity(homeIntent);
                    finish();
               }

            }
        });
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth=FirebaseAuth.getInstance();
                FirebaseUser mUser=mAuth.getCurrentUser();
                if(mUser!=null){
                    Intent homeIntent=new Intent(introActivity.this,MainActivity.class);
                    startActivity(homeIntent);
                    finish();
                }
                else{
                    Intent homeIntent=new Intent(introActivity.this,LoginActivity.class);
                    startActivity(homeIntent);
                    finish();
                }
            }
        });

    }
    private void setupOnboardoingItems(){
        List<OnboardingItem> onboardingItems=new ArrayList<>();
        OnboardingItem itemSubscribeButton=new OnboardingItem();
        itemSubscribeButton.setTitle("Love reading about a particular topic?");
        itemSubscribeButton.setDescription(" Click on the Subscribe button on the tags you wish to subscribe to!");
        itemSubscribeButton.setImage(R.drawable.subscribe_button);

        OnboardingItem itemNotification=new OnboardingItem();
        itemNotification.setTitle("Get latest notifications");
        itemNotification.setDescription("Get notified whenever a document is added to the tag you're subscribed to or when someone likes/dislikes something you uploaded!");
        itemNotification.setImage(R.drawable.notification_alert);

        OnboardingItem itemFeedsPage=new OnboardingItem();
        itemFeedsPage.setTitle("Get your daily dose of Feeds!");
        itemFeedsPage.setDescription("Go to the Feeds page to get the latest documents uploaded of the tags you're subscribed to!");
        itemFeedsPage.setImage(R.drawable.feeds_page);

        OnboardingItem createContent=new OnboardingItem();
        createContent.setTitle("Share your documents!");
        createContent.setDescription("Upload content whenever you wish to share something with the ANDy community!");
        createContent.setImage(R.drawable.upload_content);

        OnboardingItem searchBar=new OnboardingItem();
        searchBar.setTitle("Search what you love!");
        searchBar.setDescription("Go to the Search page to look up a tag you wish to view the documents of!");
        searchBar.setImage(R.drawable.search_bar);

        OnboardingItem itemProfile=new OnboardingItem();
        itemProfile.setTitle("Delete the documents you no longer need!");
        itemProfile.setDescription("From the profile page, delete what you no loner wish to share with the ANDy community!");
        itemProfile.setImage(R.drawable.profile_page);


        onboardingItems.add(itemSubscribeButton);
        onboardingItems.add(itemNotification);
        onboardingItems.add(itemFeedsPage);
        onboardingItems.add(createContent);
        onboardingItems.add(searchBar);
        onboardingItems.add(itemProfile);

        onboardingAdapter=new OnboardingAdapter(onboardingItems);
    }
    private void setupOnboardingIndicators(){
        ImageView[] indicators=new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8,0,8,0);
        for(int i=0; i<indicators.length; i++){
            indicators[i]=new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }

    }
    private void setCurrentOnboardingIndicator(int index){
        int childCount=layoutOnboardingIndicators.getChildCount();
        for(int i=0;i<childCount;i++){
            ImageView imageView=(ImageView)layoutOnboardingIndicators.getChildAt(i);
            if(i==index){
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.onboarding_indicator_active)
                );
            }
            else{
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.onboarding_indicator_inactive)
                );
            }
        }
        if (index==onboardingAdapter.getItemCount()-1){
            buttonOnboardingAction.setText("Get Started");

        }else{
            buttonOnboardingAction.setText("Next");
        }
    }
}
