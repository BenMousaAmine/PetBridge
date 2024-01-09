package com.example.petbridge.navigation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petbridge.R;
import com.example.petbridge.firebase.FirebaseManager;
import com.example.petbridge.messaging.Conversation;
import com.example.petbridge.messaging.Message;
import com.example.petbridge.messaging.MessageAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {
    private FirebaseFirestore db;
  //  private FirebaseFirestore db1;
    private RecyclerView recyclerView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
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
        View  view = inflater.inflate(R.layout.fragment_message, container, false);
        db = FirebaseManager.getFirestoreInstance();
        recyclerView = view.findViewById(R.id.mainUserRecyclerView);
        fetchData();

        return view ;
    }

    public void fetchData() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        List<Task<Void>> tasks = new ArrayList<>();

        db.collection("Conversations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot conversationDoc : task.getResult()) {
                            String user1 = conversationDoc.getString("user1");
                            String user2 = conversationDoc.getString("user2");
                            if (user1.equals(currentUserId)) {
                                tasks.add(caricaUser(user2));
                            } else if (user2.equals(currentUserId)) {
                                tasks.add(caricaUser(user1));
                            }
                        }

                        Tasks.whenAllSuccess(tasks).addOnSuccessListener(
                                unused -> updateView(messageList, userIdList)
                        );
                    }
                });
    }

    public Task<Void> caricaUser(String userId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        Message message = new Message();

        db.collection("Users")
                .document(userId)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        DocumentSnapshot documentUser = userTask.getResult();
                        if (documentUser != null && documentUser.exists()) {
                            String nome = documentUser.getString("Name");
                            String cognome = documentUser.getString("LastName");
                            String profileImage = documentUser.getString("Image");
                            message.setReciverFullName(nome + " " + cognome);
                            message.setReciverProfileImage(profileImage);
                            message.setReciverId(userId);
                        }
                        messageList.add(message);
                        userIdList.add(userId);
                        taskCompletionSource.setResult(null);
                    } else {
                        taskCompletionSource.setException(userTask.getException());
                    }
                });

        return taskCompletionSource.getTask();
    }

    private List<Message> messageList = new ArrayList<>();
    private List<String> userIdList = new ArrayList<>();


    public void updateView(List<Message> messageList, List<String> userIdList) {
        String senderId = FirebaseAuth.getInstance().getUid();
        MessageAdapter messageAdapter = new MessageAdapter(messageList.toArray(new Message[0]), senderId, userIdList.get(userIdList.size()-1), requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }




}