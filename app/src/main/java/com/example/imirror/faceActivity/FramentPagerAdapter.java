package com.example.imirror.faceActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FramentPagerAdapter extends FragmentStateAdapter {
    public FramentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position){
            case 0:
                return new Fragment1Overview();
            case 1:
                return new Fragment2Diagnostic();
            default:
                return new Fragment3Suggestion();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }


}
