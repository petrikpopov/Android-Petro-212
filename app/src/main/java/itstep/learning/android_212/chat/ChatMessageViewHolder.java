package itstep.learning.android_212.chat;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import itstep.learning.android_212.ChatActivity;
import itstep.learning.android_212.R;
import itstep.learning.android_212.orm.ChatMessage;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    public static final SimpleDateFormat momentFormat =
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT );

    private final LinearLayout layout;
    private final TextView tvAuthor;
    private final TextView tvText;
    private final TextView tvMoment;
    private ChatMessage chatMessage;

    public ChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        layout = itemView.findViewById( R.id.chat_msg_layout );
        tvAuthor = itemView.findViewById( R.id.chat_msg_author ) ;
        tvText = itemView.findViewById( R.id.chat_msg_text ) ;
        tvMoment = itemView.findViewById( R.id.chat_msg_moment ) ;
        chatMessage = null;
        itemView.setOnClickListener( v -> Toast.makeText(itemView.getContext().getApplicationContext(), chatMessage.getText(), Toast.LENGTH_SHORT).show());
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        tvAuthor.setText( chatMessage.getAuthor() );
        tvText.setText( chatMessage.getText() );
        tvMoment.setText( momentFormat.format( chatMessage.getMoment() ) );
        if( chatMessage.getAuthor().equals( ChatActivity.author ) ) {
            layout.setGravity( Gravity.END );
        }
        else {
            layout.setGravity( Gravity.START );
        }
    }
}
/*
ViewHolder - проміжна ланка між розміткою та кодом, яка відповідає
за взаємодію RecyclerView з одиничними представленнями (розміткою одного елемента)
Наприклад, розділяє доступ за id, однаковими між різними елементами.
 */