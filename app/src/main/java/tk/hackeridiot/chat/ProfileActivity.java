package tk.hackeridiot.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String reciveUserID, sendUserID, Current_state;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button senMessageRequestButton;
    private DatabaseReference UserRef, ChatRequestRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        reciveUserID = getIntent().getExtras().get("visit_user_if").toString();
        sendUserID = mAuth.getCurrentUser().getUid();
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_profile_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        senMessageRequestButton = (Button) findViewById(R.id.sen_message_request_button);
        Current_state = "new";
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(reciveUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.logo_app).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }
                else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {
        ChatRequestRef.child(sendUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(reciveUserID)){
                            String request_type = dataSnapshot.child(reciveUserID).child("request_type").getValue().toString();
                            if (request_type.equals("sent")){
                                Current_state = "request_sent";
                                senMessageRequestButton.setText("Cancle Chat Request");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        if (!sendUserID.equals(reciveUserID)) {
            senMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    senMessageRequestButton.setEnabled(false);
                    if (Current_state.equals("new")){
                        SendChatRequest();
                    }
                }
            });
        }
        else {
            senMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void SendChatRequest() {
        ChatRequestRef.child(sendUserID).child(reciveUserID)
                .child("request_type").setValue("send")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ChatRequestRef.child(reciveUserID).child(sendUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                senMessageRequestButton.setEnabled(true);
                                                Current_state = "request_sent";
                                                senMessageRequestButton.setText("Cancle Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
