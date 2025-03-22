package itstep.learning.android_212;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import itstep.learning.android_212.chat.ChatMessageAdapter;
import itstep.learning.android_212.orm.ChatMessage;

public class ChatActivity extends AppCompatActivity {
    private static final String chatUrl = "https://chat.momentfor.fun/";
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    ExecutorService threadPool;
    RecyclerView rvContainer;
    ChatMessageAdapter chatMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvContainer = findViewById( R.id.chat_rv_container );
        rvContainer.setLayoutManager( new LinearLayoutManager( this ) );
        chatMessageAdapter = new ChatMessageAdapter( chatMessages );
        rvContainer.setAdapter( chatMessageAdapter );

        threadPool = Executors.newFixedThreadPool(3);
        CompletableFuture
                .runAsync( this::loadChat, threadPool )
                .thenRun( () -> runOnUiThread( this::showChat ) ) ;
    }

    private void loadChat() {
        try( InputStream urlStream = new URL( chatUrl ).openStream() ) {
            String content = Services.readStreamToString( urlStream );
            JSONObject jsonObject = new JSONObject( content ) ;
            JSONArray arr = jsonObject.getJSONArray( "data" ) ;
            for (int i = 0; i < arr.length(); i++) {
                chatMessages.add( ChatMessage.fromJson( arr.getJSONObject( i ) ) ) ;
            }
        }
        catch (MalformedURLException ex) {
            Log.e("loadChat", "MalformedURLException: " + ex.getMessage());
        }
        catch (IOException ex) {
            Log.e("loadChat", "IOException: " + ex.getMessage());
        }
        catch( JSONException ex ) {
            Log.e("loadChat", "JSONException: " + ex.getMessage());
        }
    }

    private void showChat() {
        chatMessageAdapter.notifyItemRangeChanged(0, chatMessages.size());
    }

    @Override
    protected void onDestroy() {
        threadPool.shutdownNow();
        super.onDestroy();
    }
}
/*
Д.З. Повторити вправу з одержання курсів валют з
використанням для показу результатів елемент
RecyclerView
 */