package com.example.petbridge.messaging;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petbridge.R;
import com.example.petbridge.navigation.ConversationFragment;
import com.example.petbridge.navigation.OnItemClickListener;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private final Message[] message ;
    private OnItemClickListener onItemClickListener ;
    private  final  String senderId ;
    private final String reciverId ;
   private final FragmentManager fragmentManager;
    public MessageAdapter(Message[] message, String senderId, String reciverId, FragmentManager fragmentManager) {
        this.message = message;
        this.senderId = senderId;
        this.reciverId = reciverId;
        this.fragmentManager = fragmentManager;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usermessage ,parent ,false);
        return new MessageViewHolder(view , senderId , reciverId);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(message[position]);
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("senderId", senderId);
            bundle.putString("receiverId",message[position].getReciverId());
            bundle.putString("fullName" , message[position].getReciverFullName());

            ConversationFragment conversationFragment = new ConversationFragment();
            conversationFragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, conversationFragment, null)
                    .setReorderingAllowed(true)
                    .commit();
        });
    }


    @Override
    public int getItemCount() {
        return message.length;
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener ;
    }


    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final ImageView profileImage;
        private final String senderId;
        private final String reciverId;


        public MessageViewHolder(@NonNull View itemView, String senderId, String reciverId) {
            super(itemView);
            name = itemView.findViewById(R.id.username) ;
            profileImage = itemView.findViewById(R.id.userimg);
            this.senderId = senderId;
            this.reciverId = reciverId;
        }

        public void bind (Message message){
            name.setText(message.getReciverFullName());
            Glide.with(itemView.getContext())
                    .load(Uri.parse(message.getReciverProfileImage()))
                    .into(profileImage);

        }

    }
}