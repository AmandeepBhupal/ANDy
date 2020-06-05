package com.andy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

public class DocumentLinkAdapter extends RecyclerView.Adapter<DocumentLinkAdapter.ViewHolder> {

    private ArrayList<DocumentLink> list;
    private Context context;

    DocumentLinkAdapter(ArrayList<DocumentLink> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.document_link_card_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        viewHolder.documentName.setText(list.get(i).getDocumentName());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(list.get(i).getDocumentLink());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("URI", uri.toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String path = uri.getPath();

                if (!path.isEmpty() && path.contains("PDF") || path.contains("pdf")) {
                    intent.setDataAndType(uri, "application/pdf");
                } else {
                    intent.setClass(context.getApplicationContext(), FeedsWebViewActivity.class);
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView documentName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            documentName = itemView.findViewById(R.id.documentName);
        }
    }
}
