package com.example.mjesto;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.Serializable;

public class MainViewModel extends ViewModel {

    public MutableLiveData<Fragment> vm_fragment;

    public MutableLiveData<Fragment> getFragment() {
        if (vm_fragment == null) {
            vm_fragment = new MutableLiveData<>();
            vm_fragment.setValue(null);
        }
        return vm_fragment;
    }

    public void setFragment(Fragment vm_fragment) {
        this.vm_fragment.setValue(vm_fragment);
    }
}
