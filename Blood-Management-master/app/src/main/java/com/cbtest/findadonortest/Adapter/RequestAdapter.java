package com.cbtest.findadonortest.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cbtest.findadonortest.Model.Request;
import com.cbtest.findadonortest.Model.User;
import com.cbtest.findadonortest.R;
import com.cbtest.findadonortest.ViewRequestActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder>{

    private Context context;
    private List<Request> requestList;

    public RequestAdapter(Context context, List<Request> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.requesr_display_layout, parent,false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Request request = requestList.get(position);


        DatabaseReference senderReference = FirebaseDatabase.getInstance().getReference().child("users").child(request.getSenderId());
        senderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    holder.senderName.setText("Sent By: "+snapshot.child("name").getValue().toString());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference receiverReference = FirebaseDatabase.getInstance().getReference().child("users").child(request.getReceiverId());
        receiverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.receiverName.setText("Received by: "+snapshot.child("name").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tittle.setText("Subject: "+request.getTittle());
        holder.description.setText("Status: "+request.getSate());
        holder.timeStamp.setText("Time: "+request.getTimeStamp());
        holder.viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewRequestActivity.class);
                intent.putExtra("requestId",request.getRequestID());
                context.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() { return requestList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView senderName,receiverName,tittle,description,timeStamp;
        public Button viewDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            senderName = itemView.findViewById(R.id.senderName);
            receiverName = itemView.findViewById(R.id.receiverName);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            tittle = itemView.findViewById(R.id.tittle);
            description = itemView.findViewById(R.id.description);
            viewDetails = itemView.findViewById(R.id.viewDetails);

        }
    }


}
