package com.example.petbridge.navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petbridge.firebase.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.petbridge.AddPublicationFragment;
import com.example.petbridge.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private ImageView addPub;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    FragmentManager fragmentManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseManager.getFirestoreInstance();
        recyclerView = view.findViewById(R.id.recyclerViewHome);

        addPub = view.findViewById(R.id.addpub);

        addPub.setOnClickListener(v -> {
            fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, AddPublicationFragment.class, null)
                    .setReorderingAllowed(true)
                    .commit();
        });

        fetchData();
        return view;
    }

    public void fetchData() {
        db.collection("publications")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    List<Publication> publicationList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Publication publication = document.toObject(Publication.class);
                        String userId = publication.getUserId();
                        db.collection("Users")
                                .document(userId)
                                .addSnapshotListener((documentSnapshot, e1) -> {
                                    if (e1 != null) {
                                        return;
                                    }
                                    if (documentSnapshot.exists()) {
                                        String nome = documentSnapshot.getString("Name");
                                        String cognome = documentSnapshot.getString("LastName");
                                        String profileImage = documentSnapshot.getString("Image");
                                        publication.setNome(nome + " " + cognome);
                                        publication.setProfileImage(profileImage);
                                    }
                                    publicationList.add(publication);
                                    updateView(publicationList, publication.getUserId());
                                });
                    }
                });
    }
    private void updateView(List<Publication> publicationList , String reciverId) {
        String senderId = FirebaseAuth.getInstance().getUid();
        PublicationAdapter publicationAdapter = new PublicationAdapter(publicationList.toArray(new Publication[0]), senderId, reciverId);
        recyclerView.setAdapter(publicationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        publicationAdapter.notifyDataSetChanged();
    }
}