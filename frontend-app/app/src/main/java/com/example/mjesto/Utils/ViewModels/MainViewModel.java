package com.example.mjesto.Utils.ViewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v4.app.Fragment;

public class MainViewModel extends ViewModel {

    public final String TAG = MainViewModel.class.getSimpleName();

    public MutableLiveData<Fragment> vm_fragment;
    public MutableLiveData<Fragment> vm_fragment_no_backstack;
    private String fragmentTag;

    public MutableLiveData<Fragment> getFragment() {
        if (vm_fragment == null) {
            vm_fragment = new MutableLiveData<>();
            vm_fragment.setValue(null);
            fragmentTag = new String();
        }
        return vm_fragment;
    }

    public MutableLiveData<Fragment> getFragmentNoBackstack() {
        if (vm_fragment_no_backstack == null) {
            vm_fragment_no_backstack = new MutableLiveData<>();
            vm_fragment_no_backstack.setValue(null);
            fragmentTag = new String();
        }
        return vm_fragment_no_backstack;
    }

    public void setFragment(Fragment fragment, String tag) {
        if (tag.isEmpty()) {
            fragmentTag = tag;
        } else if (fragmentTag.equals(tag)) {
            return;
        }
        vm_fragment.setValue(fragment);
        fragmentTag = tag;
    }

    public void setFragmentNoBackstack(Fragment fragment) {
        vm_fragment_no_backstack.setValue(fragment);
    }

    public void setTAG(String tag) {
        fragmentTag = tag;
    }
}
