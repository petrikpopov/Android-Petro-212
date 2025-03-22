package itstep.learning.android_212.chat;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import itstep.learning.android_212.R;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private final TextView tvAuthor;
    private final TextView tvText;

    public ChatMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        tvAuthor = itemView.findViewById( R.id.chat_msg_author ) ;
        tvText = itemView.findViewById( R.id.chat_msg_text ) ;
        itemView.setOnClickListener( v -> Toast.makeText(v.getContext(), tvText.getText(), Toast.LENGTH_SHORT).show());
    }

    public TextView getTvAuthor() {
        return tvAuthor;
    }

    public TextView getTvText() {
        return tvText;
    }
}
/*
ViewHolder - проміжна ланка між розміткою та кодом, яка відповідає
за взаємодію RecyclerView з одиничними представленнями (розміткою одного елемента)
Наприклад, розділяє доступ за id, однаковими між різними елементами.
 */