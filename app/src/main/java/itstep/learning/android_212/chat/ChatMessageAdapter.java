package itstep.learning.android_212.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itstep.learning.android_212.R;
import itstep.learning.android_212.orm.ChatMessage;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    private final List<ChatMessage> messages;   // дані для показу, що передаються ззовні

    public ChatMessageAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from( parent.getContext() )
                .inflate( R.layout.chat_message, parent, false );
        return new ChatMessageViewHolder( view );
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = messages.get( position ) ;
        holder.getTvAuthor().setText( message.getAuthor() );
        holder.getTvText().setText( message.getText() );
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
/*
Посередник між даними (колекцією ORM) та представленням (контейнером)
 */