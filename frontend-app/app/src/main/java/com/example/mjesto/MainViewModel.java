package com.example.mjesto;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.Serializable;

public class MainViewModel extends ViewModel {

    public final String TAG = MainViewModel.class.getSimpleName();

    public MutableLiveData<Fragment> vm_fragment;

    public MutableLiveData<Fragment> getFragment() {
        if (vm_fragment == null) {
            vm_fragment = new MutableLiveData<>();
            vm_fragment.setValue(null);
        }
        return vm_fragment;
    }

    public void setFragment(Fragment fragment) {
        if (!vm_fragment.equals(fragment) ) {
            vm_fragment.setValue(fragment);
        } else {
            Log.d(TAG, "Fragment already present");
        }
    }
}
