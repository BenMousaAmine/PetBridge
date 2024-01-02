package com.example.petbridge.navigation;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petbridge.R;

import org.w3c.dom.Text;

public class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.PublicationViewHolder> {
    private final Publication [] publications ;

    public PublicationAdapter(Publication[] publications) {
        this.publications = publications;
    }

    @NonNull
    @Override
    public PublicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_home , parent , false);
        return new PublicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicationViewHolder holder, int position) {
        holder.bind(publications[position]);
    }

    @Override
    public int getItemCount() {
        return publications.length;
    }


    static class  PublicationViewHolder extends RecyclerView.ViewHolder{
     private final TextView name ;
        private final TextView publicationText ;
     private final ImageView icon ;
   private final ImageView imagepub ;



        public PublicationViewHolder(@NonNull View itemView) {
            super(itemView);
        name = itemView.findViewById(R.id.text_view_profileName);
            publicationText = itemView.findViewById(R.id.text_view_project_publication);
        icon = itemView.findViewById(R.id.image_profile);
       imagepub = itemView.findViewById(R.id.image_publication);

        }
        public void bind (Publication publication){
     name.setText(publication.getNome());
            publicationText.setText(publication.getPubText());
            Glide.with(itemView.getContext())
                            .load(Uri.parse(publication.getProfileImage()))
                                    .into(icon);
           Glide.with(itemView.getContext())
                   .load(Uri.parse(publication.getPubImage()))
                   .into(imagepub);

        }
    }
}
