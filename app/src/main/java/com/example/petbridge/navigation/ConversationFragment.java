package com.example.petbridge.navigation;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.petbridge.R;
import com.example.petbridge.firebase.FirebaseManager;
import com.example.petbridge.messaging.Conversation;
import com.example.petbridge.messaging.ConversationAdapter;
import com.example.petbridge.messaging.Message;
import com.example.petbridge.messaging.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
public class ConversationFragment extends Fragment {
    private RecyclerView recyclerView;
    private ConversationAdapter conversationAdapter;
    private List<Message> messageList;
    private String currentUserId;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView name ;
    private  FirebaseFirestore db ;
    private String senderId;
    private String receiverId;
    private String fullName ;

    EditText msgText ;
    CardView send ;

    public ConversationFragment (){

    }
    public static ConversationFragment newInstance(String param1, String param2) {
        ConversationFragment fragment = new ConversationFragment();
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
            senderId = getArguments().getString("senderId");
            receiverId = getArguments().getString("receiverId");
            fullName = getArguments().getString("fullName");
        }
        db = FirebaseManager.getFirestoreInstance();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversation, container, false);
        recyclerView = view.findViewById(R.id.layout_conversation);

        name=view.findViewById(R.id.recivername);
        name.setText(fullName);
        send = view.findViewById(R.id.sendbtn);
        msgText = view.findViewById(R.id.textmsg);

        send.setOnClickListener(v -> {
            String msg = msgText.getText().toString()  ;
            msgText.setText("");
            msgText.setHint("Type here your message");
            sendMessage(msg);
        });

        messageList = new ArrayList<>();
        currentUserId = FirebaseAuth.getInstance().getUid();

        conversationAdapter = new ConversationAdapter(messageList, currentUserId );
        recyclerView.setAdapter(conversationAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        loadMessagesFromFirebase();
        return view;
    }
    private void loadMessagesFromFirebase() {
        String converId = generateConversationId(senderId, receiverId);
        db.collection("Conversations")
                .document(converId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    messageList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        messageList.add(message);
                    }

                    conversationAdapter.notifyDataSetChanged();
                });
    }


    private void sendMessage(String messageText) {
        String converId = generateConversationId(senderId, receiverId);
        CollectionReference conversationsCollection = db.collection("Conversations");
        DocumentReference conversationDocument = conversationsCollection.document(converId);
        CollectionReference messagesCollection = conversationDocument.collection("messages");
        Message newMessage = new Message(senderId, receiverId, messageText, new Date());
        messageList.add(newMessage);
        conversationAdapter.notifyItemInserted(messageList.size() - 1);
        addMessageToCollection(messagesCollection, newMessage);
    }

    private void addMessageToCollection(CollectionReference messagesCollection, Message newMessage) {
        messagesCollection.add(newMessage)
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(e -> {

                });
    }

    private String generateConversationId(String uid1, String uid2) {
        List<String> sortedUids = new ArrayList<>(Arrays.asList(uid1, uid2));
        Collections.sort(sortedUids);
        return sortedUids.get(0) + "_" + sortedUids.get(1);
    }
}