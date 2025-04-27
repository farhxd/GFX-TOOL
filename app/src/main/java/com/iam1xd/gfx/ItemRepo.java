package com.iam1xd.gfx;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ItemRepo {
    private DatabaseReference linksRef;
    public ItemRepo() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        linksRef = database.getReference("links");
    }

    public void fetchLinks(ValueEventListener valueEventListener) {
        linksRef.addListenerForSingleValueEvent(valueEventListener);
    }
}
