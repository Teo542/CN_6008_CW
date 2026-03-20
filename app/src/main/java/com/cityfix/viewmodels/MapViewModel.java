package com.cityfix.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cityfix.models.FaultReport;
import com.cityfix.repositories.ReportRepository;

import java.util.List;

public class MapViewModel extends ViewModel {

    private final ReportRepository reportRepository;
    public final MutableLiveData<List<FaultReport>> reports = new MutableLiveData<>();

    public MapViewModel() {
        reportRepository = new ReportRepository();
        // Start listening as soon as ViewModel is created
        reportRepository.listenToReports(reports);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Stop Firestore listener when fragment is destroyed — prevents memory leaks
        reportRepository.removeListener();
    }
}
