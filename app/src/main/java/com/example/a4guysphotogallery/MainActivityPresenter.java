package com.example.a4guysphotogallery;

public class MainActivityPresenter {

    private MainActivityView view;
    private MainActivityInteractor interactor;

    public MainActivityPresenter(MainActivityInteractor interactor) {
        this.interactor = interactor;
    }

    public void bind(MainActivityView view) {
        this.view = view;
    }

    public void unbind() {
        view = null;
    }
}
