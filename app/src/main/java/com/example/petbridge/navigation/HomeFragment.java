package com.example.petbridge.navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.petbridge.AddPublicationFragment;
import com.example.petbridge.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private ImageView addPub;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseFirestore db1;


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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        db1 = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewHome);
/*

        PublicationAdapter publicationAdapter = new PublicationAdapter(publication);
        recyclerView.setAdapter(publicationAdapter);*/

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
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Publication> publicationList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Publication publication = document.toObject(Publication.class);
                            String userId = publication.getUserId();
                            db1.collection("Users")
                                    .document(userId)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot documentUser = task1.getResult();
                                            if (documentUser.exists()) {
                                                String nome = documentUser.getString("Name");
                                                String cognome = documentUser.getString("LastName");
                                                String profileImage = documentUser.getString("Image");
                                                publication.setNome(nome + " " + cognome);
                                                publication.setProfileImage(profileImage);
                                            }
                                            publicationList.add(publication);
                                        }

                                        // Update the RecyclerView with the fetched data
                                        updateView(publicationList);

                                    });
                        }
                    }
                });
    }
    

    private void updateView(List<Publication> publicationList) {
        // Update your RecyclerView adapter with the fetched data
        PublicationAdapter publicationAdapter = new PublicationAdapter(publicationList.toArray(new Publication[0]));
        recyclerView.setAdapter(publicationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

}