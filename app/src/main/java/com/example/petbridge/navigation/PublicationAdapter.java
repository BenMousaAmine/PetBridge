package com.example.petbridge.navigation;
import static com.example.petbridge.messaging.FCMNotification.sendNotification;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;



import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petbridge.R;
import com.example.petbridge.firebase.FirebaseManager;
import com.example.petbridge.messaging.Conversation;
import com.example.petbridge.messaging.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.PublicationViewHolder> {
    private final Publication[] publications;
    private OnItemClickListener onItemClickListener;

    private final String senderId;
    private final String reciverId;

    public PublicationAdapter(Publication[] publications, String senderId, String reciverId) {
        this.publications = publications;
        this.senderId = senderId;
        this.reciverId = reciverId;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public PublicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_home, parent, false);
        return new PublicationViewHolder(view, senderId, reciverId);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicationViewHolder holder, int position) {
        holder.bind(publications[position]);
    }

    @Override
    public int getItemCount() {
        return publications.length;
    }


    static class PublicationViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView publicationText;
        private final ImageView icon;
        private final ImageView imagepub;
        private Button contact;
        private final String senderId;
        private final String reciverId;

        public PublicationViewHolder(@NonNull View itemView, String senderId, String reciverId) {
            super(itemView);
            name = itemView.findViewById(R.id.text_view_profileName);
            publicationText = itemView.findViewById(R.id.text_view_project_publication);
            icon = itemView.findViewById(R.id.image_profile);
            imagepub = itemView.findViewById(R.id.image_publication);
            contact = itemView.findViewById(R.id.contact);
            this.senderId = senderId;
            this.reciverId = reciverId;


        }

        public void bind(Publication publication) {
            name.setText(publication.getNome());
            publicationText.setText(publication.getPubText());
            Glide.with(itemView.getContext())
                    .load(Uri.parse(publication.getProfileImage()))
                    .into(icon);
            Glide.with(itemView.getContext())
                    .load(Uri.parse(publication.getPubImage()))
                    .into(imagepub);
            contact.setOnClickListener(view -> {
                writeMessage(view.getContext(), getAdapterPosition() , publication.getUserId());
            });
        }

        public void writeMessage(Context context, int position ,String reciverId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Invia un messaggio");
            final EditText msg = new EditText(context);
            msg.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(msg);

            builder.setPositiveButton("Send", ((dialog, which) -> {
                String userMsg = msg.getText().toString();
                sendMessage(senderId, reciverId, userMsg);
                Toast.makeText(context, "Message Send Success", Toast.LENGTH_SHORT).show();
            }));
            builder.setNegativeButton("Cancel", ((dialog, which) -> {
                dialog.dismiss();
            }));
            builder.create().show();


        }

        public void sendMessage(String senderId, String receiverId, String messageText) {
            FirebaseFirestore db = FirebaseManager.getFirestoreInstance();


            String conversationId = generateConversationId(FirebaseAuth.getInstance().getUid(), receiverId);
            CollectionReference conversationsCollection = db.collection("Conversations");
            DocumentReference conversationDocument = conversationsCollection.document(conversationId);
            // Ottenere il riferimento alla raccolta "messages" all'interno del documento della conversazione
            CollectionReference messagesCollection = conversationDocument.collection("messages");
            // Creare un nuovo messaggio
            Message newMessage = new Message(senderId, receiverId, messageText, new Date());
            // Controllare se la conversazione esiste già
            conversationDocument.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // La conversazione esiste già, aggiungi solo il messaggio
                    addMessageToCollection(messagesCollection, newMessage);
                    sendNotificationToUser(receiverId, messageText);
                } else {
                    // La conversazione non esiste, creala e aggiungi user1 e user2
                    Conversation newConversation = new Conversation(senderId, receiverId);
                    conversationsCollection.document(conversationId).set(newConversation)
                            .addOnSuccessListener(aVoid -> {
                                // Aggiungi il messaggio e invia la notifica
                                addMessageToCollection(messagesCollection, newMessage);
                                sendNotificationToUser(receiverId, messageText);
                            })
                            .addOnFailureListener(e -> {
                                // Gestire eventuali errori durante la creazione della conversazione
                            });
                }
            });
        }
        private void sendNotificationToUser(String userId, String messageText) {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fcmToken = documentSnapshot.getString("fcmToken");
                            if (fcmToken != null) {
                                Context context = itemView.getContext();
                                sendNotification(context,fcmToken, messageText, "Nuovo messaggio");
                            }
                        }
                    });
        }
        private void addMessageToCollection(CollectionReference messagesCollection, Message newMessage) {
            messagesCollection.add(newMessage)
                    .addOnSuccessListener(documentReference -> {
                        // Il messaggio è stato aggiunto con successo alla raccolta "messages"
                        // Puoi anche gestire altri aggiornamenti o azioni qui se necessario
                    })
                    .addOnFailureListener(e -> {
                        // Gestire eventuali errori durante l'aggiunta del messaggio
                    });
        }

        private String generateConversationId(String uid1, String uid2) {
            // Ordina gli UID in modo da ottenere una sequenza univoca per la conversazione
            List<String> sortedUids = new ArrayList<>(Arrays.asList(uid1, uid2));
            Collections.sort(sortedUids);

            // Unisce gli UID per formare l'ID della conversazione
            return sortedUids.get(0) + "_" + sortedUids.get(1);
        }
    }
}