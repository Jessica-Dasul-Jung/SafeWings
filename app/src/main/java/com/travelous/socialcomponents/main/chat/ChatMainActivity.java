package com.travelous.socialcomponents.main.chat;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.travelous.socialcomponents.R;



public class ChatMainActivity extends AppCompatActivity {

    private FirebaseListAdapter<ChatMessage> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                "me")
                        );

                // Clear the input
                input.setText("");
            }
        });

        displayChatMessage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        displayChatMessage();
    }

    public void displayChatMessage() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        //;
        //        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
        //                R.layout.message, FirebaseDatabase.getInstance().getReference())

        Query query = FirebaseDatabase.getInstance().getReference().child("chats").limitToLast(50);
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>().setQuery(query, ChatMessage.class).setLayout(R.layout.message).build();
        FirebaseListAdapter<ChatMessage> adapter = new FirebaseListAdapter<ChatMessage>(options){
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}
