package com.example.educher_child;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.example.educher_child.AppConfiguration.SUGGESTION;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SugestionViewHolder> {

    private List<SuggestModel> models;
    private Context context;
    private String parent_key,childiD;

    public SuggestionAdapter(List<SuggestModel> models,String parent_key,String childiD, Context context) {
        this.models = models;
        this.context = context;
        this.parent_key = parent_key;
        this.childiD = childiD;
    }

    @NonNull
    @Override
    public SugestionViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SugestionViewHolder(LayoutInflater.from(context).inflate(R.layout.suggestion_item,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SugestionViewHolder sugestionViewHolder, final int i) {
        final SuggestModel suggestModel = models.get(i);
        sugestionViewHolder.app.setText(suggestModel.getName());
        VersionChecker vc = new VersionChecker();
        try {
            String path = vc.execute(models.get(i).getLink()).get();
            Glide.with(context).load(path).into(sugestionViewHolder.appIcon);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sugestionViewHolder.install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donwload(suggestModel.getLink());
            }
        });


    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class SugestionViewHolder extends RecyclerView.ViewHolder {

        private TextView app,install,cancel;
        ImageView appIcon;
        private ConstraintLayout constraintLayout;

        public SugestionViewHolder(@NonNull View itemView) {
            super(itemView);

            app = itemView.findViewById(R.id.textView20);
            appIcon = itemView.findViewById(R.id.imageView3);
            install = itemView.findViewById(R.id.install);
            constraintLayout = itemView.findViewById(R.id.cons);
        }
    }

    private void donwload(String uri){
        try {
            Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        }
    }
}
