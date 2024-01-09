package com.example.petbridge.firebase;


import com.google.firebase.firestore.FirebaseFirestore;
// singelton per fare un sola istanziare una volta firestore
public class FirebaseManager {
    private static FirebaseFirestore firebaseFirestore;

    private FirebaseManager() {
    }

    public static FirebaseFirestore getFirestoreInstance() {
        if (firebaseFirestore == null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
        }
        return firebaseFirestore;
    }
}
