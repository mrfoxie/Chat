package tk.hackeridiot.chat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdptor extends RecyclerView.Adapter<MessageAdptor.MessageViewHolder> {
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    public MessageAdptor (List<Messages> userMessageList){
        this.userMessageList = userMessageList;
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;
        public MessageViewHolder(View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.send_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receive_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_send_image_view);
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageViewHolder, int i) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(i);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiveImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiveImage).placeholder(R.drawable.logo_app).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        if (fromMessageType.equals("text")){

            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            }
            else {
                messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
