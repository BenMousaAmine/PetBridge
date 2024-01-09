
package com.example.petbridge.messaging;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.petbridge.R;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUserId;




    public ConversationAdapter(List<Message> messageList, String currentUserId ) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_conversation, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
       holder.bind(messageList.get(position) );

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView leftMessage;
        TextView rightMessage;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            leftMessage = itemView.findViewById(R.id.leftMessageLayout);
            rightMessage = itemView.findViewById(R.id.rightMessageLayout);
        }

        void bind (Message message) {
           String currentUserId = FirebaseAuth.getInstance().getUid();
            if (message.getSenderId().equals(currentUserId)) {
                leftMessage.setVisibility(View.GONE);
                rightMessage.setText(message.getTextMsg());
            } else {
                rightMessage.setVisibility(View.GONE);
                leftMessage.setText(message.getTextMsg());
            }
        }
        }
    }

