package kellehj1.FYP.birdID;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kellehj1.FYP.birdID.R;

public class MainActivity extends AppCompatActivity {

    Button button_fillscreen, button_create_db;
    EditText editText;


    public static final String EXTRA_MESSAGE = "kellehj1.FYP.birdID.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_fillscreen = findViewById(R.id.button_fillscreen);
        button_create_db = findViewById(R.id.button_create_db);
        editText = findViewById(R.id.editText);
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    /** Called when the user taps the Bird Time button */
    public void birdTime(View view) {
        Intent intent = new Intent(this, FillActivity.class);
        startActivity(intent);
    }

    public void createDB(View view) throws Exception {
        DataBaseHelper dbHelper = new DataBaseHelper(MainActivity.this, "TIT_TABLE", "tits.json");
        if(dbHelper.addBirds()) {
            Toast.makeText(MainActivity.this, "DB created", Toast.LENGTH_LONG).show();
        }
        else {
            throw new Exception("Error adding birds to database.");
        }
        //ModelTit modelTit = new ModelTit();
    }
}