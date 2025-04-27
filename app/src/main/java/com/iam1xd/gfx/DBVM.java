package com.iam1xd.gfx;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DBVM extends ViewModel {
    private final MutableLiveData<List<items>> liveData = new MutableLiveData<>();
    public final ItemRepo itemRepo = new ItemRepo();

    public LiveData<List<items>> getLinks() {
        return liveData;
    }
    public void loadLinks() {
        itemRepo.fetchLinks(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<items> links = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String key = snap.getKey();
                    String value = snap.getValue(String.class);
                    links.add(new items(key, value));
                }
                liveData.setValue(links);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "onCancelled: ");
            }
        });
    }
}
